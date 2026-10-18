package pt.flawless.ftags;

import pt.flawless.fapi.database.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Acesso à tabela {@code ftags_owned}. Todos os métodos correm no pool do {@link DatabaseService} e os
 * futuros falham com a {@link SQLException} original.
 */
public class SqlTagsRepository implements TagsRepository {

    private static final String SELECT_OWNED = "SELECT tag_id, visible FROM ftags_owned WHERE uuid = ?";
    private static final String INSERT = "INSERT IGNORE INTO ftags_owned (uuid, tag_id) VALUES (?, ?)";
    private static final String DELETE = "DELETE FROM ftags_owned WHERE uuid = ? AND tag_id = ?";
    private static final String LOCK_HOLDERS = "SELECT uuid FROM ftags_owned WHERE tag_id = ? FOR UPDATE";
    private static final String DELETE_TAG = "DELETE FROM ftags_owned WHERE tag_id = ?";
    private static final String EXISTS = "SELECT 1 FROM ftags_owned WHERE uuid = ? AND tag_id = ?";
    private static final String SET_VISIBLE = "UPDATE ftags_owned SET visible = ? WHERE uuid = ? AND tag_id = ?";

    private final DatabaseService database;

    public SqlTagsRepository(DatabaseService database) {
        this.database = database;
    }

    @Override
    public CompletableFuture<List<OwnedTag>> findOwned(UUID uuid) {
        return database.supplyAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SELECT_OWNED)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    List<OwnedTag> owned = new ArrayList<>();
                    while (rs.next()) {
                        owned.add(new OwnedTag(rs.getString(1), rs.getBoolean(2)));
                    }
                    return owned;
                }
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> insert(UUID uuid, String tagId) {
        return database.supplyAsync(connection -> insertRow(connection, uuid, tagId));
    }

    @Override
    public CompletableFuture<Boolean> delete(UUID uuid, String tagId) {
        return database.supplyAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(DELETE)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, tagId);
                return ps.executeUpdate() > 0;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> replaceHolder(UUID uuid, String tagId) {
        return database.supplyAsync(connection -> inTransaction(connection, () -> {
            // bloqueia os detentores atuais para duas trocas em paralelo não deixarem dois detentores
            List<String> holders = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(LOCK_HOLDERS)) {
                ps.setString(1, tagId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        holders.add(rs.getString(1));
                    }
                }
            }
            if (holders.size() == 1 && holders.get(0).equals(uuid.toString())) {
                return false;
            }
            try (PreparedStatement ps = connection.prepareStatement(DELETE_TAG)) {
                ps.setString(1, tagId);
                ps.executeUpdate();
            }
            insertRow(connection, uuid, tagId);
            return true;
        }));
    }

    @Override
    public CompletableFuture<Boolean> setVisible(UUID uuid, String tagId, boolean visible) {
        // SELECT prévio: o Connector/J devolve linhas encontradas (não alteradas) mas assim não depende disso
        return database.supplyAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(EXISTS)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, tagId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(SET_VISIBLE)) {
                ps.setBoolean(1, visible);
                ps.setString(2, uuid.toString());
                ps.setString(3, tagId);
                ps.executeUpdate();
            }
            return true;
        });
    }

    @Override
    public CompletableFuture<Void> clear(String tagId) {
        return database.runAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(DELETE_TAG)) {
                ps.setString(1, tagId);
                ps.executeUpdate();
            }
        });
    }

    private static boolean insertRow(Connection connection, UUID uuid, String tagId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, tagId);
            return ps.executeUpdate() > 0;
        }
    }

    @FunctionalInterface
    private interface TxWork<R> {
        R run() throws SQLException;
    }

    /** Corre o trabalho numa transação: commit se correr bem, rollback antes de propagar qualquer falha. */
    private static <R> R inTransaction(Connection connection, TxWork<R> work) throws SQLException {
        connection.setAutoCommit(false);
        try {
            R result = work.run();
            connection.commit();
            return result;
        } catch (SQLException | RuntimeException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackFailure) {
                e.addSuppressed(rollbackFailure);
            }
            throw e;
        }
    }
}

package pt.flawless.ftags;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlTagsRepositoryIT {

    private TestDatabaseService db;
    private SqlTagsRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        db = TestDb.service();
        TestDb.resetSchema(db);
        repo = new SqlTagsRepository(db);
    }

    @AfterEach
    void tearDown() {
        if (db != null) {
            db.close();
        }
    }

    private static <T> T get(java.util.concurrent.CompletableFuture<T> f) throws Exception {
        return f.get(30, TimeUnit.SECONDS);
    }

    @Test
    void insertDuplicadoDevolveFalse() throws Exception {
        UUID a = UUID.randomUUID();
        assertTrue(get(repo.insert(a, "vip")));
        assertFalse(get(repo.insert(a, "vip")));
    }

    @Test
    void deleteInexistenteDevolveFalse() throws Exception {
        UUID a = UUID.randomUUID();
        assertFalse(get(repo.delete(a, "vip")));
        get(repo.insert(a, "vip"));
        assertTrue(get(repo.delete(a, "vip")));
        assertTrue(get(repo.findOwned(a)).isEmpty());
    }

    @Test
    void findOwnedDevolveVisibilidade() throws Exception {
        UUID a = UUID.randomUUID();
        get(repo.insert(a, "vip"));
        get(repo.insert(a, "rei"));
        assertTrue(get(repo.setVisible(a, "rei", false)));
        List<OwnedTag> owned = get(repo.findOwned(a));
        assertEquals(2, owned.size());
        assertTrue(owned.contains(new OwnedTag("vip", true)));
        assertTrue(owned.contains(new OwnedTag("rei", false)));
    }

    @Test
    void setVisibleSemPossuirDevolveFalse() throws Exception {
        assertFalse(get(repo.setVisible(UUID.randomUUID(), "vip", false)));
    }

    @Test
    void setVisibleParaMesmoValorDevolveTrue() throws Exception {
        UUID a = UUID.randomUUID();
        get(repo.insert(a, "vip"));
        assertTrue(get(repo.setVisible(a, "vip", true)));
    }

    @Test
    void replaceHolderTrocaDetentor() throws Exception {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        get(repo.insert(a, "rico"));
        assertTrue(get(repo.replaceHolder(b, "rico")));
        assertTrue(get(repo.findOwned(a)).isEmpty());
        assertEquals(List.of(new OwnedTag("rico", true)), get(repo.findOwned(b)));
    }

    @Test
    void replaceHolderMesmoDetentorDevolveFalse() throws Exception {
        UUID a = UUID.randomUUID();
        get(repo.insert(a, "rico"));
        assertFalse(get(repo.replaceHolder(a, "rico")));
        assertEquals(1, get(repo.findOwned(a)).size());
    }

    @Test
    void clearRemoveTodos() throws Exception {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        get(repo.insert(a, "vip"));
        get(repo.insert(b, "vip"));
        get(repo.insert(a, "rei"));
        get(repo.clear("vip"));
        assertEquals(List.of(new OwnedTag("rei", true)), get(repo.findOwned(a)));
        assertTrue(get(repo.findOwned(b)).isEmpty());
    }
}

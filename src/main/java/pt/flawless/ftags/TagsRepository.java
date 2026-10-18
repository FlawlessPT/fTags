package pt.flawless.ftags;

import pt.flawless.fapi.tags.TagSurface;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Persistência das tags dos jogadores. */
public interface TagsRepository {
    CompletableFuture<List<OwnedTag>> findOwned(UUID uuid);

    /** @return {@code false} se já tinha */
    CompletableFuture<Boolean> insert(UUID uuid, String tagId);

    /** @return {@code false} se não tinha */
    CompletableFuture<Boolean> delete(UUID uuid, String tagId);

    /** Tag unique: passa o detentor para o jogador numa só transação. @return {@code false} se já era o detentor único */
    CompletableFuture<Boolean> replaceHolder(UUID uuid, String tagId);

    /** Muda a visibilidade da tag numa superfície. @return {@code false} se o jogador não possui a tag */
    CompletableFuture<Boolean> setVisible(UUID uuid, String tagId, TagSurface surface, boolean visible);

    /** Remove todos os detentores da tag. */
    CompletableFuture<Void> clear(String tagId);
}

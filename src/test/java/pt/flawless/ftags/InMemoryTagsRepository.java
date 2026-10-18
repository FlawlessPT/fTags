package pt.flawless.ftags;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Repositório em memória para testes. */
class InMemoryTagsRepository implements TagsRepository {

    record Key(UUID uuid, String tagId) {}

    final Map<Key, Boolean> rows = new LinkedHashMap<>();

    @Override
    public CompletableFuture<List<OwnedTag>> findOwned(UUID uuid) {
        List<OwnedTag> out = new ArrayList<>();
        rows.forEach((k, v) -> { if (k.uuid().equals(uuid)) out.add(new OwnedTag(k.tagId(), v)); });
        return CompletableFuture.completedFuture(out);
    }

    @Override
    public CompletableFuture<Boolean> insert(UUID uuid, String tagId) {
        return CompletableFuture.completedFuture(rows.putIfAbsent(new Key(uuid, tagId), true) == null);
    }

    @Override
    public CompletableFuture<Boolean> delete(UUID uuid, String tagId) {
        return CompletableFuture.completedFuture(rows.remove(new Key(uuid, tagId)) != null);
    }

    @Override
    public CompletableFuture<Boolean> replaceHolder(UUID uuid, String tagId) {
        boolean onlyHolder = rows.containsKey(new Key(uuid, tagId))
                && rows.keySet().stream().filter(k -> k.tagId().equals(tagId)).count() == 1;
        if (onlyHolder) return CompletableFuture.completedFuture(false);
        rows.keySet().removeIf(k -> k.tagId().equals(tagId));
        rows.put(new Key(uuid, tagId), true);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> setVisible(UUID uuid, String tagId, boolean visible) {
        return CompletableFuture.completedFuture(rows.replace(new Key(uuid, tagId), visible) != null);
    }

    @Override
    public CompletableFuture<Void> clear(String tagId) {
        rows.keySet().removeIf(k -> k.tagId().equals(tagId));
        return CompletableFuture.completedFuture(null);
    }
}

package pt.flawless.ftags;

import pt.flawless.fapi.tags.TagsApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Regras das tags, sem dependência do Bukkit. Os futuros completam onde o repositório os completar. */
public class TagsService implements TagsApi {

    private final TagsConfig config;
    private final TagsRepository repository;

    public TagsService(TagsConfig config, TagsRepository repository) {
        this.config = config;
        this.repository = repository;
    }

    @Override
    public CompletableFuture<List<String>> getVisibleTags(UUID uuid) {
        return list(uuid).thenApply(owned -> owned.stream()
                .filter(OwnedTag::visible)
                .map(o -> config.find(o.tagId()).orElseThrow().text())
                .toList());
    }

    @Override
    public CompletableFuture<Boolean> give(UUID uuid, String tagId) {
        TagDefinition tag = require(tagId);
        return tag.unique() ? repository.replaceHolder(uuid, tagId) : repository.insert(uuid, tagId);
    }

    @Override
    public CompletableFuture<Boolean> take(UUID uuid, String tagId) {
        require(tagId);
        return repository.delete(uuid, tagId);
    }

    /** @return vazio se o jogador não possui a tag; senão o novo estado de visibilidade */
    public CompletableFuture<Optional<Boolean>> toggle(UUID uuid, String tagId) {
        return repository.findOwned(uuid).thenCompose(owned -> {
            Optional<OwnedTag> current = owned.stream().filter(o -> o.tagId().equals(tagId)).findFirst();
            if (current.isEmpty()) return CompletableFuture.completedFuture(Optional.<Boolean>empty());
            boolean next = !current.get().visible();
            return repository.setVisible(uuid, tagId, next)
                    .thenApply(ok -> ok ? Optional.of(next) : Optional.<Boolean>empty());
        });
    }

    /** Tags do jogador que ainda existem na config, pela ordem da config. */
    public CompletableFuture<List<OwnedTag>> list(UUID uuid) {
        return repository.findOwned(uuid).thenApply(owned -> {
            List<OwnedTag> result = new ArrayList<>();
            for (TagDefinition def : config.tags()) {
                owned.stream().filter(o -> o.tagId().equals(def.id())).findFirst().ifPresent(result::add);
            }
            return result;
        });
    }

    /** Presente: dá a tag a esse jogador; vazio: ninguém a tem. */
    public CompletableFuture<Void> applyRichest(String tagId, Optional<UUID> richest) {
        return richest.isPresent()
                ? give(richest.get(), tagId).thenApply(b -> (Void) null)
                : repository.clear(tagId);
    }

    private TagDefinition require(String tagId) {
        return config.find(tagId).orElseThrow(() -> new IllegalArgumentException("Tag desconhecida: " + tagId));
    }
}

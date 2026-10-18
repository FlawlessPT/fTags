package pt.flawless.ftags;

import pt.flawless.fapi.economy.EconomyApi;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Atribui as tags "auto: richest" ao jogador mais rico. Um erro nunca retira o detentor atual. */
public class RichestRefreshTask implements Runnable {

    private final TagsService service;
    private final EconomyApi economy;
    private final TagsConfig config;
    private final BiConsumer<TagDefinition, UUID> onNewHolder;
    private final Consumer<Throwable> onError;

    /**
     * @param onNewHolder chamado fora da thread principal quando o detentor da tag muda; uma falha aqui
     *                    vai para {@code onError} e não afeta a atribuição
     */
    public RichestRefreshTask(TagsService service, EconomyApi economy, TagsConfig config,
                              BiConsumer<TagDefinition, UUID> onNewHolder, Consumer<Throwable> onError) {
        this.service = service;
        this.economy = economy;
        this.config = config;
        this.onNewHolder = onNewHolder;
        this.onError = onError;
    }

    @Override
    public void run() {
        for (TagDefinition tag : config.tags()) {
            if (!"richest".equals(tag.auto())) continue;
            try {
                economy.getRichest()
                        .thenCompose(r -> service.applyRichest(tag.id(), r)
                                .thenAccept(changed -> {
                                    if (changed) onNewHolder.accept(tag, r.orElseThrow());
                                }))
                        .exceptionally(e -> { onError.accept(e); return null; });
            } catch (RuntimeException e) {
                onError.accept(e);
            }
        }
    }
}

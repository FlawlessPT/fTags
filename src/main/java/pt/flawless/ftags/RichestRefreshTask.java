package pt.flawless.ftags;

import pt.flawless.fapi.economy.EconomyApi;

import java.util.function.Consumer;

/** Atribui as tags "auto: richest" ao jogador mais rico. Um erro nunca retira o detentor atual. */
public class RichestRefreshTask implements Runnable {

    private final TagsService service;
    private final EconomyApi economy;
    private final TagsConfig config;
    private final Consumer<Throwable> onError;

    public RichestRefreshTask(TagsService service, EconomyApi economy, TagsConfig config, Consumer<Throwable> onError) {
        this.service = service;
        this.economy = economy;
        this.config = config;
        this.onError = onError;
    }

    @Override
    public void run() {
        for (TagDefinition tag : config.tags()) {
            if (!"richest".equals(tag.auto())) continue;
            try {
                economy.getRichest()
                        .thenCompose(r -> service.applyRichest(tag.id(), r))
                        .exceptionally(e -> { onError.accept(e); return null; });
            } catch (RuntimeException e) {
                onError.accept(e);
            }
        }
    }
}

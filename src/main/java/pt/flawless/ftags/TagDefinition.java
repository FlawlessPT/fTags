package pt.flawless.ftags;

import java.util.List;

/**
 * Definição de uma tag da config.
 *
 * @param id     identificador estável
 * @param text   texto já com {@code §} (convertido de {@code &})
 * @param unique só um jogador de cada vez pode ter a tag
 * @param auto     regra de atribuição automática (ex.: {@code richest}); {@code null} = manual
 * @param announce linhas do anúncio de novo detentor, já com {@code §} (nunca {@code null}; vazia = sem anúncio)
 */
public record TagDefinition(String id, String text, boolean unique, String auto, List<String> announce) {}

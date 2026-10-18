package pt.flawless.ftags;

/**
 * Definição de uma tag da config.
 *
 * @param id     identificador estável
 * @param text   texto já com {@code §} (convertido de {@code &})
 * @param unique só um jogador de cada vez pode ter a tag
 * @param auto   regra de atribuição automática (ex.: {@code richest}); {@code null} = manual
 */
public record TagDefinition(String id, String text, boolean unique, String auto) {}

package pt.flawless.ftags;

import pt.flawless.fapi.tags.TagSurface;

import java.util.Optional;

/** Disposição do menu /tags (27 slots): por coluna, o item da tag, o vidro do chat e o vidro do tab. Sem Bukkit. */
final class TagsMenuLayout {
    static final int MAX_TAGS = 9;
    static final int SIZE = 27;
    private static final int ROW = 9;

    /** Vidro que alterna a visibilidade de uma superfície numa coluna. */
    record Target(int column, TagSurface surface) {}

    private TagsMenuLayout() {}

    static int tagSlot(int column) {
        return column;
    }

    static int glassSlot(int column, TagSurface surface) {
        return column + (surface == TagSurface.CHAT ? ROW : 2 * ROW);
    }

    /** Data value do painel de vidro: 5 verde (visível), 14 vermelho (oculto). */
    static int glassData(boolean visible) {
        return visible ? 5 : 14;
    }

    static String glassName(TagSurface surface, boolean visible) {
        return (visible ? "§a" : "§c") + (surface == TagSurface.CHAT ? "Chat" : "Tab") + ": " + (visible ? "Visível" : "Oculto");
    }

    static Optional<Target> targetOf(int slot) {
        if (slot < ROW || slot >= SIZE) return Optional.empty();
        return Optional.of(new Target(slot % ROW, slot < 2 * ROW ? TagSurface.CHAT : TagSurface.TAB));
    }
}

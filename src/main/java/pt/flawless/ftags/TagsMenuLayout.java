package pt.flawless.ftags;

import pt.flawless.fapi.tags.TagSurface;

import java.util.Optional;

/**
 * Disposição do menu /tags (54 slots, 6 linhas) com borda de vidro. O interior são as linhas 1-4 e as colunas 1-7;
 * por tag: item na linha 1, vidro do chat na linha 2, vidro do tab na linha 3 (linha 4 vazia). Sem Bukkit.
 */
final class TagsMenuLayout {
    static final int MAX_TAGS = 7;
    static final int SIZE = 54;
    private static final int ROW = 9;

    /** Vidro que alterna a visibilidade de uma superfície numa coluna. */
    record Target(int column, TagSurface surface) {}

    private TagsMenuLayout() {}

    static int tagSlot(int column) {
        return ROW + 1 + column;
    }

    static int glassSlot(int column, TagSurface surface) {
        return tagSlot(column) + (surface == TagSurface.CHAT ? ROW : 2 * ROW);
    }

    /** Data value do painel de vidro: 5 verde (visível), 14 vermelho (oculto). */
    static int glassData(boolean visible) {
        return visible ? 5 : 14;
    }

    static String glassName(TagSurface surface, boolean visible) {
        return (visible ? "§a" : "§c") + (surface == TagSurface.CHAT ? "Chat" : "Tab") + ": " + (visible ? "Visível" : "Oculto");
    }

    static Optional<Target> targetOf(int slot) {
        int offset = slot - glassSlot(0, TagSurface.CHAT);
        if (offset < 0 || offset >= 2 * ROW || offset % ROW >= MAX_TAGS) return Optional.empty();
        return Optional.of(new Target(offset % ROW, offset < ROW ? TagSurface.CHAT : TagSurface.TAB));
    }

    /** Linhas 0 e 5, colunas 0 e 8. */
    static boolean isBorder(int slot) {
        int row = slot / ROW, col = slot % ROW;
        return row == 0 || row == SIZE / ROW - 1 || col == 0 || col == ROW - 1;
    }
}

package pt.flawless.ftags;

import org.junit.jupiter.api.Test;
import pt.flawless.fapi.tags.TagSurface;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagsMenuLayoutTest {

    @Test
    void slotsDaColuna() {
        assertEquals(10, TagsMenuLayout.tagSlot(0));
        assertEquals(19, TagsMenuLayout.glassSlot(0, TagSurface.CHAT));
        assertEquals(28, TagsMenuLayout.glassSlot(0, TagSurface.TAB));
        assertEquals(16, TagsMenuLayout.tagSlot(6));
        assertEquals(25, TagsMenuLayout.glassSlot(6, TagSurface.CHAT));
        assertEquals(34, TagsMenuLayout.glassSlot(6, TagSurface.TAB));
        assertEquals(7, TagsMenuLayout.MAX_TAGS);
        assertEquals(54, TagsMenuLayout.SIZE);
    }

    @Test
    void vidroVerdeEVermelho() {
        assertEquals(5, TagsMenuLayout.glassData(true));
        assertEquals(14, TagsMenuLayout.glassData(false));
    }

    @Test
    void nomesDosVidros() {
        assertEquals("§aChat: Visível", TagsMenuLayout.glassName(TagSurface.CHAT, true));
        assertEquals("§cChat: Oculto", TagsMenuLayout.glassName(TagSurface.CHAT, false));
        assertEquals("§aTab: Visível", TagsMenuLayout.glassName(TagSurface.TAB, true));
        assertEquals("§cTab: Oculto", TagsMenuLayout.glassName(TagSurface.TAB, false));
    }

    @Test
    void targetOfSlotDeVidro() {
        assertEquals(new TagsMenuLayout.Target(0, TagSurface.CHAT), TagsMenuLayout.targetOf(19).orElseThrow());
        assertEquals(new TagsMenuLayout.Target(6, TagSurface.CHAT), TagsMenuLayout.targetOf(25).orElseThrow());
        assertEquals(new TagsMenuLayout.Target(0, TagSurface.TAB), TagsMenuLayout.targetOf(28).orElseThrow());
        assertEquals(new TagsMenuLayout.Target(6, TagSurface.TAB), TagsMenuLayout.targetOf(34).orElseThrow());
    }

    @Test
    void targetOfOutrosSlotsEEmpty() {
        for (int slot : new int[]{0, 8, 9, 10, 16, 18, 26, 27, 35, 37, 43, 45, 53, -1, 54})
            assertTrue(TagsMenuLayout.targetOf(slot).isEmpty(), "slot " + slot);
    }

    @Test
    void borda() {
        for (int slot : new int[]{0, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 53})
            assertTrue(TagsMenuLayout.isBorder(slot), "slot " + slot);
        for (int slot : new int[]{10, 16, 19, 25, 28, 34, 37, 43})
            assertFalse(TagsMenuLayout.isBorder(slot), "slot " + slot);
    }
}

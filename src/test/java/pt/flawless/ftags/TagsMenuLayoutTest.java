package pt.flawless.ftags;

import org.junit.jupiter.api.Test;
import pt.flawless.fapi.tags.TagSurface;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagsMenuLayoutTest {

    @Test
    void slotsDaColuna() {
        assertEquals(0, TagsMenuLayout.tagSlot(0));
        assertEquals(9, TagsMenuLayout.glassSlot(0, TagSurface.CHAT));
        assertEquals(18, TagsMenuLayout.glassSlot(0, TagSurface.TAB));
        assertEquals(8, TagsMenuLayout.tagSlot(8));
        assertEquals(17, TagsMenuLayout.glassSlot(8, TagSurface.CHAT));
        assertEquals(26, TagsMenuLayout.glassSlot(8, TagSurface.TAB));
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
        assertEquals(new TagsMenuLayout.Target(0, TagSurface.CHAT), TagsMenuLayout.targetOf(9).orElseThrow());
        assertEquals(new TagsMenuLayout.Target(0, TagSurface.TAB), TagsMenuLayout.targetOf(18).orElseThrow());
        assertEquals(new TagsMenuLayout.Target(8, TagSurface.TAB), TagsMenuLayout.targetOf(26).orElseThrow());
    }

    @Test
    void targetOfOutrosSlotsEEmpty() {
        for (int slot : new int[]{0, 8, 27, -1}) assertTrue(TagsMenuLayout.targetOf(slot).isEmpty(), "slot " + slot);
    }
}

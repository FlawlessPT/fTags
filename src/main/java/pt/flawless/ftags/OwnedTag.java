package pt.flawless.ftags;

import pt.flawless.fapi.tags.TagSurface;

/** Tag que um jogador possui e se está visível no chat e na lista de jogadores (tab). */
public record OwnedTag(String tagId, boolean visibleChat, boolean visibleTab) {

    public boolean visible(TagSurface surface) {
        return surface == TagSurface.CHAT ? visibleChat : visibleTab;
    }
}

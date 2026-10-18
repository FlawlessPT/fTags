package pt.flawless.ftags;

/** Tag que um jogador possui e se está visível no chat e na lista de jogadores (tab). */
public record OwnedTag(String tagId, boolean visibleChat, boolean visibleTab) {}

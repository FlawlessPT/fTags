package pt.flawless.ftags;

/** Tag que um jogador possui e se está visível no chat. */
public record OwnedTag(String tagId, boolean visible) {}

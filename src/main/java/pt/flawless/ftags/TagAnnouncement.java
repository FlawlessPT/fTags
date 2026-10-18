package pt.flawless.ftags;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Monta as linhas do anúncio de novo detentor de uma tag. Lógica pura, sem Bukkit. */
final class TagAnnouncement {

    private static final Pattern PLACEHOLDER = Pattern.compile("[{](jogador|tag|saldo)[}]");

    private TagAnnouncement() {}

    /**
     * Substitui {@code {jogador}}, {@code {tag}} e {@code {saldo}} numa só passagem:
     * os valores inseridos nunca são expandidos nem coloridos de novo.
     */
    static List<String> render(List<String> lines, String player, String tagText, String balance) {
        return lines.stream().map(line -> {
            Matcher m = PLACEHOLDER.matcher(line);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                String value = switch (m.group(1)) {
                    case "jogador" -> player;
                    case "tag" -> tagText;
                    default -> balance;
                };
                m.appendReplacement(sb, Matcher.quoteReplacement(value));
            }
            m.appendTail(sb);
            return sb.toString();
        }).toList();
    }
}

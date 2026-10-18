package pt.flawless.ftags;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import pt.flawless.fapi.tags.TagSurface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Configuração do fTags: tags (pela ordem de apresentação), intervalo de refresh e mensagens. */
public record TagsConfig(List<TagDefinition> tags, int refreshSeconds, Map<String, String> messages) {

    private static final int MIN_REFRESH = 5;

    private static final Map<String, String> DEFAULT_MESSAGES = Map.ofEntries(
            Map.entry("no-tag", "&cNão tens essa tag."),
            Map.entry("unknown-tag", "&cTag desconhecida."),
            Map.entry("unavailable", "&cSistema de tags indisponível."),
            Map.entry("usage-general", "&eUso: /tags"),
            Map.entry("usage-help", "&eUso: /tags help"),
            Map.entry("help-header", "&6Comandos de admin:"),
            Map.entry("players-only", "&cApenas jogadores."),
            Map.entry("no-tags", "&7Não tens tags."),
            Map.entry("menu-title", "Tags"),
            Map.entry("glass-chat-visible", TagsMenuLayout.glassName(TagSurface.CHAT, true)),
            Map.entry("glass-chat-hidden", TagsMenuLayout.glassName(TagSurface.CHAT, false)),
            Map.entry("glass-tab-visible", TagsMenuLayout.glassName(TagSurface.TAB, true)),
            Map.entry("glass-tab-hidden", TagsMenuLayout.glassName(TagSurface.TAB, false)),
            Map.entry("usage-add", "&eUso: /tags add <jogador> <tag>"),
            Map.entry("usage-remove", "&eUso: /tags remove <jogador> <tag>"),
            Map.entry("unknown-player", "&cJogador desconhecido."),
            Map.entry("already-has", "&c{jogador} já tem essa tag."),
            Map.entry("not-owned", "&c{jogador} não tem essa tag."),
            Map.entry("given", "&aTag {tag} &adada a {jogador}."),
            Map.entry("taken", "&aTag {tag} &aremovida a {jogador}."),
            Map.entry("received", "&aRecebeste a tag {tag}&a."),
            Map.entry("error", "&cOcorreu um erro. Tenta mais tarde."));

    public static TagsConfig from(ConfigurationSection c) {
        List<TagDefinition> tags = new ArrayList<>();
        for (Map<?, ?> m : c.getMapList("tags")) {
            Object id = m.get("id");
            Object text = m.get("text");
            if (id == null || text == null) continue;
            Object unique = m.get("unique");
            Object auto = m.get("auto");
            tags.add(new TagDefinition(
                    String.valueOf(id),
                    ChatColor.translateAlternateColorCodes('&', String.valueOf(text)),
                    unique != null && Boolean.parseBoolean(String.valueOf(unique)),
                    auto == null ? null : String.valueOf(auto),
                    announceLines(m.get("announce"))));
        }
        int refresh = Math.max(MIN_REFRESH, c.getInt("refresh-seconds", 60));
        Map<String, String> messages = new HashMap<>();
        DEFAULT_MESSAGES.forEach((k, v) ->
                messages.put(k, ChatColor.translateAlternateColorCodes('&', c.getString("messages." + k, v))));
        return new TagsConfig(List.copyOf(tags), refresh, Map.copyOf(messages));
    }

    /** Ausente ou que não seja lista = sem anúncio (uma config antiga não tem a chave). */
    private static List<String> announceLines(Object raw) {
        if (!(raw instanceof List<?> list)) return List.of();
        return list.stream()
                .map(l -> ChatColor.translateAlternateColorCodes('&', l == null ? "" : String.valueOf(l)))
                .toList();
    }

    public Optional<TagDefinition> find(String id) {
        return tags.stream().filter(t -> t.id().equals(id)).findFirst();
    }
}

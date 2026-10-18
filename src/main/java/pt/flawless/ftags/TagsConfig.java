package pt.flawless.ftags;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Configuração do fTags: tags (pela ordem de apresentação), intervalo de refresh e mensagens. */
public record TagsConfig(List<TagDefinition> tags, int refreshSeconds, Map<String, String> messages) {

    private static final int MIN_REFRESH = 5;

    private static final Map<String, String> DEFAULT_MESSAGES = Map.of(
            "no-tag", "&cNão tens essa tag.",
            "unknown-tag", "&cTag desconhecida.",
            "now-visible", "&aTag visível.",
            "now-hidden", "&eTag oculta.",
            "unavailable", "&cSistema de tags indisponível.");

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
                    auto == null ? null : String.valueOf(auto)));
        }
        int refresh = Math.max(MIN_REFRESH, c.getInt("refresh-seconds", 60));
        Map<String, String> messages = new HashMap<>();
        DEFAULT_MESSAGES.forEach((k, v) ->
                messages.put(k, ChatColor.translateAlternateColorCodes('&', c.getString("messages." + k, v))));
        return new TagsConfig(List.copyOf(tags), refresh, Map.copyOf(messages));
    }

    public Optional<TagDefinition> find(String id) {
        return tags.stream().filter(t -> t.id().equals(id)).findFirst();
    }
}

package pt.flawless.ftags;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pt.flawless.fapi.tags.TagSurface;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class TagsCommand implements CommandExecutor {
    private static final String ADMIN = "ftags.admin";

    private final TagsService service;
    private final TagsConfig config;
    private final Plugin plugin;

    public TagsCommand(TagsService service, TagsConfig config, Plugin plugin) {
        this.service = service;
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean admin = sender.hasPermission(ADMIN);
        if (args.length > 0 && admin) {
            if (args[0].equalsIgnoreCase("add")) { change(sender, args, true); return true; }
            if (args[0].equalsIgnoreCase("remove")) { change(sender, args, false); return true; }
        }
        if (args.length > 1) {
            sender.sendMessage(msg(admin ? "usage-general-admin" : "usage-general"));
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) list(player); else toggle(player, args[0]);
        return true;
    }

    private void list(Player player) {
        service.list(player.getUniqueId()).whenComplete((owned, e) -> reply(player, e, () -> {
            if (owned.isEmpty()) { player.sendMessage(msg("list-empty")); return; }
            player.sendMessage(msg("list-header"));
            for (OwnedTag o : owned) {
                String text = config.find(o.tagId()).orElseThrow().text();
                String state = msg(o.visibleChat() ? "state-visible" : "state-hidden");
                player.sendMessage(msg("list-line").replace("{tag}", text).replace("{estado}", state));
            }
        }));
    }

    private void toggle(Player player, String tagId) {
        if (config.find(tagId).isEmpty()) { player.sendMessage(msg("unknown-tag")); return; }
        service.toggle(player.getUniqueId(), tagId, TagSurface.CHAT).whenComplete((state, e) -> reply(player, e, () ->
                player.sendMessage(state.isEmpty() ? msg("no-tag") : msg(state.get() ? "now-visible" : "now-hidden"))));
    }

    private void change(CommandSender sender, String[] args, boolean add) {
        if (args.length != 3) { sender.sendMessage(msg(add ? "usage-add" : "usage-remove")); return; }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.isOnline() && !target.hasPlayedBefore()) { sender.sendMessage(msg("unknown-player")); return; }
        String name = target.getName() != null ? target.getName() : args[1];
        String tagId = args[2];
        CompletableFuture<Boolean> result;
        try {
            result = add ? service.give(target.getUniqueId(), tagId) : service.take(target.getUniqueId(), tagId);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(msg("unknown-tag"));
            return;
        }
        String tagText = config.find(tagId).orElseThrow().text();
        result.whenComplete((ok, e) -> reply(sender, e, () -> {
            if (!ok) {
                sender.sendMessage(msg(add ? "already-has" : "not-owned").replace("{jogador}", name));
                return;
            }
            sender.sendMessage(msg(add ? "given" : "taken").replace("{tag}", tagText).replace("{jogador}", name));
            Player online = Bukkit.getPlayer(target.getUniqueId());
            if (add && online != null) online.sendMessage(msg("received").replace("{tag}", tagText));
        }));
    }

    /** Volta à thread principal; em erro (ex.: BD em baixo) avisa o remetente. */
    private void reply(CommandSender sender, Throwable error, Runnable onSuccess) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (error != null) {
                Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
                plugin.getLogger().warning("erro no /tags: " + cause);
                sender.sendMessage(msg("error"));
                return;
            }
            onSuccess.run();
        });
    }

    private String msg(String key) {
        return config.messages().get(key);
    }
}

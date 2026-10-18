package pt.flawless.ftags;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class TagsCommand implements CommandExecutor {
    private static final String ADMIN = "ftags.admin";

    private final TagsService service;
    private final TagsMenu menu;
    private final TagsConfig config;
    private final Plugin plugin;

    public TagsCommand(TagsService service, TagsMenu menu, TagsConfig config, Plugin plugin) {
        this.menu = menu;
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
            if (args[0].equalsIgnoreCase("help")) { help(sender); return true; }
        }
        if (args.length > 0) {
            sender.sendMessage(msg("usage-general"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(msg("players-only"));
            return true;
        }
        menu.open(player);
        return true;
    }

    private void help(CommandSender sender) {
        sender.sendMessage(msg("help-header"));
        for (String key : new String[]{"usage-add", "usage-remove", "usage-help"}) sender.sendMessage(msg(key));
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

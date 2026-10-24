package pt.flawless.ftags;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import pt.flawless.fapi.enums.FInventorySize;
import pt.flawless.fapi.inventory.FInventory;
import pt.flawless.fapi.items.FItemStack;
import pt.flawless.fapi.tags.TagSurface;

import java.util.List;
import java.util.concurrent.CompletionException;

/** Menu /tags: abre o inventário e trata os cliques. Os futuros do serviço completam fora da thread principal. */
public class TagsMenu implements Listener {

    /** Identifica o inventário nos eventos e guarda as tags mostradas, por coluna. */
    private static final class MenuHolder implements InventoryHolder {
        private final List<String> tagIds;
        private Inventory inventory;

        MenuHolder(List<String> tagIds) {
            this.tagIds = tagIds;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private final TagsService service;
    private final TagsConfig config;
    private final Plugin plugin;

    public TagsMenu(TagsService service, TagsConfig config, Plugin plugin) {
        this.service = service;
        this.config = config;
        this.plugin = plugin;
    }

    public void open(Player player) {
        service.list(player.getUniqueId()).whenComplete((owned, e) -> runSync(() -> {
            if (e != null) { fail(player, e); return; }
            if (!player.isOnline()) return;
            List<OwnedTag> shown = owned.size() > TagsMenuLayout.MAX_TAGS ? owned.subList(0, TagsMenuLayout.MAX_TAGS) : owned;
            if (shown.isEmpty()) { player.sendMessage(msg("no-tags")); return; }
            MenuHolder holder = new MenuHolder(shown.stream().map(OwnedTag::tagId).toList());
            FInventory menu = new FInventory(holder, FInventorySize.SIZE_54, msg("menu-title"));
            holder.inventory = menu.getInventory();
            for (int slot = 0; slot < TagsMenuLayout.SIZE; slot++) {
                if (TagsMenuLayout.isBorder(slot)) {
                    holder.inventory.setItem(slot, new FItemStack(Material.GRAY_STAINED_GLASS_PANE, 1, " ").createItem());
                }
            }
            for (int c = 0; c < shown.size(); c++) {
                OwnedTag tag = shown.get(c);
                menu.setItem(TagsMenuLayout.tagSlot(c),
                        new FItemStack(Material.NAME_TAG, 1, config.find(tag.tagId()).orElseThrow().text()));
                for (TagSurface s : TagSurface.values()) {
                    setGlass(holder.inventory, c, s, tag.visible(s));
                }
            }
            player.openInventory(holder.inventory);
        }));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        // Top inventory com o nosso holder: cobre cliques no menu e shift-click/hotbar vindos do inventário do jogador.
        if (!(e.getInventory().getHolder() instanceof MenuHolder holder)) return;
        e.setCancelled(true);
        if (e.getRawSlot() >= TagsMenuLayout.SIZE || !(e.getWhoClicked() instanceof Player player)) return;
        TagsMenuLayout.targetOf(e.getRawSlot())
                .filter(t -> t.column() < holder.tagIds.size())
                .ifPresent(t -> toggle(player, holder, t));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof MenuHolder) e.setCancelled(true);
    }

    private void toggle(Player player, MenuHolder holder, TagsMenuLayout.Target target) {
        service.toggle(player.getUniqueId(), holder.tagIds.get(target.column()), target.surface())
                .whenComplete((state, e) -> runSync(() -> {
                    if (e != null) { fail(player, e); return; }
                    if (state.isEmpty()) { player.sendMessage(msg("no-tag")); return; }
                    setGlass(holder.inventory, target.column(), target.surface(), state.get());
                }));
    }

    private void setGlass(Inventory inventory, int column, TagSurface surface, boolean visible) {
        String name = msg("glass-" + surface.name().toLowerCase() + (visible ? "-visible" : "-hidden"));
        inventory.setItem(TagsMenuLayout.glassSlot(column, surface),
                new FItemStack(TagsMenuLayout.glassMaterial(visible), 1, name).createItem());
    }

    private void fail(Player player, Throwable error) {
        Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
        plugin.getLogger().warning("erro no menu /tags: " + cause);
        player.sendMessage(msg("error"));
    }

    private void runSync(Runnable task) {
        try {
            Bukkit.getScheduler().runTask(plugin, task);
        } catch (RuntimeException ex) {
            // plugin a desligar
        }
    }

    private String msg(String key) {
        return config.messages().get(key);
    }
}

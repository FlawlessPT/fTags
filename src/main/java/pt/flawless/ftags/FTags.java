package pt.flawless.ftags;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fapi.database.DatabaseService;
import pt.flawless.fapi.database.Databases;
import pt.flawless.fapi.economy.Economies;
import pt.flawless.fapi.economy.EconomyApi;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;
import pt.flawless.fapi.tags.TagsApi;

import java.sql.SQLException;
import java.util.List;

public class FTags extends JavaPlugin {
    private final FConsoleLogger consoleLogger = new FConsoleLogger(this.getName());
    private boolean enabledOk = false;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            TagsConfig config = TagsConfig.from(getConfig());
            DatabaseService database = Databases.get();
            database.migrate(this, List.of("db/ftags/V001__create_owned.sql", "db/ftags/V002__add_visible_tab.sql"));
            EconomyApi economy = Economies.get();
            TagsService service = new TagsService(config, new SqlTagsRepository(database));

            getCommand("tags").setExecutor(new TagsCommand(service, config, this));
            getServer().getServicesManager().register(TagsApi.class, service, this, ServicePriority.Normal);

            RichestRefreshTask refresh = new RichestRefreshTask(service, economy, config,
                    e -> consoleLogger.sendMessage("falha ao atualizar a tag do mais rico: " + e.getMessage(),
                            EMessageTypeEnum.ERROR));
            getServer().getScheduler().runTaskTimerAsynchronously(this, refresh, 0L, config.refreshSeconds() * 20L);
        } catch (SQLException | RuntimeException e) {
            // inclui DatabaseUnavailableException e EconomyUnavailableException (RuntimeException)
            consoleLogger.sendMessage("não foi possível iniciar as tags: " + e.getMessage(), EMessageTypeEnum.ERROR);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        enabledOk = true;
        consoleLogger.sendEnablePluginMessage();
    }

    @Override
    public void onDisable() {
        if (enabledOk) {
            consoleLogger.sendDisablePluginMessage();
        }
    }
}

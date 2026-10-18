package pt.flawless.ftags;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.flawless.fapi.economy.EconomyApi;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class RichestRefreshTaskTest {

    private static final String YAML = """
            tags:
              - id: magnata
                text: "&6[Magnata]"
                unique: true
                auto: richest
              - id: vip
                text: "&a[VIP]"
            """;

    InMemoryTagsRepository repo = new InMemoryTagsRepository();
    List<Throwable> errors = new ArrayList<>();
    CompletableFuture<Optional<UUID>> richest;
    RichestRefreshTask task;
    UUID a = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TagsConfig config = TagsConfig.from(YamlConfiguration.loadConfiguration(new StringReader(YAML)));
        TagsService service = new TagsService(config, repo);
        EconomyApi economy = new EconomyApi() {
            public CompletableFuture<Long> getBalance(UUID u) { throw new UnsupportedOperationException(); }
            public CompletableFuture<Long> deposit(UUID u, long c) { throw new UnsupportedOperationException(); }
            public CompletableFuture<Long> withdraw(UUID u, long c) { throw new UnsupportedOperationException(); }
            public CompletableFuture<Long> transfer(UUID f, UUID t, long c) { throw new UnsupportedOperationException(); }
            public String format(long c) { throw new UnsupportedOperationException(); }
            public CompletableFuture<Optional<UUID>> getRichest() { return richest; }
        };
        task = new RichestRefreshTask(service, economy, config, errors::add);
    }

    @Test
    void richestPresentGivesTag() {
        richest = CompletableFuture.completedFuture(Optional.of(a));
        task.run();
        assertTrue(repo.rows.containsKey(new InMemoryTagsRepository.Key(a, "magnata")));
        assertTrue(errors.isEmpty());
    }

    @Test
    void emptyClearsTag() {
        repo.insert(a, "magnata");
        richest = CompletableFuture.completedFuture(Optional.empty());
        task.run();
        assertTrue(repo.rows.isEmpty());
    }

    @Test
    void failureKeepsHolderAndReportsError() {
        repo.insert(a, "magnata");
        richest = CompletableFuture.failedFuture(new RuntimeException("bd"));
        task.run();
        assertTrue(repo.rows.containsKey(new InMemoryTagsRepository.Key(a, "magnata")));
        assertEquals(1, errors.size());
    }
}

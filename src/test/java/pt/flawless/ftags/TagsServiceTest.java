package pt.flawless.ftags;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TagsServiceTest {

    private static final String YAML = """
            refresh-seconds: 1
            tags:
              - id: magnata
                text: "&6[Magnata]"
                unique: true
                auto: richest
              - id: vip
                text: "&a[VIP]"
            """;

    InMemoryTagsRepository repo;
    TagsService service;
    TagsConfig config;
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        config = TagsConfig.from(YamlConfiguration.loadConfiguration(new StringReader(YAML)));
        repo = new InMemoryTagsRepository();
        service = new TagsService(config, repo);
    }

    @Test
    void configLeTagsERefreshMinimo() {
        assertEquals(5, config.refreshSeconds());
        assertEquals(new TagDefinition("magnata", "§6[Magnata]", true, "richest"), config.tags().get(0));
        assertEquals(new TagDefinition("vip", "§a[VIP]", false, null), config.tags().get(1));
        assertTrue(config.find("nada").isEmpty());
        assertNotNull(config.messages().get("no-tag"));
    }

    @Test
    void getVisibleTagsSegueOrdemDaConfig() {
        service.give(a, "vip").join();
        service.give(a, "magnata").join();
        assertEquals(List.of("§6[Magnata]", "§a[VIP]"), service.getVisibleTags(a).join());
    }

    @Test
    void getVisibleTagsIgnoraOcultasEDesconhecidas() {
        service.give(a, "vip").join();
        service.give(a, "magnata").join();
        service.toggle(a, "vip").join();
        repo.insert(a, "removida").join();
        assertEquals(List.of("§6[Magnata]"), service.getVisibleTags(a).join());
    }

    @Test
    void giveDevolveFalseSeJaTinha() {
        assertTrue(service.give(a, "vip").join());
        service.toggle(a, "vip").join();
        assertFalse(service.give(a, "vip").join());
        assertEquals(List.of(new OwnedTag("vip", false)), repo.findOwned(a).join());
    }

    @Test
    void giveUniqueTiraAoAnteriorEDevolveTrue() {
        service.give(a, "magnata").join();
        assertTrue(service.give(b, "magnata").join());
        assertTrue(repo.findOwned(a).join().isEmpty());
        assertEquals(1, repo.findOwned(b).join().size());
    }

    @Test
    void giveUniqueAoDetentorAtualDevolveFalse() {
        service.give(a, "magnata").join();
        assertFalse(service.give(a, "magnata").join());
    }

    @Test
    void giveTagDesconhecidaLancaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service.give(a, "x"));
    }

    @Test
    void takeTagDesconhecidaLancaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service.take(a, "x"));
    }

    @Test
    void takeDevolveFalseSeNaoTinha() {
        assertFalse(service.take(a, "vip").join());
        service.give(a, "vip").join();
        assertTrue(service.take(a, "vip").join());
    }

    @Test
    void toggleAlternaVisibilidade() {
        service.give(a, "vip").join();
        assertEquals(Optional.of(false), service.toggle(a, "vip").join());
        assertEquals(Optional.of(true), service.toggle(a, "vip").join());
    }

    @Test
    void toggleSemPossuirDevolveEmpty() {
        assertEquals(Optional.empty(), service.toggle(a, "vip").join());
    }

    @Test
    void applyRichestPresenteDaTagEVazioLimpa() {
        service.applyRichest("magnata", Optional.of(a)).join();
        assertEquals(1, repo.findOwned(a).join().size());
        service.applyRichest("magnata", Optional.of(b)).join();
        assertTrue(repo.findOwned(a).join().isEmpty());
        service.applyRichest("magnata", Optional.empty()).join();
        assertTrue(repo.findOwned(b).join().isEmpty());
    }
}

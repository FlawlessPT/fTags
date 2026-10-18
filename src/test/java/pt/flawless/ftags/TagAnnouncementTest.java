package pt.flawless.ftags;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TagAnnouncementTest {

    @Test
    void substituiOsTresPlaceholders() {
        assertEquals(List.of("Ana tem §6[M] com 12,50€"),
                TagAnnouncement.render(List.of("{jogador} tem {tag} com {saldo}"), "Ana", "§6[M]", "12,50€"));
    }

    @Test
    void umaSoPassagem() {
        assertEquals(List.of("{saldo} &c {tag} | 1€"),
                TagAnnouncement.render(List.of("{jogador} | {saldo}"), "{saldo} &c {tag}", "t", "1€"));
    }

    @Test
    void linhaVaziaMantida() {
        assertEquals(List.of("", "x", ""), TagAnnouncement.render(List.of("", "x", ""), "a", "t", "s"));
    }

    @Test
    void listaVaziaDevolveVazio() {
        assertEquals(List.of(), TagAnnouncement.render(List.of(), "a", "t", "s"));
    }
}

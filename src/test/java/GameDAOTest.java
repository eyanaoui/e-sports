package com.esports.dao;

import com.esports.models.Game;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameDAOTest {

    GameDAO service = new GameDAO();
    int idGameTest;

    @Test
    @Order(1)
    void testAjouterGame() throws SQLException {
        Game g = new Game();
        g.setName("TestGame");
        g.setSlug("test-game");
        g.setDescription("desc");
        g.setCoverImage("img.png");
        g.setHasRanking(true);

        service.add(g);

        List<Game> games = service.getAll();

        assertFalse(games.isEmpty());

        Game added = games.stream()
                .filter(game -> game.getName().equals("TestGame"))
                .findFirst()
                .orElse(null);

        assertNotNull(added);

        idGameTest = added.getId();
    }

    @Test
    @Order(2)
    void testModifierGame() throws SQLException {
        Game g = new Game();
        g.setId(idGameTest);
        g.setName("GameModifie");
        g.setSlug("modif");
        g.setDescription("modif");
        g.setCoverImage("modif.png");
        g.setHasRanking(false);

        service.update(g);

        List<Game> games = service.getAll();

        boolean trouve = games.stream()
                .anyMatch(game -> game.getName().equals("GameModifie"));

        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerGame() throws SQLException {
        service.delete(idGameTest);

        List<Game> games = service.getAll();

        boolean existe = games.stream()
                .anyMatch(g -> g.getId() == idGameTest);

        assertFalse(existe);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        if (idGameTest != 0) {
            service.delete(idGameTest);
            idGameTest = 0;
        }
    }
}
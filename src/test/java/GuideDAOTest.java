package com.esports.dao;

import com.esports.models.Guide;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GuideDAOTest {

    GuideDAO service = new GuideDAO();
    int idGuideTest;

    @Test
    @Order(1)
    void testAjouterGuide() throws SQLException {
        Guide g = new Guide();
        g.setGameId(1); // must exist
        g.setTitle("TestGuide");
        g.setDescription("desc");
        g.setDifficulty("easy");
        g.setAuthorId(1); // must exist
        g.setCoverImage("img.png");

        service.add(g);

        List<Guide> guides = service.getAll();

        assertFalse(guides.isEmpty());

        Guide added = guides.stream()
                .filter(guide -> guide.getTitle().equals("TestGuide"))
                .findFirst()
                .orElse(null);

        assertNotNull(added);

        idGuideTest = added.getId();
    }

    @Test
    @Order(2)
    void testModifierGuide() throws SQLException {
        Guide g = new Guide();
        g.setId(idGuideTest);
        g.setGameId(1);
        g.setTitle("GuideModifie");
        g.setDescription("modif");
        g.setDifficulty("hard");
        g.setAuthorId(1);
        g.setCoverImage("modif.png");

        service.update(g);

        List<Guide> guides = service.getAll();

        boolean trouve = guides.stream()
                .anyMatch(guide -> guide.getTitle().equals("GuideModifie"));

        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerGuide() throws SQLException {
        service.delete(idGuideTest);

        List<Guide> guides = service.getAll();

        boolean existe = guides.stream()
                .anyMatch(g -> g.getId() == idGuideTest);

        assertFalse(existe);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        if (idGuideTest != 0) {
            service.delete(idGuideTest);
            idGuideTest = 0;
        }
    }
}
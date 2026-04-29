package com.esports.dao;

import com.esports.models.GuideStep;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GuideStepDAOTest {

    GuideStepDAO service = new GuideStepDAO();
    int idStepTest;

    @Test
    @Order(1)
    void testAjouterStep() throws SQLException {
        GuideStep s = new GuideStep();
        s.setGuideId(1); // must exist
        s.setTitle("StepTest");
        s.setContent("content");
        s.setStepOrder(1);
        s.setImage("img.png");
        s.setVideoUrl("url");

        service.add(s);

        List<GuideStep> steps = service.getAll();

        assertFalse(steps.isEmpty());

        GuideStep added = steps.stream()
                .filter(step -> step.getTitle().equals("StepTest"))
                .findFirst()
                .orElse(null);

        assertNotNull(added);

        idStepTest = added.getId();
    }

    @Test
    @Order(2)
    void testModifierStep() throws SQLException {
        GuideStep s = new GuideStep();
        s.setId(idStepTest);
        s.setTitle("StepModifie");
        s.setContent("modif");
        s.setStepOrder(2);
        s.setImage("modif.png");
        s.setVideoUrl("modif");

        service.update(s);

        List<GuideStep> steps = service.getAll();

        boolean trouve = steps.stream()
                .anyMatch(step -> step.getTitle().equals("StepModifie"));

        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerStep() throws SQLException {
        service.delete(idStepTest);

        List<GuideStep> steps = service.getAll();

        boolean existe = steps.stream()
                .anyMatch(s -> s.getId() == idStepTest);

        assertFalse(existe);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        if (idStepTest != 0) {
            service.delete(idStepTest);
            idStepTest = 0;
        }
    }
}
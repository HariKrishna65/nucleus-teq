package com.interview.tracker.entity;

import com.interview.tracker.constants.Stage;
import com.interview.tracker.constants.StageStatus;
import org.junit.jupiter.api.Test;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityAccessorTest {

    @Test
    void feedbackStatusAliasesResult() {
        Feedback feedback = new Feedback();

        feedback.setStatus("Selected");

        assertEquals("Selected", feedback.getResult());
        assertEquals("Selected", feedback.getStatus());
    }

    @Test
    void userBooleanGettersHandleNullDefaults() {
        User user = new User();

        assertFalse(user.isEmailVerified());
        assertTrue(user.isActive());
    }

    @Test
    void candidateAccessorsRoundTripApplicationFields() {
        Candidate candidate = new Candidate();
        LocalDate birthDate = LocalDate.of(2000, 1, 1);
        LocalDateTime applicationDate = LocalDateTime.of(2026, 5, 5, 10, 0);
        User user = new User();
        JobDescription jd = new JobDescription();

        candidate.setId(1L);
        candidate.setFullName("Candidate Name");
        candidate.setPhone("9876543210");
        candidate.setDateOfBirth(birthDate);
        candidate.setApplicationDate(applicationDate);
        candidate.setExperience(2.0);
        candidate.setStage(Stage.SCREENING);
        candidate.setStageStatus(StageStatus.PENDING);
        candidate.setUser(user);
        candidate.setJd(jd);

        assertEquals(1L, candidate.getId());
        assertEquals("Candidate Name", candidate.getFullName());
        assertEquals("9876543210", candidate.getPhone());
        assertEquals(birthDate, candidate.getDateOfBirth());
        assertEquals(applicationDate, candidate.getApplicationDate());
        assertEquals(2.0, candidate.getExperience());
        assertEquals(Stage.SCREENING, candidate.getStage());
        assertEquals(StageStatus.PENDING, candidate.getStageStatus());
        assertEquals(user, candidate.getUser());
        assertEquals(jd, candidate.getJd());
    }

    @Test
    void interviewPanelsDefaultToMutableEmptyList() {
        Interview interview = new Interview();
        Panel panel = new Panel();

        interview.getPanels().add(panel);

        assertEquals(List.of(panel), interview.getPanels());
    }

    @Test
    void entityBeanAccessorsRoundTripCommonPropertyTypes() throws Exception {
        for (Class<?> type : List.of(
                Candidate.class,
                Feedback.class,
                Interview.class,
                JobDescription.class,
                Panel.class,
                User.class
        )) {
            Object bean = type.getConstructor().newInstance();
            for (PropertyDescriptor property : Introspector.getBeanInfo(type, Object.class).getPropertyDescriptors()) {
                if (property.getReadMethod() == null || property.getWriteMethod() == null) {
                    continue;
                }
                Object value = valueFor(property.getPropertyType());
                if (value == null) {
                    continue;
                }

                property.getWriteMethod().invoke(bean, value);

                if (property.getPropertyType().isArray()) {
                    assertArrayEquals((byte[]) value, (byte[]) property.getReadMethod().invoke(bean));
                } else {
                    assertEquals(value, property.getReadMethod().invoke(bean));
                }
            }
        }
    }

    private Object valueFor(Class<?> type) {
        if (type == String.class) return "value";
        if (type == Long.class) return 1L;
        if (type == Integer.class) return 30;
        if (type == Double.class) return 2.5;
        if (type == boolean.class || type == Boolean.class) return true;
        if (type == LocalDate.class) return LocalDate.of(2026, 5, 5);
        if (type == LocalDateTime.class) return LocalDateTime.of(2026, 5, 5, 9, 30);
        if (type == byte[].class) return "resume".getBytes();
        if (type == Stage.class) return Stage.L1_TECH;
        if (type == StageStatus.class) return StageStatus.COMPLETED;
        if (type == Candidate.class) return new Candidate();
        if (type == Feedback.class) return new Feedback();
        if (type == Interview.class) return new Interview();
        if (type == JobDescription.class) return new JobDescription();
        if (type == Panel.class) return new Panel();
        if (type == User.class) return new User();
        if (type == List.class) return new ArrayList<Panel>();
        return null;
    }
}

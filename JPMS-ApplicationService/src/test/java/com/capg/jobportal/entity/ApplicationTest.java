package com.capg.jobportal.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.capg.jobportal.enums.ApplicationStatus;

class ApplicationTest {

    @Test
    void defaultConstructor() {
        Application app = new Application();
        assertNull(app.getId());
        assertNull(app.getUserId());
        assertEquals(ApplicationStatus.APPLIED, app.getStatus());
    }

    @Test
    void gettersAndSetters() {
        Application app = new Application();
        app.setId(1L);
        app.setUserId(100L);
        app.setJobId(200L);
        app.setResumeUrl("resume.pdf");
        app.setCoverLetter("cover");
        app.setStatus(ApplicationStatus.SHORTLISTED);
        app.setRecruiterNote("Good");

        assertEquals(1L, app.getId());
        assertEquals(100L, app.getUserId());
        assertEquals(200L, app.getJobId());
        assertEquals("resume.pdf", app.getResumeUrl());
        assertEquals("cover", app.getCoverLetter());
        assertEquals(ApplicationStatus.SHORTLISTED, app.getStatus());
        assertEquals("Good", app.getRecruiterNote());
    }

    @Test
    void onCreate_setTimestamps() throws Exception {
        Application app = new Application();
        assertNull(app.getAppliedAt());
        assertNull(app.getUpdatedAt());

        Method onCreate = Application.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(app);

        assertNotNull(app.getAppliedAt());
        assertNotNull(app.getUpdatedAt());
    }

    @Test
    void onUpdate_setsUpdatedAt() throws Exception {
        Application app = new Application();
        Method onUpdate = Application.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(app);
        assertNotNull(app.getUpdatedAt());
    }
}

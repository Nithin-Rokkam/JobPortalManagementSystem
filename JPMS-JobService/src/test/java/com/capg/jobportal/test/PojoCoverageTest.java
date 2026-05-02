package com.capg.jobportal.test;

import org.junit.jupiter.api.Test;
import java.io.File;
import com.capg.jobportal.test.util.PojoTestUtils;

public class PojoCoverageTest {

    @Test
    public void testAllPojos() throws Exception {
        String[] packages = {
            "com.capg.jobportal.dto",
            "com.capg.jobportal.model",
            "com.capg.jobportal.entity",
            "com.capg.jobportal.event"
        };

        for (String pkg : packages) {
            String path = "src/main/java/" + pkg.replace('.', '/');
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                for (File file : dir.listFiles()) {
                    if (file.getName().endsWith(".java")) {
                        String className = pkg + "." + file.getName().replace(".java", "");
                        try {
                            Class<?> clazz = Class.forName(className);
                            PojoTestUtils.validateAccessors(clazz);
                        } catch (ClassNotFoundException e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testJobEntityLifecycle() throws Exception {
        com.capg.jobportal.entity.Job job = new com.capg.jobportal.entity.Job();
        
        java.lang.reflect.Method onCreate = com.capg.jobportal.entity.Job.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        java.lang.reflect.Method onUpdate = com.capg.jobportal.entity.Job.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);

        job.setStatus(null);
        onCreate.invoke(job);
        org.junit.jupiter.api.Assertions.assertNotNull(job.getCreatedAt());
        org.junit.jupiter.api.Assertions.assertNotNull(job.getUpdatedAt());
        org.junit.jupiter.api.Assertions.assertEquals(com.capg.jobportal.enums.JobStatus.ACTIVE, job.getStatus());

        job.setStatus(com.capg.jobportal.enums.JobStatus.CLOSED);
        onCreate.invoke(job);
        org.junit.jupiter.api.Assertions.assertEquals(com.capg.jobportal.enums.JobStatus.CLOSED, job.getStatus());

        onUpdate.invoke(job);
        org.junit.jupiter.api.Assertions.assertNotNull(job.getUpdatedAt());
    }
}

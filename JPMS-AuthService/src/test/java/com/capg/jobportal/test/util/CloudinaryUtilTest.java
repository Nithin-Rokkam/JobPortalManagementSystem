package com.capg.jobportal.test.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import com.capg.jobportal.util.CloudinaryUtil;

class CloudinaryUtilTest {

    private final CloudinaryUtil cloudinaryUtil = new CloudinaryUtil();

    // ─── Image Validation ──────────────────────────────────────────

    @Test
    void uploadProfilePicture_nullFile_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> cloudinaryUtil.uploadProfilePicture(null));
    }

    @Test
    void uploadProfilePicture_emptyFile_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> cloudinaryUtil.uploadProfilePicture(file));
    }

    @Test
    void uploadProfilePicture_tooLargeFile_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(3L * 1024 * 1024);
        assertThrows(IllegalArgumentException.class,
                () -> cloudinaryUtil.uploadProfilePicture(file));
    }

    @Test
    void uploadProfilePicture_invalidContentType_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");
        assertThrows(IllegalArgumentException.class,
                () -> cloudinaryUtil.uploadProfilePicture(file));
    }

    @Test
    void uploadProfilePicture_nullContentType_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> cloudinaryUtil.uploadProfilePicture(file));
    }

    // ─── Resume Validation ──────────────────────────────────────────

    @Test
    void uploadResume_nullFile_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> cloudinaryUtil.uploadResume(null));
    }

    @Test
    void uploadResume_emptyFile_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> cloudinaryUtil.uploadResume(file));
    }

    @Test
    void uploadResume_tooLargeFile_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(6L * 1024 * 1024);
        assertThrows(IllegalArgumentException.class,
                () -> cloudinaryUtil.uploadResume(file));
    }

    @Test
    void uploadResume_invalidContentType_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("image/jpeg");
        assertThrows(IllegalArgumentException.class,
                () -> cloudinaryUtil.uploadResume(file));
    }

    @Test
    void uploadResume_nullContentType_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> cloudinaryUtil.uploadResume(file));
    }
}

package com.capg.jobportal.test.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.capg.jobportal.dto.ErrorResponse;
import com.capg.jobportal.dto.JobRequestDTO;
import com.capg.jobportal.dto.JobResponseDTO;
import com.capg.jobportal.dto.PagedResponse;

class DtoTest {

    @Test
    void jobRequestDTO_gettersSetters() {
        JobRequestDTO dto = new JobRequestDTO();
        dto.setTitle("Dev");
        dto.setCompanyName("Corp");
        dto.setLocation("NYC");
        dto.setSalary(new BigDecimal("100000"));
        dto.setExperienceYears(5);
        dto.setJobType("FULL_TIME");
        dto.setSkillsRequired("Java");
        dto.setDescription("desc");
        dto.setStatus("ACTIVE");
        dto.setDeadline(LocalDate.of(2026, 12, 31));

        assertEquals("Dev", dto.getTitle());
        assertEquals("Corp", dto.getCompanyName());
        assertEquals("NYC", dto.getLocation());
        assertEquals(new BigDecimal("100000"), dto.getSalary());
        assertEquals(5, dto.getExperienceYears());
        assertEquals("FULL_TIME", dto.getJobType());
        assertEquals("Java", dto.getSkillsRequired());
        assertEquals("desc", dto.getDescription());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals(LocalDate.of(2026, 12, 31), dto.getDeadline());
    }

    @Test
    void jobResponseDTO_gettersSetters() {
        JobResponseDTO dto = new JobResponseDTO();
        dto.setId(1L);
        dto.setTitle("Dev");
        dto.setCompanyName("Corp");
        dto.setLocation("NYC");
        dto.setSalary(new BigDecimal("100000"));
        dto.setExperienceYears(5);
        dto.setJobType("FULL_TIME");
        dto.setSkillsRequired("Java");
        dto.setDescription("desc");
        dto.setStatus("ACTIVE");
        dto.setDeadline(LocalDate.of(2026, 12, 31));
        dto.setPostedBy(10L);
        LocalDateTime now = LocalDateTime.now();
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);

        assertEquals(1L, dto.getId());
        assertEquals("Dev", dto.getTitle());
        assertEquals("Corp", dto.getCompanyName());
        assertEquals("NYC", dto.getLocation());
        assertEquals(new BigDecimal("100000"), dto.getSalary());
        assertEquals(5, dto.getExperienceYears());
        assertEquals("FULL_TIME", dto.getJobType());
        assertEquals("Java", dto.getSkillsRequired());
        assertEquals("desc", dto.getDescription());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals(10L, dto.getPostedBy());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    void pagedResponse_constructorAndGetters() {
        PagedResponse<String> pr = new PagedResponse<>(List.of("a", "b"), 0, 1, 2, true);
        assertEquals(2, pr.getContent().size());
        assertEquals(0, pr.getCurrentPage());
        assertEquals(1, pr.getTotalPages());
        assertEquals(2, pr.getTotalElements());
        assertTrue(pr.isLast());
    }

    @Test
    void pagedResponse_setters() {
        PagedResponse<String> pr = new PagedResponse<>(List.of(), 0, 0, 0, false);
        pr.setContent(List.of("x"));
        pr.setCurrentPage(1);
        pr.setTotalPages(5);
        pr.setTotalElements(50);
        pr.setLast(true);

        assertEquals(1, pr.getContent().size());
        assertEquals(1, pr.getCurrentPage());
        assertEquals(5, pr.getTotalPages());
        assertEquals(50, pr.getTotalElements());
        assertTrue(pr.isLast());
    }

    @Test
    void errorResponse_constructorAndSetters() {
        ErrorResponse er = new ErrorResponse(400, "Bad", "msg");
        assertEquals(400, er.getStatus());
        assertNotNull(er.getTimestamp());

        er.setStatus(500);
        er.setError("Internal");
        er.setMessage("new");
        LocalDateTime ts = LocalDateTime.now();
        er.setTimestamp(ts);
        assertEquals(500, er.getStatus());
        assertEquals("Internal", er.getError());
        assertEquals("new", er.getMessage());
        assertEquals(ts, er.getTimestamp());
    }
}

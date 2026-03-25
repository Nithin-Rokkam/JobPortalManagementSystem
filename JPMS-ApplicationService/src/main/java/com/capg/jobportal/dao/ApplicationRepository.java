package com.capg.jobportal.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capg.jobportal.entity.Application;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByUserId(Long userId);

    List<Application> findByJobId(Long jobId);

    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    Optional<Application> findByIdAndUserId(Long id, Long userId);
}
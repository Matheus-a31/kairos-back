package com.kairos.repository;

import com.kairos.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p JOIN p.members pm WHERE pm.user.id = :userId")
    Page<Project> findProjectsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT p FROM Project p JOIN p.members pm WHERE pm.user.id = :userId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Project> findProjectsByUserIdAndName(@Param("userId") Long userId, @Param("name") String name, Pageable pageable);
}

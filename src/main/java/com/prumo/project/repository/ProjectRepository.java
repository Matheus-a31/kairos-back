package com.prumo.project.repository;
import com.prumo.auth.model.User;


import com.prumo.project.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p JOIN p.members pm WHERE pm.user.id = :userId")
    Page<Project> findProjectsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query(value = """
        SELECT p.* FROM projects p
        JOIN project_members pm ON pm.project_id = p.id
        WHERE pm.user_id = :userId
          AND fts_project_match(p.name, p.description, :name) = true
        """,
        countQuery = """
        SELECT COUNT(p.id) FROM projects p
        JOIN project_members pm ON pm.project_id = p.id
        WHERE pm.user_id = :userId
          AND fts_project_match(p.name, p.description, :name) = true
        """,
        nativeQuery = true)
    Page<Project> findProjectsByUserIdAndName(@Param("userId") Long userId, @Param("name") String name, Pageable pageable);
}



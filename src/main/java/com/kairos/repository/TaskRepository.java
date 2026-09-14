package com.kairos.repository;

import com.kairos.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    
    // Custom query to verify if a user has critical tasks in a project (used for US12 mock validation)
    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.project.id = :projectId AND t.assignee.id = :userId AND t.priority = 'URGENT' AND t.status != 'DONE'")
    boolean hasCriticalTasks(@Param("projectId") Long projectId, @Param("userId") Long userId);
}

package com.kairos.project.repository;
import com.kairos.project.model.Project;

import com.kairos.project.model.KanbanColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface KanbanColumnRepository extends JpaRepository<KanbanColumn, Long> {

    @Query("SELECT kc FROM KanbanColumn kc WHERE kc.project.id = :projectId ORDER BY kc.position ASC")
    List<KanbanColumn> findByProjectIdOrderByPosition(Long projectId);

    boolean existsByProjectIdAndId(Long projectId, Long columnId);
}



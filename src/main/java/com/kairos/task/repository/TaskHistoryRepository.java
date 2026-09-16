package com.kairos.task.repository;
import com.kairos.task.model.Task;

import com.kairos.task.model.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    List<TaskHistory> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}



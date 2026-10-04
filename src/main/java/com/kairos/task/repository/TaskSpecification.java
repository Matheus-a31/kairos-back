package com.kairos.task.repository;
import com.kairos.project.model.Project;


import com.kairos.task.model.Task;
import com.kairos.task.model.TaskPriority;
import com.kairos.task.model.TaskStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;

public class TaskSpecification {

    public static Specification<Task> buildFilters(Long projectId, String search, TaskStatus status, TaskPriority priority, Long assigneeId) {
        return (root, query, builder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            // Must belong to project
            predicates.add(builder.equal(root.get("project").get("id"), projectId));

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.toLowerCase() + "%";
                var titlePredicate = builder.like(builder.lower(root.get("title")), pattern);
                var descPredicate = builder.like(builder.lower(root.get("description")), pattern);
                predicates.add(builder.or(titlePredicate, descPredicate));
            }

            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(builder.equal(root.get("priority"), priority));
            }

            if (assigneeId != null) {
                predicates.add(builder.equal(root.get("assignee").get("id"), assigneeId));
            }

            // For fetching tags efficiently (avoid N+1) if it's not a count query
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("tags", JoinType.LEFT);
                root.fetch("assignee", JoinType.LEFT);
                query.distinct(true);
            }

            return builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}



package com.kairos.task.repository;

import com.kairos.task.model.Task;
import com.kairos.task.model.TaskPriority;
import com.kairos.task.model.TaskStatus;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskSpecificationTest {

    @Mock
    private Root<Task> root;
    
    @Mock
    private CriteriaQuery<?> query;
    
    @Mock
    private CriteriaBuilder builder;

    @Mock
    private Path<Object> projectPath;

    @Mock
    private Path<Object> projectIdPath;

    @Mock
    private Path<Object> titlePath;
    
    @Mock
    private Path<Object> descPath;
    
    @Mock
    private Path<Object> statusPath;
    
    @Mock
    private Path<Object> priorityPath;

    @Mock
    private Path<Object> assigneePath;
    
    @Mock
    private Path<Object> assigneeIdPath;

    @Mock
    private Predicate conjunctionPredicate;
    
    @Mock
    private Predicate finalPredicate;
    
    @Mock
    private Expression<String> lowerTitleExpr;
    
    @Mock
    private Expression<String> lowerDescExpr;

    @Test
    void testBuildFilters() {
        Long projectId = 1L;
        String search = "test";
        TaskStatus status = TaskStatus.TODO;
        TaskPriority priority = TaskPriority.HIGH;
        Long assigneeId = 2L;

        // Mock builder conjunction
        lenient().when(builder.conjunction()).thenReturn(conjunctionPredicate);
        lenient().when(conjunctionPredicate.getExpressions()).thenReturn(mock(java.util.List.class));

        // Mock project path
        lenient().when(root.get("project")).thenReturn(projectPath);
        lenient().when(projectPath.get("id")).thenReturn(projectIdPath);
        lenient().when(builder.equal(projectIdPath, projectId)).thenReturn(finalPredicate);

        // Mock search paths
        lenient().when(root.get("title")).thenReturn(titlePath);
        lenient().when(builder.lower(any())).thenReturn(lowerTitleExpr);
        lenient().when(builder.like(eq(lowerTitleExpr), anyString())).thenReturn(finalPredicate);

        lenient().when(root.get("description")).thenReturn(descPath);
        lenient().when(builder.lower(any())).thenReturn(lowerDescExpr);
        lenient().when(builder.like(eq(lowerDescExpr), anyString())).thenReturn(finalPredicate);

        lenient().when(builder.or(any(), any())).thenReturn(finalPredicate);

        // Mock status path
        lenient().when(root.get("status")).thenReturn(statusPath);
        lenient().when(builder.equal(statusPath, status)).thenReturn(finalPredicate);

        // Mock priority path
        lenient().when(root.get("priority")).thenReturn(priorityPath);
        lenient().when(builder.equal(priorityPath, priority)).thenReturn(finalPredicate);

        // Mock assignee path
        lenient().when(root.get("assignee")).thenReturn(assigneePath);
        lenient().when(assigneePath.get("id")).thenReturn(assigneeIdPath);
        lenient().when(builder.equal(assigneeIdPath, assigneeId)).thenReturn(finalPredicate);

        // Mock result type
        doReturn(Task.class).when(query).getResultType();

        Specification<Task> spec = TaskSpecification.buildFilters(projectId, search, status, priority, assigneeId);
        Predicate result = spec.toPredicate(root, query, builder);

        assertNotNull(result);

        verify(root).fetch("tags", JoinType.LEFT);
        verify(root).fetch("assignee", JoinType.LEFT);
    }
}

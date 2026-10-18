package com.prumo;

import com.prumo.auth.model.Role;
import com.prumo.auth.model.User;
import com.prumo.auth.repository.UserRepository;
import com.prumo.project.model.Project;
import com.prumo.project.repository.ProjectRepository;
import com.prumo.task.model.Task;
import com.prumo.task.model.TaskStatus;
import com.prumo.task.repository.TaskRepository;
import com.prumo.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class DebugIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @WithMockUser(username = "debug@example.com")
    public void testChangeStatus() {
        User user = userRepository.findByEmail("debug@example.com")
                .orElseGet(() -> userRepository.save(new User("Debug", "debug@example.com", "pass", Role.MANAGER)));

        User assignee = userRepository.findByEmail("assignee@example.com")
                .orElseGet(() -> userRepository.save(new User("Assignee", "assignee@example.com", "pass", Role.MEMBER)));

        Project project = new Project();
        project.setName("P1");
        project = projectRepository.save(project);

        Task task = new Task();
        task.setTitle("T1");
        task.setProject(project);
        task.setAssignee(assignee);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(com.prumo.task.model.TaskPriority.MEDIUM);
        task = taskRepository.save(task);

        try {
            taskService.changeStatus(project.getId(), task.getId(), TaskStatus.IN_PROGRESS, null);
            System.out.println("SUCCESS!!!");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

package com.kairos;

import com.kairos.auth.model.Role;
import com.kairos.auth.model.User;
import com.kairos.auth.repository.UserRepository;
import com.kairos.project.model.Project;
import com.kairos.project.repository.ProjectRepository;
import com.kairos.task.model.Task;
import com.kairos.task.model.TaskStatus;
import com.kairos.task.repository.TaskRepository;
import com.kairos.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
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
        User user = new User("Debug", "debug@example.com", "pass", Role.MANAGER);
        user = userRepository.save(user);

        User assignee = new User("Assignee", "assignee@example.com", "pass", Role.MEMBER);
        assignee = userRepository.save(assignee);

        Project project = new Project();
        project.setName("P1");
        project = projectRepository.save(project);

        Task task = new Task();
        task.setTitle("T1");
        task.setProject(project);
        task.setAssignee(assignee);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(com.kairos.task.model.TaskPriority.MEDIUM);
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

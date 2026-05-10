package pl.edu.prir.integral.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.prir.integral.model.BenchmarkResult;
import pl.edu.prir.integral.model.Task;
import pl.edu.prir.integral.model.TaskRequest;
import pl.edu.prir.integral.service.TaskService;

import java.util.List;
import java.util.Map;

/**
 * REST API dla zadan calkowania numerycznego.
 *
 * POST   /tasks              - Tworzy nowe zadanie (202 Accepted)
 * GET    /tasks              - Lista wszystkich zadan
 * GET    /tasks/{id}         - Szczegoly zadania
 * GET    /tasks/{id}/benchmark - Benchmark 1/2/4/8 watkow
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskRequest request) {
        try {
            Task task = taskService.submitTask(request);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                    "taskId", task.getTaskId(),
                    "status", task.getStatus().name(),
                    "method", task.getMethod().name(),
                    "createdAt", task.getCreatedAt().toString()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTask(@PathVariable String id) {
        return taskService.getTask(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Zadanie nie istnieje: " + id)));
    }

    @GetMapping("/{id}/benchmark")
    public ResponseEntity<?> runBenchmark(@PathVariable String id) {
        return taskService.runBenchmark(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Zadanie nie istnieje: " + id)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}

package pl.edu.prir.integral.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task {
    private String taskId;
    private IntegrationMethod method;
    private String function;
    private double a;
    private double b;
    private long intervals;
    private int workers;
    private TaskStatus status;
    private Double result;
    private String errorMessage;
    private Long executionTimeMs;
    private Instant createdAt;
    private Instant completedAt;

    public Task() {}

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public IntegrationMethod getMethod() { return method; }
    public void setMethod(IntegrationMethod method) { this.method = method; }

    public String getFunction() { return function; }
    public void setFunction(String function) { this.function = function; }

    public double getA() { return a; }
    public void setA(double a) { this.a = a; }

    public double getB() { return b; }
    public void setB(double b) { this.b = b; }

    public long getIntervals() { return intervals; }
    public void setIntervals(long intervals) { this.intervals = intervals; }

    public int getWorkers() { return workers; }
    public void setWorkers(int workers) { this.workers = workers; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Double getResult() { return result; }
    public void setResult(Double result) { this.result = result; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}

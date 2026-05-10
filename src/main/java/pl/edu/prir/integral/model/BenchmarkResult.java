package pl.edu.prir.integral.model;

import java.util.List;
import java.util.Map;

public class BenchmarkResult {
    private String taskId;
    private IntegrationMethod method;
    private List<WorkerResult> results;
    private Map<String, Double> speedup;
    private Map<String, Double> efficiency;

    public BenchmarkResult() {}

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public IntegrationMethod getMethod() { return method; }
    public void setMethod(IntegrationMethod method) { this.method = method; }

    public List<WorkerResult> getResults() { return results; }
    public void setResults(List<WorkerResult> results) { this.results = results; }

    public Map<String, Double> getSpeedup() { return speedup; }
    public void setSpeedup(Map<String, Double> speedup) { this.speedup = speedup; }

    public Map<String, Double> getEfficiency() { return efficiency; }
    public void setEfficiency(Map<String, Double> efficiency) { this.efficiency = efficiency; }

    public static class WorkerResult {
        private int workers;
        private double avgTimeMs;
        private double minTimeMs;
        private double maxTimeMs;
        private double result;

        public WorkerResult() {}

        public WorkerResult(int workers, double avgTimeMs, double minTimeMs,
                            double maxTimeMs, double result) {
            this.workers = workers;
            this.avgTimeMs = avgTimeMs;
            this.minTimeMs = minTimeMs;
            this.maxTimeMs = maxTimeMs;
            this.result = result;
        }

        public int getWorkers() { return workers; }
        public void setWorkers(int workers) { this.workers = workers; }

        public double getAvgTimeMs() { return avgTimeMs; }
        public void setAvgTimeMs(double avgTimeMs) { this.avgTimeMs = avgTimeMs; }

        public double getMinTimeMs() { return minTimeMs; }
        public void setMinTimeMs(double minTimeMs) { this.minTimeMs = minTimeMs; }

        public double getMaxTimeMs() { return maxTimeMs; }
        public void setMaxTimeMs(double maxTimeMs) { this.maxTimeMs = maxTimeMs; }

        public double getResult() { return result; }
        public void setResult(double result) { this.result = result; }
    }
}

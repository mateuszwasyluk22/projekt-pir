package pl.edu.prir.integral.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.edu.prir.integral.model.BenchmarkResult;
import pl.edu.prir.integral.model.Task;
import pl.edu.prir.integral.model.TaskRequest;
import pl.edu.prir.integral.model.TaskStatus;
import pl.edu.prir.integral.sequential.SequentialIntegrator;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Zarzadza cyklem zycia zadan obliczeniowych.
 *
 * Mechanizmy wspolbieznosci:
 * 1. ConcurrentHashMap      - thread-safe storage zadan
 * 2. LinkedBlockingQueue    - kolejka oczekujacych zadan
 * 3. ExecutorService        - pula watkow dispatcherow
 * 4. ForkJoinPool            - rownolegle obliczenia (w IntegrationService)
 */
@Service
public class TaskService {

    private final IntegrationService integrationService;
    private final ConcurrentHashMap<String, Task> taskStore = new ConcurrentHashMap<>();
    private BlockingQueue<String> taskQueue;
    private ExecutorService dispatcherPool;

    @Value("${integral.max-queue-size:100}")
    private int maxQueueSize;

    @Value("${integral.dispatcher-threads:2}")
    private int dispatcherThreads;

    public TaskService(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @PostConstruct
    public void init() {
        taskQueue = new LinkedBlockingQueue<>(maxQueueSize);
        dispatcherPool = Executors.newFixedThreadPool(dispatcherThreads, r -> {
            Thread t = new Thread(r, "integral-dispatcher-" + UUID.randomUUID().toString().substring(0, 6));
            t.setDaemon(true);
            return t;
        });
        for (int i = 0; i < dispatcherThreads; i++) {
            dispatcherPool.submit(this::dispatchLoop);
        }
    }

    private void dispatchLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String taskId = taskQueue.take();
                processTask(taskId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[Dispatcher] Blad: " + e.getMessage());
            }
        }
    }

    private void processTask(String taskId) {
        Task task = taskStore.get(taskId);
        if (task == null) return;

        task.setStatus(TaskStatus.RUNNING);
        long startTime = System.currentTimeMillis();

        try {
            double result = integrationService.compute(
                    task.getFunction(), task.getA(), task.getB(),
                    task.getIntervals(), task.getWorkers(), task.getMethod()
            );
            task.setResult(result);
            task.setStatus(TaskStatus.DONE);
        } catch (Exception e) {
            task.setErrorMessage("Blad obliczen: " + e.getMessage());
            task.setStatus(TaskStatus.FAILED);
        } finally {
            task.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            task.setCompletedAt(Instant.now());
        }
    }

    public Task submitTask(TaskRequest request) {
        SequentialIntegrator.validateExpression(request.getFunction());

        Task task = new Task();
        task.setTaskId(UUID.randomUUID().toString());
        task.setMethod(request.getMethod());
        task.setFunction(request.getFunction());
        task.setA(request.getA());
        task.setB(request.getB());
        task.setIntervals(request.getIntervals());
        task.setWorkers(Math.max(1, Math.min(request.getWorkers(), 16)));
        task.setStatus(TaskStatus.QUEUED);
        task.setCreatedAt(Instant.now());

        taskStore.put(task.getTaskId(), task);

        boolean added = taskQueue.offer(task.getTaskId());
        if (!added) {
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage("Kolejka zadan jest pelna.");
            task.setCompletedAt(Instant.now());
        }
        return task;
    }

    public Optional<Task> getTask(String id) {
        return Optional.ofNullable(taskStore.get(id));
    }

    public List<Task> getAllTasks() {
        return taskStore.values().stream()
                .sorted(Comparator.comparing(Task::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Benchmark - 1/2/4/8 workerow, 2 warmup + 5 pomiarow.
     */
    public Optional<BenchmarkResult> runBenchmark(String taskId) {
        Task task = taskStore.get(taskId);
        if (task == null) return Optional.empty();

        int[] workerCounts = {1, 2, 4, 8};
        int warmupRuns = 2;
        int measureRuns = 5;

        List<BenchmarkResult.WorkerResult> workerResults = new ArrayList<>();

        for (int workers : workerCounts) {
            // Warmup (JIT compilation)
            for (int i = 0; i < warmupRuns; i++) {
                integrationService.compute(task.getFunction(), task.getA(), task.getB(),
                        task.getIntervals(), workers, task.getMethod());
            }

            // Wlasciwe pomiary
            long[] times = new long[measureRuns];
            double lastResult = 0;
            for (int i = 0; i < measureRuns; i++) {
                long t0 = System.currentTimeMillis();
                lastResult = integrationService.compute(task.getFunction(), task.getA(),
                        task.getB(), task.getIntervals(), workers, task.getMethod());
                times[i] = System.currentTimeMillis() - t0;
            }

            long minT = Arrays.stream(times).min().orElse(0);
            long maxT = Arrays.stream(times).max().orElse(0);
            double avgT = Arrays.stream(times).average().orElse(0);

            workerResults.add(new BenchmarkResult.WorkerResult(workers, avgT, minT, maxT, lastResult));
        }

        // Oblicz speedup S(N) = T1/TN i efficiency E(N) = S(N)/N
        double baseTime = workerResults.get(0).getAvgTimeMs();
        Map<String, Double> speedup    = new LinkedHashMap<>();
        Map<String, Double> efficiency = new LinkedHashMap<>();

        for (BenchmarkResult.WorkerResult wr : workerResults) {
            double sp = (baseTime > 0) ? baseTime / wr.getAvgTimeMs() : 1.0;
            speedup.put(String.valueOf(wr.getWorkers()), round2(sp));
            efficiency.put(String.valueOf(wr.getWorkers()), round2(sp / wr.getWorkers()));
        }

        BenchmarkResult result = new BenchmarkResult();
        result.setTaskId(taskId);
        result.setMethod(task.getMethod());
        result.setResults(workerResults);
        result.setSpeedup(speedup);
        result.setEfficiency(efficiency);

        return Optional.of(result);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    @PreDestroy
    public void shutdown() {
        if (dispatcherPool != null) {
            dispatcherPool.shutdownNow();
        }
    }
}

package pl.edu.prir.integral.service;

import org.springframework.stereotype.Service;
import pl.edu.prir.integral.concurrent.GaussForkJoinTask;
import pl.edu.prir.integral.concurrent.RectangleForkJoinTask;
import pl.edu.prir.integral.concurrent.TrapezoidForkJoinTask;
import pl.edu.prir.integral.model.IntegrationMethod;
import pl.edu.prir.integral.sequential.SequentialIntegrator;

import java.util.concurrent.ForkJoinPool;
import java.util.function.DoubleUnaryOperator;

/**
 * Serwis obliczeniowy - calkowanie numeryczne.
 * Wybiera metode (sekwencyjna/rownolegla) na podstawie liczby workerow.
 */
@Service
public class IntegrationService {

    /**
     * Glowna metoda - automatycznie wybiera sekwencyjna lub rownolegla.
     */
    public double compute(String function, double a, double b, long n,
                           int workers, IntegrationMethod method) {
        if (workers <= 1) {
            return computeSequential(function, a, b, n, method);
        }
        return computeParallel(function, a, b, n, workers, method);
    }

    public double computeSequential(String function, double a, double b, long n,
                                      IntegrationMethod method) {
        switch (method) {
            case RECTANGLE:
                return SequentialIntegrator.rectangle(function, a, b, n);
            case TRAPEZOID:
                return SequentialIntegrator.trapezoid(function, a, b, n);
            case GAUSS:
                return SequentialIntegrator.gauss(function, a, b, n);
            default:
                throw new IllegalArgumentException("Nieznana metoda: " + method);
        }
    }

    public double computeParallel(String function, double a, double b, long n,
                                    int workers, IntegrationMethod method) {
        ForkJoinPool pool = new ForkJoinPool(workers);
        try {
            switch (method) {
                case RECTANGLE:
                    return computeParallelRectangle(function, a, b, n, pool);
                case TRAPEZOID:
                    return computeParallelTrapezoid(function, a, b, n, pool);
                case GAUSS:
                    return computeParallelGauss(function, a, b, n, pool);
                default:
                    throw new IllegalArgumentException("Nieznana metoda: " + method);
            }
        } finally {
            pool.shutdown();
        }
    }

    private double computeParallelRectangle(String function, double a, double b,
                                              long n, ForkJoinPool pool) {
        DoubleUnaryOperator f = SequentialIntegrator.buildFunction(function);
        double dx = (b - a) / n;

        RectangleForkJoinTask task = new RectangleForkJoinTask(0, n, a, dx, f);
        double sum = pool.invoke(task);
        return dx * sum;
    }

    private double computeParallelTrapezoid(String function, double a, double b,
                                              long n, ForkJoinPool pool) {
        DoubleUnaryOperator f = SequentialIntegrator.buildFunction(function);
        double dx = (b - a) / n;

        // Pierwszy i ostatni punkt maja wage 0.5
        TrapezoidForkJoinTask task = new TrapezoidForkJoinTask(0, n, a, dx, f, 0.5, 0.5);
        double sum = pool.invoke(task);
        return dx * sum;
    }

    private double computeParallelGauss(String function, double a, double b,
                                          long n, ForkJoinPool pool) {
        DoubleUnaryOperator f = SequentialIntegrator.buildFunction(function);
        double subDx = (b - a) / n;

        GaussForkJoinTask task = new GaussForkJoinTask(0, n, a, subDx, f);
        return pool.invoke(task);
    }
}

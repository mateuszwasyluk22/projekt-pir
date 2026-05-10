package pl.edu.prir.integral.concurrent;

import java.util.concurrent.RecursiveTask;
import java.util.function.DoubleUnaryOperator;

/**
 * Zadanie rekurencyjne ForkJoin do rownoleglego calkowania metoda prostokatow.
 *
 * Algorytm:
 *   - Dzielimy n podprzedzialow na dwie polowy
 *   - Lewa polowa: fork (asynchronicznie)
 *   - Prawa polowa: compute (na biezacym watku)
 *   - Laczymy wyniki (suma)
 *
 *   Wynik = dx * SUM f(a + (i+0.5)*dx) dla i = start..end-1
 *   Kazde podzadanie liczy SUM dla swojego zakresu i.
 */
public class RectangleForkJoinTask extends RecursiveTask<Double> {

    /** Ponizej tego progu liczymy sekwencyjnie (lisc) */
    private static final long THRESHOLD = 50_000L;

    private final long start;
    private final long end;
    private final double a;
    private final double dx;
    private final DoubleUnaryOperator f;

    public RectangleForkJoinTask(long start, long end, double a, double dx,
                                  DoubleUnaryOperator f) {
        this.start = start;
        this.end = end;
        this.a = a;
        this.dx = dx;
        this.f = f;
    }

    @Override
    protected Double compute() {
        long count = end - start;
        if (count <= THRESHOLD) {
            return computeSequential();
        }

        long mid = start + count / 2;

        RectangleForkJoinTask left  = new RectangleForkJoinTask(start, mid, a, dx, f);
        RectangleForkJoinTask right = new RectangleForkJoinTask(mid, end, a, dx, f);

        left.fork();
        double rightSum = right.compute();
        double leftSum  = left.join();

        return leftSum + rightSum;
    }

    private double computeSequential() {
        double sum = 0.0;
        for (long i = start; i < end; i++) {
            double x = a + (i + 0.5) * dx;
            sum += f.applyAsDouble(x);
        }
        return sum;
    }
}

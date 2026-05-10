package pl.edu.prir.integral.concurrent;

import pl.edu.prir.integral.sequential.GaussWeights;

import java.util.concurrent.RecursiveTask;
import java.util.function.DoubleUnaryOperator;

/**
 * Zadanie rekurencyjne ForkJoin do rownoleglego calkowania kwadratura Gaussa-Legendre.
 *
 * Algorytm (composite Gauss 3-point):
 *   [a, b] dzielimy na K subprzedzialow
 *   Kazdy subprzedzial [a_k, b_k] = [a + k*subDx, a + (k+1)*subDx]
 *     x_kj = (subDx/2) * t_j + (a_k + b_k)/2   (transformacja z [-1,1])
 *     wynik_k = (subDx/2) * SUM(w_j * f(x_kj))
 *   Wynik calkowity = SUM wynik_k
 *
 * Rownolegly podzial:
 *   - Dzielimy liste K subprzedzialow na polowy (jak merge sort)
 *   - Kazdy watek liczy Gaussa na swoich subprzedzialach
 *   - ForkJoin laczy sume
 */
public class GaussForkJoinTask extends RecursiveTask<Double> {

    private static final long THRESHOLD = 10_000L;

    private final long startK;
    private final long endK;
    private final double a;
    private final double subDx;
    private final DoubleUnaryOperator f;

    /**
     * @param startK  indeks pierwszego subprzedzialu (wlacznie)
     * @param endK    indeks ostatniego subprzedzialu (wykluczajaco)
     * @param a       dolna granica calkowania
     * @param subDx   szerokosc pojedynczego subprzedzialu = (b-a)/K
     * @param f       funkcja podcalkowa
     */
    public GaussForkJoinTask(long startK, long endK, double a, double subDx,
                              DoubleUnaryOperator f) {
        this.startK = startK;
        this.endK = endK;
        this.a = a;
        this.subDx = subDx;
        this.f = f;
    }

    @Override
    protected Double compute() {
        long count = endK - startK;
        if (count <= THRESHOLD) {
            return computeSequential();
        }

        long mid = startK + count / 2;

        GaussForkJoinTask left  = new GaussForkJoinTask(startK, mid, a, subDx, f);
        GaussForkJoinTask right = new GaussForkJoinTask(mid, endK, a, subDx, f);

        left.fork();
        double rightSum = right.compute();
        double leftSum  = left.join();

        return leftSum + rightSum;
    }

    private double computeSequential() {
        double[] nodes   = GaussWeights.NODES_3;
        double[] weights = GaussWeights.WEIGHTS_3;
        double sum = 0.0;
        double halfSubDx = subDx / 2.0;

        for (long k = startK; k < endK; k++) {
            double ak  = a + k * subDx;
            double mid = ak + halfSubDx;

            for (int j = 0; j < nodes.length; j++) {
                double x = halfSubDx * nodes[j] + mid;
                sum += weights[j] * f.applyAsDouble(x);
            }
        }

        // kazdy subprzedzial ma factor (subDx/2) - mnozymy sume przez niego
        return (subDx / 2.0) * sum;
    }
}

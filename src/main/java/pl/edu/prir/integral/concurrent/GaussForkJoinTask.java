package pl.edu.prir.integral.concurrent;

import pl.edu.prir.integral.sequential.GaussWeights;

import java.util.concurrent.RecursiveTask;
import java.util.function.DoubleUnaryOperator;

public class GaussForkJoinTask extends RecursiveTask<Double> {

    private static final long THRESHOLD = 10_000L;

    private final long startK;
    private final long endK;
    private final double a;
    private final double subDx;
    private final DoubleUnaryOperator f;


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

        return (subDx / 2.0) * sum;
    }
}

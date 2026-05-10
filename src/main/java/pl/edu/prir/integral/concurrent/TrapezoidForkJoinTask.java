package pl.edu.prir.integral.concurrent;

import java.util.concurrent.RecursiveTask;
import java.util.function.DoubleUnaryOperator;

/**
 * Zadanie rekurencyjne ForkJoin do rownoleglego calkowania metoda trapezow.
 *
 * Algorytm (composite trapezoidal rule):
 *   sum = f(a)/2 + f(x_1) + f(x_2) + ... + f(x_{n-1}) + f(b)/2
 *   wynik = dx * sum
 *
 * Rownolegly podzial:
 *   - Kazde podzadanie liczy sume czastkowa dla zakresu [start, end)
 *   - Brzegowe punkty (i=0, i=n) maja wage 0.5, reszta 1.0
 *   - ForkJoin dzieli zakres na dwie polowy
 *
 * Uwaga: punkt startowy i endowy moga byc brzegowe (waga 0.5).
 * Podzial rekurencyjny: punkt mid dostaje wage 0.5 w obu podzadaniach
 * (suma 0.5+0.5=1.0 = poprawna waga wewnetrznego punktu).
 */
public class TrapezoidForkJoinTask extends RecursiveTask<Double> {

    private static final long THRESHOLD = 50_000L;

    private final long start;
    private final long end;
    private final double a;
    private final double dx;
    private final DoubleUnaryOperator f;
    private final double startWeight;
    private final double endWeight;

    /**
     * @param start        indeks pierwszego podprzedzialu (wlacznie)
     * @param end          indeks ostatniego podprzedzialu (wykluczajaco)
     * @param a            dolna granica calkowania
     * @param dx           szerokosc podprzedzialu
     * @param f            funkcja podcalkowa
     * @param startWeight  waga punktu start (0.5 dla brzegu, 1.0 wewnatrz)
     * @param endWeight    waga punktu end (0.5 dla brzegu, 1.0 wewnatrz)
     */
    public TrapezoidForkJoinTask(long start, long end, double a, double dx,
                                  DoubleUnaryOperator f, double startWeight, double endWeight) {
        this.start = start;
        this.end = end;
        this.a = a;
        this.dx = dx;
        this.f = f;
        this.startWeight = startWeight;
        this.endWeight = endWeight;
    }

    @Override
    protected Double compute() {
        long count = end - start;
        if (count <= THRESHOLD) {
            return computeSequential();
        }

        long mid = start + count / 2;

        // Punkt mid jest wspoldzielony: lewa ma endWeight=0.5, prawa ma startWeight=0.5
        // Razem daja wage 1.0 (poprawne)
        TrapezoidForkJoinTask left  = new TrapezoidForkJoinTask(start, mid, a, dx, f,
                startWeight, 0.5);
        TrapezoidForkJoinTask right = new TrapezoidForkJoinTask(mid, end, a, dx, f,
                0.5, endWeight);

        left.fork();
        double rightSum = right.compute();
        double leftSum  = left.join();

        return leftSum + rightSum;
    }

    private double computeSequential() {
        double sum = 0.0;

        // Pierwszy punkt z ewentualna waga 0.5
        sum += startWeight * f.applyAsDouble(a + start * dx);

        // Srodkowe punkty z waga 1.0
        for (long i = start + 1; i < end; i++) {
            double x = a + i * dx;
            sum += f.applyAsDouble(x);
        }

        // Ostatni punkt z ewentualna waga 0.5 (tylko jesli end > start)
        if (end > start) {
            sum += endWeight * f.applyAsDouble(a + end * dx);
        }

        return sum;
    }
}

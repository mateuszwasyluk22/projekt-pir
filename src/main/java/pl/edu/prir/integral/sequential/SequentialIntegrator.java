package pl.edu.prir.integral.sequential;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.function.DoubleUnaryOperator;

/**
 * Sekwencyjna (baseline) wersja calkowania numerycznego.
 * Obsluguje 3 metody: prostokaty (midpoint), trapezy, Gauss-Legendre 3-punktowa.
 */
public class SequentialIntegrator {

    /**
     * Buduje funkcje matematyczna z wyrazenia tekstowego za pomoca exp4j.
     */
    public static DoubleUnaryOperator buildFunction(String expression) {
        try {
            Expression expr = new ExpressionBuilder(expression)
                    .variable("x")
                    .build();
            return x -> {
                expr.setVariable("x", x);
                return expr.evaluate();
            };
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Nieprawidlowe wyrazenie: '" + expression + "'. " + e.getMessage(), e);
        }
    }

    /**
     * Waliduje wyrazenie - wyrzuca wyjatek jesli jest bledne.
     */
    public static void validateExpression(String expression) {
        buildFunction(expression).applyAsDouble(1.0);
    }

    /**
     * Metoda prostokatow (midpoint rule).
     * f(x_i) obliczane w srodku kazdego podprzedzialu.
     * integral ~ dx * SUM f(a + (i+0.5)*dx)
     */
    public static double rectangle(String function, double a, double b, long n) {
        DoubleUnaryOperator f = buildFunction(function);
        double dx = (b - a) / n;
        double sum = 0.0;

        for (long i = 0; i < n; i++) {
            double x = a + (i + 0.5) * dx;
            sum += f.applyAsDouble(x);
        }
        return dx * sum;
    }

    /**
     * Metoda trapezow (composite trapezoidal rule).
     * integral ~ dx * [f(a)/2 + f(x_1) + ... + f(x_{n-1}) + f(b)/2]
     */
    public static double trapezoid(String function, double a, double b, long n) {
        DoubleUnaryOperator f = buildFunction(function);
        double dx = (b - a) / n;

        double sum = 0.5 * f.applyAsDouble(a) + 0.5 * f.applyAsDouble(b);

        for (long i = 1; i < n; i++) {
            double x = a + i * dx;
            sum += f.applyAsDouble(x);
        }
        return dx * sum;
    }

    /**
     * Kwadratura Gaussa-Legendre 3-punktowa (composite).
     * [a,b] dzielimy na n subprzedzialow i na kazdym stosujemy 3-punktowa kwadrature.
     *
     * Dla kazdego subprzedzialu [a_k, b_k]:
     *   x_kj = (b_k - a_k)/2 * t_j + (a_k + b_k)/2
     *   wynik_k = (b_k - a_k)/2 * SUM(w_j * f(x_kj))
     */
    public static double gauss(String function, double a, double b, long n) {
        DoubleUnaryOperator f = buildFunction(function);
        double[] nodes = GaussWeights.NODES_3;
        double[] weights = GaussWeights.WEIGHTS_3;

        double subDx = (b - a) / n;
        double sum = 0.0;

        for (long k = 0; k < n; k++) {
            double ak = a + k * subDx;
            double bk = a + (k + 1) * subDx;
            double halfLen = (bk - ak) / 2.0;
            double mid = (ak + bk) / 2.0;

            for (int j = 0; j < nodes.length; j++) {
                double x = halfLen * nodes[j] + mid;
                sum += weights[j] * f.applyAsDouble(x);
            }
        }
        return (subDx / 2.0) * sum;
    }
}

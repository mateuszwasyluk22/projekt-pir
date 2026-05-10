package pl.edu.prir.integral.sequential;

public final class GaussWeights {

    /** Wezly 3-punktowej kwadratury Gaussa-Legendre na [-1, 1] */
    public static final double[] NODES_3 = {
        -Math.sqrt(3.0 / 5.0),
        0.0,
        Math.sqrt(3.0 / 5.0)
    };

    /** Wagi 3-punktowej kwadratury Gaussa-Legendre */
    public static final double[] WEIGHTS_3 = {
        5.0 / 9.0,
        8.0 / 9.0,
        5.0 / 9.0
    };

    /** Wezly 5-punktowej kwadratury Gaussa-Legendre na [-1, 1] */
    public static final double[] NODES_5 = {
        -Math.sqrt(5.0 + 2.0 * Math.sqrt(10.0 / 7.0)) / 3.0,
        -Math.sqrt(5.0 - 2.0 * Math.sqrt(10.0 / 7.0)) / 3.0,
        0.0,
        Math.sqrt(5.0 - 2.0 * Math.sqrt(10.0 / 7.0)) / 3.0,
        Math.sqrt(5.0 + 2.0 * Math.sqrt(10.0 / 7.0)) / 3.0
    };

    /** Wagi 5-punktowej kwadratury Gaussa-Legendre */
    public static final double[] WEIGHTS_5 = {
        (322.0 - 13.0 * Math.sqrt(70.0)) / 900.0,
        (322.0 + 13.0 * Math.sqrt(70.0)) / 900.0,
        128.0 / 225.0,
        (322.0 + 13.0 * Math.sqrt(70.0)) / 900.0,
        (322.0 - 13.0 * Math.sqrt(70.0)) / 900.0
    };

    private GaussWeights() {}
}

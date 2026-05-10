package pl.edu.prir.integral.model;

public enum IntegrationMethod {
    RECTANGLE("Metoda prostokatow"),
    TRAPEZOID("Metoda trapezow"),
    GAUSS("Kwadratura Gaussa-Legendre");

    private final String description;

    IntegrationMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

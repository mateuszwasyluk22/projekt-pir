package pl.edu.prir.integral.model;

public class TaskRequest {
    private IntegrationMethod method = IntegrationMethod.TRAPEZOID;
    private String function = "sin(x^2)";
    private double a = 0.0;
    private double b = 1.0;
    private long intervals = 1_000_000L;
    private int workers = 4;

    public TaskRequest() {}

    public IntegrationMethod getMethod() { return method; }
    public void setMethod(IntegrationMethod method) { this.method = method; }

    public String getFunction() { return function; }
    public void setFunction(String function) { this.function = function; }

    public double getA() { return a; }
    public void setA(double a) { this.a = a; }

    public double getB() { return b; }
    public void setB(double b) { this.b = b; }

    public long getIntervals() { return intervals; }
    public void setIntervals(long intervals) { this.intervals = intervals; }

    public int getWorkers() { return workers; }
    public void setWorkers(int workers) { this.workers = workers; }
}

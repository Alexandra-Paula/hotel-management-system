package strategy;

public interface PaymentStrategy {
    void pay(double amount);
    String getMethodName();
}
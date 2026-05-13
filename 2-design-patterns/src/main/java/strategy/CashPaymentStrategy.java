package strategy;

public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("Cash: Payment of €" +
                String.format("%.2f", amount) + " received. ✔");
    }

    @Override
    public String getMethodName() {
        return "Cash";
    }
}
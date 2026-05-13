package payment;

public class StripePaymentService {
    public void makeStripePayment(double amount) {
        System.out.println("Processing payment of €" + String.format("%.2f", amount));
        System.out.println("Payment successful!");
    }
}
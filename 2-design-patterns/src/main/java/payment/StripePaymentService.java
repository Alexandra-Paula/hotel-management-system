package payment;

public class StripePaymentService {
    public void makeStripePayment(double amount) {
        System.out.println("Stripe: Processing payment of €" + String.format("%.2f", amount));
        System.out.println("Stripe: Payment successful!");
    }
}
package payment;

public class StripeAdapter implements PaymentProcessor {
    private StripePaymentService stripe;

    public StripeAdapter(StripePaymentService stripe) {
        this.stripe = stripe;
    }

    @Override
    public void processPayment(double amount) {
        stripe.makeStripePayment(amount);
    }
}
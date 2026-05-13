package strategy;

import payment.StripeAdapter;
import payment.StripePaymentService;

public class StripePaymentStrategy implements PaymentStrategy {
    private final StripeAdapter adapter;

    public StripePaymentStrategy() {
        this.adapter = new StripeAdapter(new StripePaymentService());
    }

    @Override
    public void pay(double amount) {
        adapter.processPayment(amount);
    }

    @Override
    public String getMethodName() {
        return "Card (Stripe)";
    }
}
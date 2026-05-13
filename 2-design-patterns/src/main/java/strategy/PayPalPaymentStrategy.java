package strategy;

import payment.PayPalAdapter;
import payment.PayPalPaymentService;

public class PayPalPaymentStrategy implements PaymentStrategy {
    private final PayPalAdapter adapter;

    public PayPalPaymentStrategy() {
        this.adapter = new PayPalAdapter(new PayPalPaymentService());
    }

    @Override
    public void pay(double amount) {
        adapter.processPayment(amount);
    }

    @Override
    public String getMethodName() {
        return "PayPal";
    }
}
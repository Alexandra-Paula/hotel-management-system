package payment;

public class PayPalAdapter implements PaymentProcessor {
    private PayPalPaymentService paypal;

    public PayPalAdapter(PayPalPaymentService paypal) {
        this.paypal = paypal;
    }

    @Override
    public void processPayment(double amount) {
        paypal.sendPayPalPayment(amount);
    }
}
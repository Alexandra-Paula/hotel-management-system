package payment;

public class PayPalPaymentService {
    public void sendPayPalPayment(double amount) {
        System.out.println("PayPal: Sending payment of €" + String.format("%.2f", amount));
        System.out.println("PayPal: Payment confirmed! ✔");
    }
}
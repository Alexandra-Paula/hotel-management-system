package composite;

public class SingleService implements ServiceComponent {
    private String description;
    private double price;

    public SingleService(String description, double price) {
        this.description = description;
        this.price = price;
    }

    @Override
    public String getDescription() { return description; }

    @Override
    public double getPrice() { return price; }

    @Override
    public void display() {
        System.out.println("  • " + description + " - €" + price);
    }
}
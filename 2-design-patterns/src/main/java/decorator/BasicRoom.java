package decorator;

public class BasicRoom implements RoomComponent {
    private String description;
    private double price;

    public BasicRoom(String description, double price) {
        this.description = description;
        this.price = price;
    }

    @Override
    public String getDescription() { return description; }

    @Override
    public double getPrice() { return price; }
}
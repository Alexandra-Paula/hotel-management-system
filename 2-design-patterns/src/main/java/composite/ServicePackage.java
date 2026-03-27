package composite;

import java.util.ArrayList;
import java.util.List;

public class ServicePackage implements ServiceComponent {
    private String packageName;
    private List<ServiceComponent> components = new ArrayList<>();

    public ServicePackage(String packageName) {
        this.packageName = packageName;
    }

    public void add(ServiceComponent component) {
        components.add(component);
    }

    @Override
    public String getDescription() { return packageName; }

    @Override
    public double getPrice() {
        double total = 0;
        for (ServiceComponent c : components) {
            total += c.getPrice();
        }
        return total;
    }

    @Override
    public void display() {
        System.out.println("Package: " + packageName + " (Total: €" + getPrice() + ")");
        for (ServiceComponent c : components) {
            c.display();
        }
    }
}
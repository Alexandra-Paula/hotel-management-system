package models;

public class SpaAccess extends ExtraService {
    public SpaAccess() {
        description = "Spa Access";
        price = 40;
    }
    @Override
    public ExtraService clone(){
        return new SpaAccess();
    }
}

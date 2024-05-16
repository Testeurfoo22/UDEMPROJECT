public interface AbstractLocalisation {
    void make(String id, String name, String city, TravelType type);
    void updateName(String newName);
    void updateCity(String newCity);
    String getId();
    String getName();
    String getCity();
    TravelType getType();
    String accept(LocalisationVisitor visitor);
}
abstract class BaseLocalisation implements AbstractLocalisation {
    protected String id;
    protected String name;
    protected String city;
    protected TravelType type;

    @Override
    public void make(String id, String name, String city, TravelType type) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.type = type;
    }
    @Override
    public void updateName(String newName) {
        this.name = newName;
    }
    @Override
    public void updateCity(String newCity) {
        this.city = newCity;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getCity() {
        return city;
    }
    public TravelType getType() {
        return type;
    }

    public abstract String accept(LocalisationVisitor visitor);
}

class Airport extends BaseLocalisation {
    public Airport(String id, String name, String city, TravelType type) {
        super.make(id, name, city, type);
    }
    @Override
    public String accept(LocalisationVisitor visitor) {
        return visitor.visit(this);
    }
}
class Port extends BaseLocalisation {
    public Port(String id, String name, String city, TravelType type) {
        super.make(id, name, city, type);
    }
    @Override
    public String accept(LocalisationVisitor visitor) {
        return visitor.visit(this);
    }
}
class Station extends BaseLocalisation {
    public Station(String id, String name, String city, TravelType type) {
        super.make(id, name, city, type);
    }
    @Override
    public String accept(LocalisationVisitor visitor) {
        return visitor.visit(this);
    }
}

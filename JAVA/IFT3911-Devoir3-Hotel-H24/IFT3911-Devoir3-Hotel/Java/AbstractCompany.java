public interface AbstractCompany {
    void make(String id, String iata, String name, TravelType type);
    void updateName(String newName);
    String getId();
    String getIata();
    String getName();
    TravelType getType();
    String accept(CompanyVisitor visitor);
}
abstract class BaseCompany implements AbstractCompany {
    protected String id;
    protected String iata;
    protected String name;
    protected TravelType type;

    @Override
    public void make(String id, String iata, String name, TravelType type) {
        this.id = id;
        this.iata = iata;
        this.name = name;
        this.type = type;
    }
    @Override
    public void updateName(String newName) {
        this.name = newName;
    }
    public String getId() {
        return id;
    }
    public String getIata() {
        return iata;
    }
    public String getName() {
        return name;
    }
    public TravelType getType() {
        return type;
    }

    public abstract String accept(CompanyVisitor visitor);
}

class AirCompany extends BaseCompany {
    public AirCompany(String id, String iata, String name, TravelType type) {
        super.make(id, iata, name, type);
    }
    @Override
    public String accept(CompanyVisitor visitor) {
        return visitor.visit(this);
    }
}
class CruiseCompany extends BaseCompany {
    public CruiseCompany(String id, String iata, String name, TravelType type) {
        super.make(id, iata, name, type);
    }
    @Override
    public String accept(CompanyVisitor visitor) {
        return visitor.visit(this);
    }
}
class TrainCompany extends BaseCompany {
    public TrainCompany(String id, String iata, String name, TravelType type) {
        super.make(id, iata, name, type);
    }
    @Override
    public String accept(CompanyVisitor visitor) {
        return visitor.visit(this);
    }
}
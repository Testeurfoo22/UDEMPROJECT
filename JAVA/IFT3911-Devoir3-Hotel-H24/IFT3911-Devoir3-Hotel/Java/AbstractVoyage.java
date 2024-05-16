import java.util.Calendar;
import java.util.List;

public interface AbstractVoyage {
    void make(String id, Integer price, AbstractCompany company, String vehicule,
              List<? extends AbstractLocalisation> localisation, List<Calendar> date, List<List<Seat>> seats, TravelType type);
    void updatePrice(Integer newPrice);
    void updateCompany(AbstractCompany newCompany);
    void updateVehicule(String newVehicule);
    void updateLocalisation(List<? extends AbstractLocalisation> newLocalisation);
    void updateDate(List<Calendar> newDate);
    String getId();
    Integer getPrice();
    AbstractCompany getCompany();
    String getVehicule();
    List<? extends AbstractLocalisation> getLocalisations();
    List<Calendar> getDates();
    List<List<Seat>> getSeats();
    TravelType getType();
    String accept(VoyageVisitor visitor, section section);
}
abstract class BaseVoyage implements AbstractVoyage {
    private String id;
    private Integer price;
    private AbstractCompany company;
    private String vehicule;
    private List<? extends AbstractLocalisation> localisation;
    private List<Calendar> date;
    private List<List<Seat>> seats;
    private TravelType type;

    @Override
    public void make(String id, Integer price, AbstractCompany company, String vehicule,
                     List<? extends AbstractLocalisation> localisation, List<Calendar> date, List<List<Seat>> seats, TravelType type) {
        this.id = id;
        this.price = price;
        this.company = company;
        this.vehicule = vehicule;
        this.localisation = localisation;
        this.date = date;
        this.seats = seats;
        this.type = type;
    }
    @Override
    public void updatePrice(Integer newPrice) {
        this.price = newPrice;
    }
    @Override
    public void updateCompany(AbstractCompany newCompany) {
        this.company = newCompany;
    }
    @Override
    public void updateVehicule(String newVehicule) {
        this.vehicule = newVehicule;
    }
    @Override
    public void updateLocalisation(List<? extends AbstractLocalisation> newLocalisation) {
        this.localisation = newLocalisation;
    }
    @Override
    public void updateDate(List<Calendar> newDate) {
        this.date = newDate;
    }
    public String getId(){
        return id;
    };
    public Integer getPrice(){
        return price;
    };
    public AbstractCompany getCompany(){
        return company;
    };
    public String getVehicule(){
        return vehicule;
    };
    public List<? extends AbstractLocalisation> getLocalisations(){
        return localisation;
    };
    public List<Calendar> getDates(){
        return date;
    };
    public List<List<Seat>> getSeats(){
        return seats;
    };
    public TravelType getType(){
        return type;
    };

    public abstract String accept(VoyageVisitor visitor, section section);
}

class Flight extends BaseVoyage {
    public Flight(String id, Integer price, AbstractCompany company, String vehicule,
                  List<? extends AbstractLocalisation> localisation, List<Calendar> date, List<List<Seat>> seats, TravelType type) {
        super.make(id, price, company, vehicule, localisation, date, seats, type);
    }
    @Override
    public String accept(VoyageVisitor visitor,section section) {
        return visitor.visit(this, section);
    }
}
class Cruise extends BaseVoyage {
    public Cruise(String id, Integer price, AbstractCompany company, String vehicule,
                  List<? extends AbstractLocalisation> localisation, List<Calendar> date, List<List<Seat>> seats, TravelType type) {
        super.make(id, price, company, vehicule, localisation, date, seats, type);
    }
    @Override
    public String accept(VoyageVisitor visitor,section section) {
        return visitor.visit(this, section);
    }
}
class Train extends BaseVoyage {
    public Train(String id, Integer price, AbstractCompany company, String vehicule,
                 List<? extends AbstractLocalisation> localisation, List<Calendar> date, List<List<Seat>> seats, TravelType type) {
        super.make(id, price, company, vehicule, localisation, date, seats, type);
    }
    @Override
    public String accept(VoyageVisitor visitor,section section) {
        return visitor.visit(this, section);
    }
}

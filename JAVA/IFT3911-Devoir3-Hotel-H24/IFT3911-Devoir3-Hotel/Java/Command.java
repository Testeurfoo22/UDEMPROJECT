import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public interface Command {
    void execute();
    void undo();
}

class AddLocalisationCommand implements Command {
    private ConcreteLocalisationFactory factory;
    private TravelType type;
    private String id;
    private String name;
    private String city;

    public AddLocalisationCommand(ConcreteLocalisationFactory factory, TravelType type, String id, String name, String city) {
        this.factory = factory;
        this.type = type;
        this.id = id;
        this.name = name;
        this.city = city;
    }

    @Override
    public void execute() {
        factory.getModel().addLocalisation(factory, type, id, name, city);
    }
    @Override
    public void undo() {
        factory.getModel().deleteLocalisation(id);
    }
}
class UpdateLocalisationCommand implements Command {
    private ConcreteLocalisationFactory factory;
    private String id;
    private String newName;
    private String newCity;
    private String oldName;
    private String oldCity;

    public UpdateLocalisationCommand(ConcreteLocalisationFactory factory, String id, String newName, String newCity) {
        this.factory = factory;
        this.id = id;
        this.newName = newName;
        this.newCity = newCity;
        AbstractLocalisation loc = factory.getModel().getLocalisationById(id);
        if (loc != null) {
            this.oldName = loc.getName();
            this.oldCity = loc.getCity();
        }
    }

    @Override
    public void execute() {
        factory.getModel().updateLocalisation(factory, id, newName, newCity);
    }
    @Override
    public void undo() {
        factory.getModel().updateLocalisation(factory, id, oldName, oldCity);
    }
}
class DeleteLocalisationCommand implements Command {
    private ConcreteLocalisationFactory factory;
    private String id;
    private TravelType type;
    private String name;
    private String city;

    public DeleteLocalisationCommand(ConcreteLocalisationFactory factory, String id) {
        this.factory = factory;
        this.id = id;
        AbstractLocalisation loc = factory.getModel().getLocalisationById(id);
        if (loc != null) {
            this.type = loc.getType();
            this.name = loc.getName();
            this.city = loc.getCity();
        }
    }

    @Override
    public void execute() {
        factory.getModel().deleteLocalisation(id);
    }
    @Override
    public void undo() {
        factory.getModel().addLocalisation(factory, type, id, name, city);
    }
}

class AddCompanyCommand implements Command {
    private ConcreteCompanyFactory factory;
    private TravelType type;
    private String id;
    private String iata;
    private String name;

    public AddCompanyCommand(ConcreteCompanyFactory factory, TravelType type, String id, String iata, String name) {
        this.factory = factory;
        this.type = type;
        this.id = id;
        this.iata = iata;
        this.name = name;
    }

    @Override
    public void execute() {
        factory.getModel().addCompany(factory, type, id, iata, name);
    }
    @Override
    public void undo() {
        factory.getModel().deleteCompany(id);
    }
}
class UpdateCompanyCommand implements Command {
    private ConcreteCompanyFactory factory;
    private String id;
    private String newName;
    private String oldName;

    public UpdateCompanyCommand(ConcreteCompanyFactory factory, String id, String newName) {
        this.factory = factory;
        this.id = id;
        this.newName = newName;
        AbstractCompany com = factory.getModel().getCompanyById(id);
        if (com != null) {
            this.oldName = com.getName();
        }
    }

    @Override
    public void execute() {
        factory.getModel().updateLocalisation(factory, id, newName);
    }
    @Override
    public void undo() {
        factory.getModel().updateLocalisation(factory, id, oldName);
    }
}
class DeleteCompanyCommand implements Command {
    private ConcreteCompanyFactory factory;
    private String id;
    private TravelType type;
    private String iata;
    private String name;

    public DeleteCompanyCommand(ConcreteCompanyFactory factory, String id) {
        this.factory = factory;
        this.id = id;
        AbstractCompany loc = factory.getModel().getCompanyById(id);
        if (loc != null) {
            this.type = loc.getType();
            this.iata = loc.getIata();
            this.name = loc.getName();
        }
    }

    @Override
    public void execute() {
        factory.getModel().deleteCompany(id);
    }
    @Override
    public void undo() {
        factory.getModel().addCompany(factory, type, id, iata, name);
    }
}

class AddVoyageCommand implements Command {
    private ConcreteVoyageFactory factory;
    private TravelType type;
    private String id;
    private Integer price;
    private AbstractCompany company;
    private String vehicule;
    private List<? extends AbstractLocalisation> localisation;
    private List<Calendar> date;
    private List<List<Seat>> seats;

    public AddVoyageCommand(ConcreteVoyageFactory factory, TravelType type, String id, Integer price, AbstractCompany company, String vehicule,
                            List<? extends AbstractLocalisation> localisation, List<Calendar> date, List<List<Seat>> seats) {
        this.factory = factory;
        this.type = type;
        this.id = id;
        this.price = price;
        this.company = company;
        this.vehicule = vehicule;
        this.localisation = localisation;
        this.date = date;
        this.seats = seats;
    }

    @Override
    public void execute() {
        factory.getModel().addVoyage(factory, type, id, price, company, vehicule, localisation, date, seats);
    }
    @Override
    public void undo() {
        factory.getModel().deleteVoyage(id);
    }
}
class UpdateVoyageCommand implements Command {
    private ConcreteVoyageFactory factory;
    private String id;
    private Integer newPrice;
    private Integer oldPrice;
    private String newVehicule;
    private String oldVehicule;
    private List<? extends AbstractLocalisation> newLocalisation;
    private List<? extends AbstractLocalisation> oldLocalisation;
    private List<Calendar> newDate;
    private List<Calendar> oldDate;

    public UpdateVoyageCommand(ConcreteVoyageFactory factory, String id, Integer newPrice,
                                String newVehicule, List<? extends AbstractLocalisation> newLocalisation, List<Calendar> newDate) {
        this.factory = factory;
        this.id = id;
        this.newPrice = newPrice;
        this.newVehicule = newVehicule;
        this.newLocalisation = newLocalisation;
        this.newDate = newDate;
        AbstractVoyage com = factory.getModel().getVoyagesById(id);
        if (com != null) {
            this.oldPrice = com.getPrice();
            this.oldVehicule = com.getVehicule();
            this.oldLocalisation = com.getLocalisations();
            this.oldDate = com.getDates();
        }
    }

    @Override
    public void execute() {
        factory.getModel().updateVoyage(factory, id, newPrice, newVehicule, newLocalisation, newDate);
    }
    @Override
    public void undo() {
        factory.getModel().updateVoyage(factory, id, oldPrice, oldVehicule, oldLocalisation, oldDate);;
    }
}
class DeleteVoyageCommand implements Command {
    private ConcreteVoyageFactory factory;
    private TravelType type;
    private String id;
    private Integer price;
    private AbstractCompany company;
    private String vehicule;
    private List<? extends AbstractLocalisation> localisation;
    private List<Calendar> date;
    private List<List<Seat>> seats;

    public DeleteVoyageCommand(ConcreteVoyageFactory factory, String id) {
        this.factory = factory;
        this.id = id;
        AbstractVoyage com = factory.getModel().getVoyagesById(id);
        if (com != null) {
            this.type = com.getType();
            this.price = com.getPrice();
            this.company = com.getCompany();
            this.vehicule = com.getVehicule();
            this.localisation = com.getLocalisations();
            this.date = com.getDates();
            this.seats = com.getSeats();
        }
    }

    @Override
    public void execute() {
        factory.getModel().deleteVoyage(id);
    }
    @Override
    public void undo() {
        factory.getModel().addVoyage(factory, type, id, price, company, vehicule, localisation, date, seats);
    }
}
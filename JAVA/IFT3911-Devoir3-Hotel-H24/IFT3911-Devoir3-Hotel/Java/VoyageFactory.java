import java.util.Calendar;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public interface VoyageFactory {
    AbstractVoyage createVoyage(TravelType type, String id, Integer price, AbstractCompany Company, String vehicule,
                                List<? extends AbstractLocalisation> localisation, List<Calendar> date, List<List<Seat>> seats);
}

class ConcreteVoyageFactory implements VoyageFactory {
    private static ConcreteVoyageFactory instance;
    private VoyageModel model;

    private ConcreteVoyageFactory(VoyageModel model) {this.model = model;}
    public static synchronized ConcreteVoyageFactory getInstance(VoyageModel model) {
        if (instance == null) {
            if (model == null) {
                throw new IllegalArgumentException("Model cannot be null on the first call to getInstance.");
            }
            instance = new ConcreteVoyageFactory(model);
        }
        return instance;
    }
    public synchronized VoyageModel getModel(){
        return model;
    }

    @Override
    public AbstractVoyage createVoyage(TravelType type, String id, Integer price, AbstractCompany company, String vehicule,
                                       List<? extends AbstractLocalisation> localisation, List<Calendar> date, List<List<Seat>> seats) {
        AbstractVoyage voyage = model.getVoyagesById(id);
        if (!company.getIata().equals(id.substring(0, 2))){
            throw new IllegalArgumentException("Error: No IATA link with ID: " + id.substring(0, 2) + " and Company " + company.getIata());
        }
        if (voyage != null && voyage.getType() == type) {
            throw new IllegalArgumentException("Error: Voyage already created with ID: " + id);
        }
        if (price <= 0){
            throw new IllegalArgumentException("Error: Parse Voyage, Price Error no Price <= 0 :");
        }
        if (date.get(0).after(date.get(1))){
            throw new IllegalArgumentException("Error: Voyage, Parse date: " + date.get(0) + " / " + date.get(1));
        }
        for (AbstractVoyage voyageV: model.getVoyages()){
            List<Calendar> VDate = voyageV.getDates();
            if (voyageV.getVehicule().equals(vehicule) && ((!date.get(0).before(VDate.get(0)) && !date.get(0).after(VDate.get(1)))
                                                            ||(!date.get(1).before(VDate.get(0)) && !date.get(1).after(VDate.get(1))))){
                throw new IllegalArgumentException("Error: Voyage, Vehicule already use on those date: ");
            }
        }
        voyage = switch (type) {
            case FlightType:
                if (localisation.size() != 2 || localisation.get(0).getId().equals(localisation.get(1).getId())){
                    throw new IllegalArgumentException("Error: Voyage localisation not equal 2, or loc1 equals loc2: ");
                }
                yield new Flight(id, price, company, vehicule, localisation, date, seats, type);
            case CruiseType:
                if (localisation.size() < 2 || !localisation.get(0).getId().equals(localisation.get(localisation.size()-1).getId())){
                    System.out.println(localisation.get(0).getId());
                    System.out.println(localisation.get(localisation.size()-1).getId());
                    throw new IllegalArgumentException("Error: Voyage localisation equals 1, or first loc and last not equals: ");
                }
                if (((long)(date.get(1).getTimeInMillis() - date.get(0).getTimeInMillis()) / (24 * 60 * 60 * 1000) > 21)){
                    throw new IllegalArgumentException("Error: Voyage Date larger than 21 days: " + date);
                }
                yield new Cruise(id, price, company, vehicule, localisation, date, seats, type);
            case TrainType:
                if (localisation.size() < 2){
                    throw new IllegalArgumentException("Error: Voyage localisation equals 1: ");
                }
                Set<String> uniqueIds = new HashSet<>();
                for (AbstractLocalisation loc : localisation) {
                    if (!uniqueIds.add(loc.getId())) {
                        throw new IllegalArgumentException("Error: Duplicate localisation found with ID: " + loc.getId());
                    }
                }
                yield new Train(id, price, company, vehicule, localisation, date, seats, type);
        };
        return voyage;
    }
    public void updateVoyage(String id, Integer newPrice, String newVehicule,
                                       List<? extends AbstractLocalisation> newLocalisation, List<Calendar> newDate) {
        AbstractVoyage voyage = model.getVoyagesById(id);
        if (voyage == null) {
            throw new IllegalArgumentException("No voyage found with ID: " + id);
        }

        boolean updatePrice = newPrice != null && newPrice > 0;
        boolean updateVehicle = newVehicule != null && !newVehicule.trim().isEmpty();
        boolean updateDates = newDate != null;
        boolean updateLocalisations = newLocalisation != null && !newLocalisation.isEmpty();

        if (newPrice != null && newPrice <= 0) {
            throw new IllegalArgumentException("Error: Price must be positive.");
        }

        if (updateVehicle && updateDates) {
            for (AbstractVoyage voyageV : model.getVoyages()) {
                List<Calendar> VDate = voyageV.getDates();
                if (voyageV.getType() == voyage.getType() && voyageV.getVehicule().equals(newVehicule) &&
                        ((!newDate.get(0).before(VDate.get(0)) && !newDate.get(0).after(VDate.get(1))) ||
                                (!newDate.get(1).before(VDate.get(0)) && !newDate.get(1).after(VDate.get(1))))) {
                    throw new IllegalArgumentException("Error: Vehicule '" + newVehicule + "' already in use on those dates: " +
                            newDate.get(0).getTime() + " / " + newDate.get(1).getTime());
                }
            }
        }

        if (updateLocalisations) {
            switch (voyage.getType()) {
                case FlightType:
                    if (newLocalisation.size() != 2 || newLocalisation.get(0).getId().equals(newLocalisation.get(1).getId())){
                        throw new IllegalArgumentException("Error: Flight requires exactly two different localisations.");
                    }
                    break;
                case CruiseType:
                case TrainType:
                    if (newLocalisation.size() > 1 || !newLocalisation.get(0).getId().equals(newLocalisation.get(newLocalisation.size() - 1).getId())){
                        throw new IllegalArgumentException("Error: Cruise and Train voyages require exactly one unique localisation.");
                    }
                    break;
            }
        }

        if (updateDates) {
            if (!newDate.get(1).after(newDate.get(0))) {
                throw new IllegalArgumentException("Error: Departure date must be before arrival date.");
            }

            if (voyage.getType() == TravelType.CruiseType && ((long)(newDate.get(1).getTimeInMillis() - newDate.get(0).getTimeInMillis()) / (24 * 60 * 60 * 1000) > 21)) {
                throw new IllegalArgumentException("Error: Cruise cannot be longer than 21 days.");
            }
        }

        if (updatePrice) {
            voyage.updatePrice(newPrice);
        }
        if (updateVehicle) {
            voyage.updateVehicule(newVehicule);
        }
        if (updateLocalisations) {
            voyage.updateLocalisation(newLocalisation);
        }
        if (updateDates && newDate.get(0).compareTo(newDate.get(1)) == 0) {
            voyage.updateDate(newDate);
        }
    }
}

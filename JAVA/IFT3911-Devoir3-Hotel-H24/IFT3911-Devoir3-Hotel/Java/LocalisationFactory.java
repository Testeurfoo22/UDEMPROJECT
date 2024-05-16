public interface LocalisationFactory {
    AbstractLocalisation createLocalisation(TravelType type, String id, String name, String city);
}

class ConcreteLocalisationFactory implements LocalisationFactory {
    private static ConcreteLocalisationFactory instance;
    private LocalisationModel model;

    private ConcreteLocalisationFactory(LocalisationModel model) {
        this.model = model;
    }
    public static synchronized ConcreteLocalisationFactory getInstance(LocalisationModel model) {
        if (instance == null) {
            if (model == null) {
                throw new IllegalArgumentException("Model cannot be null on the first call to getInstance.");
            }
            instance = new ConcreteLocalisationFactory(model);
        }
        return instance;
    }
    public synchronized LocalisationModel getModel(){
        return model;
    }

    @Override
    public AbstractLocalisation createLocalisation(TravelType type, String id, String name, String city) {
        AbstractLocalisation localisation = model.getLocalisationById(id);
        if (localisation != null && localisation.getType() == type) {
            throw new IllegalArgumentException("localisation already created with ID: " + id);
        }
        if (id.length() != 3){
            throw new IllegalArgumentException("Wrong length with ID: " + id);
        }
        localisation = switch (type) {
            case FlightType -> new Airport(id, name, city, type);
            case CruiseType -> new Port(id, name, city, type);
            case TrainType -> new Station(id, name, city, type);
        };
        return localisation;
    }
    public void updateLocalisation(String id, String newName, String newCity) {
        AbstractLocalisation localisation = model.getLocalisationById(id);
        if (localisation == null) {
            throw new IllegalArgumentException("No localisation found with ID: " + id);
        }
        if (newName != null && !newName.trim().isEmpty()) {
            localisation.updateName(newName);
        }
        if (newCity != null && !newCity.trim().isEmpty()) {
            localisation.updateCity(newCity);
        }
    }
}
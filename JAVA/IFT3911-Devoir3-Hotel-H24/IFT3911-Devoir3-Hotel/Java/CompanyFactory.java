public interface CompanyFactory {
    AbstractCompany createCompany(TravelType type, String id, String iata, String name);
}

class ConcreteCompanyFactory implements CompanyFactory {
    private static ConcreteCompanyFactory instance;
    private CompanyModel model;

    private ConcreteCompanyFactory(CompanyModel model) {
        this.model = model;
    }
    public static synchronized ConcreteCompanyFactory getInstance(CompanyModel model) {
        if (instance == null) {
            if (model == null) {
                throw new IllegalArgumentException("Model cannot be null on the first call to getInstance.");
            }
            instance = new ConcreteCompanyFactory(model);
        }
        return instance;
    }
    public synchronized CompanyModel getModel(){
        return model;
    }

    @Override
    public AbstractCompany createCompany(TravelType type, String id, String iata, String name) {
        AbstractCompany company = model.getCompanyById(id);
        if (company != null && company.getType() == type) {
            throw new IllegalArgumentException("Company already created with ID: " + id);
        }
        if (id.length() > 6){
            throw new IllegalArgumentException("Wrong length with ID: " + id);
        }
        if (iata.length() != 2 && model.getCompanyByIata(iata) != null){
            throw new IllegalArgumentException("Company already created with Iata: " + id);
        }
        company = switch (type) {
            case FlightType -> new AirCompany(id, iata, name, type);
            case CruiseType -> new CruiseCompany(id, iata, name, type);
            case TrainType -> new TrainCompany(id, iata, name, type);
        };
        return company;
    }
    public void updateCompany(String id, String newName) {
        AbstractCompany company = model.getCompanyById(id);
        if (company == null) {
            throw new IllegalArgumentException("No localisation found with ID: " + id);
        }
        if (newName != null && !newName.trim().isEmpty()) {
            company.updateName(newName);
        }
    }
}
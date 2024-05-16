import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public interface Observer {
    void update();
}

interface Observable {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}

class LocalisationModel implements Observable {
    private List<Observer> observers = new ArrayList<>();
    private List<AbstractLocalisation> localisations = new ArrayList<>();

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    public void addLocalisation(ConcreteLocalisationFactory factory, TravelType type, String id, String name, String city) {
        AbstractLocalisation newLoc = factory.createLocalisation(type, id.toUpperCase(), name.toUpperCase(), city.toUpperCase());
        localisations.add(newLoc);
        notifyObservers();
    }
    public void updateLocalisation(ConcreteLocalisationFactory factory, String id, String newName, String newCity) {
        factory.updateLocalisation(id, newName, newCity);
        notifyObservers();
    }
    public void deleteLocalisation(String id) {
        boolean exists = localisations.stream().anyMatch(localisation -> localisation.getId().equals(id));
        if (!exists) {
            throw new IllegalArgumentException("No localisation found with ID : " + id);
        }
        localisations.removeIf(localisation -> localisation.getId().equals(id));
        notifyObservers();
    }

    public List<AbstractLocalisation> getLocalisations() {
        return localisations;
    }
    public AbstractLocalisation getLocalisationById(String id) {
        for (AbstractLocalisation localisation : localisations) {
            if (localisation.getId().equals(id)) {
                return localisation;
            }
        }
        return null;
    }
}
class LocalisationPanel implements Observer {
    private LocalisationModel model;
    private JTextArea detailsArea;
    private DisplayLocalisationVisitor visitor = new DisplayLocalisationVisitor();
    private JPanel panel;
    private TravelType type;

    public LocalisationPanel(LocalisationModel model, TravelType type) {
        this.model = model;
        this.type = type;
        this.detailsArea = new JTextArea(10, 30);
        this.detailsArea.setEditable(false);
        this.panel = initUI();
        update();
    }

    private JPanel initUI() {
        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void update() {
        SwingUtilities.invokeLater(() -> {
            StringBuilder detailsBuilder = new StringBuilder("ID / NAME / CITY\n\n");
            for (AbstractLocalisation loc : model.getLocalisations()) {
                if (loc.getType() == this.type){
                    detailsBuilder.append(loc.accept(visitor)).append("\n");
                }
            }
            detailsArea.setText(detailsBuilder.toString());
        });
    }

    public JPanel getPanel() {
        return this.panel;
    }
}

class CompanyModel implements Observable {
    private List<Observer> observers = new ArrayList<>();
    private List<AbstractCompany> companies = new ArrayList<>();

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    public void addCompany(ConcreteCompanyFactory factory, TravelType type, String id, String iata, String name) {
        AbstractCompany newCom = factory.createCompany(type, id, iata, name);
        companies.add(newCom);
        notifyObservers();
    }
    public void updateLocalisation(ConcreteCompanyFactory factory, String id, String newName) {
        factory.updateCompany(id, newName);
        notifyObservers();
    }
    public void deleteCompany(String id) {
        boolean exists = companies.stream().anyMatch(company -> company.getId().equals(id));
        if (!exists) {
            throw new IllegalArgumentException("No company found with ID : " + id);
        }
        companies.removeIf(company -> company.getId().equals(id));
        notifyObservers();
    }

    public List<AbstractCompany> getCompanies() {
        return companies;
    }
    public AbstractCompany getCompanyById(String id) {
        for (AbstractCompany company : companies) {
            if (company.getId().equals(id)) {
                return company;
            }
        }
        return null;
    }
    public AbstractCompany getCompanyByIata(String iata) {
        for (AbstractCompany company : companies) {
            if (company.getIata().equals(iata)) {
                return company;
            }
        }
        return null;
    }
}
class CompanyPanel implements Observer {
    private CompanyModel model;
    private JTextArea detailsArea;
    private DisplayCompanyVisitor visitor = new DisplayCompanyVisitor();
    private JPanel panel;
    private TravelType type;

    public CompanyPanel(CompanyModel model, TravelType type) {
        this.model = model;
        this.type = type;
        this.detailsArea = new JTextArea(10, 30);
        this.detailsArea.setEditable(false);
        this.panel = initUI();
        update();
    }

    private JPanel initUI() {
        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void update() {
        SwingUtilities.invokeLater(() -> {
            StringBuilder detailsBuilder = new StringBuilder("ID / IATA / NAME\n\n");
            for (AbstractCompany loc : model.getCompanies()) {
                if (loc.getType() == this.type){
                    detailsBuilder.append(loc.accept(visitor)).append("\n");
                }
            }
            detailsArea.setText(detailsBuilder.toString());
        });
    }

    public JPanel getPanel() {
        return this.panel;
    }
}

class VoyageModel implements Observable {
    private List<Observer> observers = new ArrayList<>();
    private List<AbstractVoyage> voyages = new ArrayList<>();

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    public void addVoyage(ConcreteVoyageFactory factory, TravelType type, String id, Integer price, AbstractCompany company, String vehicule,
                                      List<? extends AbstractLocalisation> localisation, List<Calendar> date, List<List<Seat>> seats) {
        AbstractVoyage newVoy = factory.createVoyage(type, id, price, company, vehicule, localisation, date, seats);
        voyages.add(newVoy);
        notifyObservers();
    }
    public void updateVoyage(ConcreteVoyageFactory factory, String id, Integer newPrice, String newVehicule,
                             List<? extends AbstractLocalisation> newLocalisation, List<Calendar> newDate) {
        factory.updateVoyage(id, newPrice, newVehicule, newLocalisation, newDate);
        notifyObservers();
    }
    public void deleteVoyage(String id) {
        boolean exists = voyages.stream().anyMatch(voyage -> voyage.getId().equals(id));
        if (!exists) {
            throw new IllegalArgumentException("No voyage found with ID : " + id);
        }
        voyages.removeIf(voyage -> voyage.getId().equals(id));
        notifyObservers();
    }

    public List<AbstractVoyage> getVoyages() {
        return voyages;
    }
    public AbstractVoyage getVoyagesById(String id) {
        for (AbstractVoyage voyage : voyages) {
            if (voyage.getId().equals(id)) {
                return voyage;
            }
        }
        return null;
    }
}
class VoyageAdminPanel implements Observer {
    private VoyageModel model;
    private JTextArea detailsArea;
    private DisplayVoyageAdminVisitor visitor = new DisplayVoyageAdminVisitor();
    private JPanel panel;
    private TravelType type;

    public VoyageAdminPanel(VoyageModel model, TravelType type) {
        this.model = model;
        this.type = type;
        this.detailsArea = new JTextArea(10, 30);
        this.detailsArea.setEditable(false);
        this.panel = initUI();
        update();
    }

    private JPanel initUI() {
        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void update() {
        SwingUtilities.invokeLater(() -> {
            StringBuilder detailsBuilder = new StringBuilder("LOC-LOC:[IDCom]ID(DATE-DATE)|SECSIZE(REMAIN/ALL)PRICE|...\n\n");
            for (AbstractVoyage voy : model.getVoyages()) {
                if (voy.getType() == this.type){
                    detailsBuilder.append(voy.accept(visitor, null)).append("\n");
                }
            }
            detailsArea.setText(detailsBuilder.toString());
        });
    }

    public JPanel getPanel() {
        return this.panel;
    }
}
class VoyageClientPanel implements Observer {
    private VoyageModel model;
    private JTextArea detailsArea;
    private DisplayVoyageClientVisitor visitor = new DisplayVoyageClientVisitor();
    private JPanel panel;
    private TravelType type;
    private AbstractCompany company;
    private Calendar startDate;
    private Calendar endDate;
    private section section;

    public VoyageClientPanel(VoyageModel model, TravelType type) {
        this.model = model;
        this.type = type;
        this.company = null;
        this.startDate = null;
        this.endDate = null;
        this.section = null;
        this.detailsArea = new JTextArea(10, 30);
        this.detailsArea.setEditable(false);
        this.panel = initUI();
        update();
    }

    private JPanel initUI() {
        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void update() {
        SwingUtilities.invokeLater(() -> {
            StringBuilder detailsBuilder = new StringBuilder("LOC-LOC:[IDCom]ID(DATE-DATE)|PRICE|SECTIONREMAIN|...\n\n");
            for (AbstractVoyage voy : model.getVoyages()) {
                if ((this.type == null || voy.getType() == this.type) &&
                        (this.company == null || this.company.equals(voy.getCompany()))) {
                    List<Calendar> dates = voy.getDates();
                    boolean dateMatch = checkDateMatch(this.startDate, dates.get(0)) &&
                            checkDateMatch(this.endDate, dates.get(1));
                    if (dateMatch) {
                        List<List<Seat>> seats = voy.getSeats();
                        boolean hasMatchingSeat = (this.section == null) || seats.stream()
                                .flatMap(List::stream)
                                .anyMatch(seat -> this.section.equals(seat.getSection()));
                        if (hasMatchingSeat) {
                            detailsBuilder.append(voy.accept(visitor, this.section)).append("\n");
                        }
                    }
                }
            }
            detailsArea.setText(detailsBuilder.toString());
        });
    }

    private boolean checkDateMatch(Calendar filterDate, Calendar voyageDate) {
        if (filterDate == null) {
            return true;
        }
        if (voyageDate == null) {
            return false;
        }
        Calendar filterCal = (Calendar) filterDate.clone();
        filterCal.set(Calendar.HOUR_OF_DAY, 0);
        filterCal.set(Calendar.MINUTE, 0);
        filterCal.set(Calendar.SECOND, 0);
        filterCal.set(Calendar.MILLISECOND, 0);

        Calendar voyageCal = (Calendar) voyageDate.clone();
        voyageCal.set(Calendar.HOUR_OF_DAY, 0);
        voyageCal.set(Calendar.MINUTE, 0);
        voyageCal.set(Calendar.SECOND, 0);
        voyageCal.set(Calendar.MILLISECOND, 0);

        return filterCal.get(Calendar.YEAR) == voyageCal.get(Calendar.YEAR) &&
                filterCal.get(Calendar.MONTH) == voyageCal.get(Calendar.MONTH) &&
                filterCal.get(Calendar.DAY_OF_MONTH) == voyageCal.get(Calendar.DAY_OF_MONTH);
    }

    public JPanel getPanel() {
        return this.panel;
    }
    public void setCompany(AbstractCompany company) {
        this.company = company;
    }
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }
    public void setSection(section section) {
        this.section = section;
    }
}
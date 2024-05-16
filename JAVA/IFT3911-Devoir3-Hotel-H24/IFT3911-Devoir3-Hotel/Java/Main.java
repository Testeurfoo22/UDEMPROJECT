import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private LocalisationModel localisationModel = new LocalisationModel();
    private ConcreteLocalisationFactory localisationFactory;
    private CompanyModel companyModel = new CompanyModel();
    private ConcreteCompanyFactory companyFactory;
    private VoyageModel voyageModel = new VoyageModel();
    private ConcreteVoyageFactory voyageFactory;

    public Main() {
        localisationFactory = ConcreteLocalisationFactory.getInstance(localisationModel);
        companyFactory = ConcreteCompanyFactory.getInstance(companyModel);
        voyageFactory = ConcreteVoyageFactory.getInstance(voyageModel);

        loadInitialData();
        setupUI();
    }

    private void loadInitialData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("../data/localisation.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                Command addCommand = new AddLocalisationCommand(localisationFactory, TravelType.valueOf(parts[0]), parts[1], parts[2], parts[3]);
                addCommand.execute();
            }
        } catch (IOException e) {
            System.err.println("Error reading initial data: " + e.getMessage());
        }
        try (BufferedReader reader = new BufferedReader(new FileReader("../data/companies.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                Command addCommand = new AddCompanyCommand(companyFactory, TravelType.valueOf(parts[0]), parts[1], parts[2], parts[3]);
                addCommand.execute();
            }
        } catch (IOException e) {
            System.err.println("Error reading initial data: " + e.getMessage());
        }
        try (BufferedReader reader = new BufferedReader(new FileReader("../data/voyages.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");

                List<AbstractLocalisation> localisations = new ArrayList<>();
                for (String id : Arrays.asList(parts[5].split("-"))) {
                    localisations.add(localisationModel.getLocalisationById(id));
                }

                String[] dateTimeParts = parts[6].split("-");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd:HH.mm");
                List<Calendar> dates = new ArrayList<>();
                for (String dateTime : dateTimeParts) {
                    try {
                        Date date = sdf.parse(dateTime);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        dates.add(calendar);
                    } catch (ParseException e) {
                        System.out.println("Error parsing the date-time string: " + e.getMessage());
                    }
                }

                List<List<Seat>> seats = new ArrayList<>();
                for (String part : Arrays.asList(parts[7].split("-"))) {
                    List<Seat> sectionSeat = new ArrayList<>();
                    List<String> sectionValue = Arrays.asList(part.split(","));
                    for (int x = 0; x < Integer.parseInt(sectionValue.get(2)); x++){
                        switch (TravelType.valueOf(parts[0])) {
                            case FlightType:
                                sectionSeat.add(new Seat(PlaneSection.valueOf(sectionValue.get(0)), Taille.valueOf(sectionValue.get(1))));
                                break;
                            case CruiseType:
                                sectionSeat.add(new Seat(CruiseSection.valueOf(sectionValue.get(0)), Taille.valueOf(sectionValue.get(1))));
                                break;
                            case TrainType:
                                sectionSeat.add(new Seat(TrainSection.valueOf(sectionValue.get(0)), Taille.valueOf(sectionValue.get(1))));
                                break;
                            default:
                                throw new IllegalArgumentException("Unknown transport type");
                        }
                    }
                    seats.add(sectionSeat);
                }

                Command addCommand = new AddVoyageCommand(voyageFactory, TravelType.valueOf(parts[0]),
                        parts[1], Integer.parseInt(parts[4]), companyModel.getCompanyById(parts[3]), parts[2],
                        localisations, dates, seats);
                addCommand.execute();
            }
        } catch (IOException e) {
            System.err.println("Error reading initial data: " + e.getMessage());
        }
    }
    private void saveData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("../data/localisation.csv"))) {
            for (AbstractLocalisation loc : localisationModel.getLocalisations()) {
                String line = loc.getType() + ";" + loc.getId() + ";" + loc.getName() + ";" + loc.getCity();
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("../data/companies.csv"))) {
            for (AbstractCompany com : companyModel.getCompanies()) {
                String line = com.getType() + ";" + com.getId() + ";" + com.getIata() + ";" + com.getName();
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("../data/voyages.csv"))) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd:HH.mm");
            for (AbstractVoyage voy : voyageModel.getVoyages()) {
                String localisationStr = voy.getLocalisations().stream()
                        .map(AbstractLocalisation::getId)
                        .collect(Collectors.joining("-"));
                String dateStr = String.format("%s-%s",
                        dateFormat.format(voy.getDates().get(0).getTime()),
                        dateFormat.format(voy.getDates().get(1).getTime()));
                String seatStr = voy.getSeats().stream()
                        .map(Seat::formatSeatSection)
                        .collect(Collectors.joining("-"));
                String line = voy.getType() + ";" + voy.getId() + ";" + voy.getVehicule() + ";" + voy.getCompany().getId()
                        + ";" + voy.getPrice() + ";" + localisationStr + ";" + dateStr + ";" + seatStr;
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private void setupUI() {
        JFrame frame = new JFrame("System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(400, 200));
        frame.setLayout(new FlowLayout());

        JButton adminButton = new JButton("Admin");
        JButton clientButton = new JButton("Client");
        frame.add(adminButton);
        frame.add(clientButton);

        adminButton.addActionListener(e -> new AdminUI(localisationFactory, companyFactory, voyageFactory));
        clientButton.addActionListener(e -> new ClientUI(voyageFactory, companyModel, localisationModel));

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveData();
            }
        });

        frame.setVisible(true);
    }
    public static void main(String[] args) {
        new Main();
    }
}
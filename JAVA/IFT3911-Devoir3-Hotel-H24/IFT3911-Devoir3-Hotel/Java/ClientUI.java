import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ClientUI {
    private static int nextId = 1;
    private Integer instanceId;
    private ConcreteVoyageFactory factoryVoy;
    private VoyageModel modelVoy;
    private CompanyModel modelCom;
    private LocalisationModel modelLoc;
    private VoyageClientPanel voyPanel;

    public ClientUI(ConcreteVoyageFactory factoryVoy, CompanyModel modelCom, LocalisationModel modelLoc) {
        this.factoryVoy = factoryVoy;
        this.modelCom = modelCom;
        this.modelLoc = modelLoc;
        this.modelVoy = factoryVoy.getModel();

        this.instanceId = nextId++;
        initializeUI();
    }

    private void initializeUI() {
        JFrame clientFrame = new JFrame("Client Interface");
        clientFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        clientFrame.setSize(new Dimension(800, 600));
        JTabbedPane mainTabbedPane = new JTabbedPane();

        mainTabbedPane.addTab("Search", createSearchPanel());
        mainTabbedPane.addTab("Booked", createBookedPanel());
        mainTabbedPane.addTab("Paid", createPaidPanel());

        mainTabbedPane.addChangeListener(e -> {
            JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
            int index = sourceTabbedPane.getSelectedIndex();
            String title = sourceTabbedPane.getTitleAt(index);

            switch (title) {
                case "Search":
                    sourceTabbedPane.setComponentAt(index, createSearchPanel());
                    break;
                case "Booked":
                    sourceTabbedPane.setComponentAt(index, createBookedPanel());
                    break;
                case "Paid":
                    sourceTabbedPane.setComponentAt(index, createPaidPanel());
                    break;
            }
        });

        clientFrame.add(mainTabbedPane, BorderLayout.CENTER);
        clientFrame.setVisible(true);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTabbedPane travelTypeTabbedPane = new JTabbedPane();
        travelTypeTabbedPane.addTab("Air", createTravelTypeSearchPanel(TravelType.FlightType));
        travelTypeTabbedPane.addTab("Sea", null);
        travelTypeTabbedPane.addTab("Land", null);

        travelTypeTabbedPane.addChangeListener(e -> {
            JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
            int index = sourceTabbedPane.getSelectedIndex();
            String title = sourceTabbedPane.getTitleAt(index);

            switch (title) {
                case "Air":
                    sourceTabbedPane.setComponentAt(index, createTravelTypeSearchPanel(TravelType.FlightType));
                    break;
                case "Sea":
                    sourceTabbedPane.setComponentAt(index, createTravelTypeSearchPanel(TravelType.CruiseType));
                    break;
                case "Land":
                    sourceTabbedPane.setComponentAt(index, createTravelTypeSearchPanel(TravelType.TrainType));
                    break;
            }
        });

        panel.add(travelTypeTabbedPane, BorderLayout.CENTER);

        return panel;
    }
    private JPanel createTravelTypeSearchPanel(TravelType type) {
        JPanel panel = new JPanel(new BorderLayout());
        if (voyPanel != null){
            modelVoy.removeObserver(voyPanel);
        }
        voyPanel = new VoyageClientPanel(modelVoy, type);

        panel.add(voyPanel.getPanel(), BorderLayout.CENTER);
        modelVoy.addObserver(voyPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton filterButton = new JButton("Filters");
        JButton bookButton = new JButton("Book");

        filterButton.addActionListener(e -> showFilterDialog(type));
        bookButton.addActionListener(e -> showBookDialog(type));

        buttonPanel.add(filterButton);
        buttonPanel.add(bookButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
    private void showFilterDialog(TravelType type) {
        JDialog filterDialog = new JDialog();
        filterDialog.setTitle("Filter Voyages");
        filterDialog.setSize(300, 400);
        filterDialog.setLayout(new GridLayout(0, 1));

        JComboBox<String> companyComboBox = new JComboBox<>();
        modelCom.getCompanies().forEach(com -> companyComboBox.addItem(com.getId()));

        JSpinner startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy.MM.dd");
        startDateSpinner.setEditor(startDateEditor);
        startDateSpinner.setEnabled(false);

        JSpinner endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy.MM.dd");
        endDateSpinner.setEditor(endDateEditor);
        endDateSpinner.setEnabled(false);

        JCheckBox startDateCheckBox = new JCheckBox("Enable Start Date", false);
        startDateCheckBox.addActionListener(e -> {
            startDateSpinner.setEnabled(startDateCheckBox.isSelected());
            if (!startDateCheckBox.isSelected()) {
                startDateSpinner.setValue(new Date());
            }
        });

        JCheckBox endDateCheckBox = new JCheckBox("Enable End Date", false);
        endDateCheckBox.addActionListener(e -> {
            endDateSpinner.setEnabled(endDateCheckBox.isSelected());
            if (!endDateCheckBox.isSelected()) {
                endDateSpinner.setValue(new Date());
            }
        });

        JComboBox<Enum<?>> sectionComboBox = new JComboBox<>();
        switch (type) {
            case FlightType:
                for (PlaneSection section : PlaneSection.values()) {
                    sectionComboBox.addItem(section);
                }
                break;
            case CruiseType:
                for (CruiseSection section : CruiseSection.values()) {
                    sectionComboBox.addItem(section);
                }
                break;
            case TrainType:
                for (TrainSection section : TrainSection.values()) {
                    sectionComboBox.addItem(section);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown transport type");
        }
        sectionComboBox.insertItemAt(null, 0);
        sectionComboBox.setSelectedIndex(0);

        companyComboBox.insertItemAt(null, 0);
        companyComboBox.setSelectedIndex(0);

        filterDialog.add(new JLabel("Company:"));
        filterDialog.add(companyComboBox);
        filterDialog.add(startDateCheckBox);
        filterDialog.add(new JLabel("Start Date:"));
        filterDialog.add(startDateSpinner);
        filterDialog.add(endDateCheckBox);
        filterDialog.add(new JLabel("End Date:"));
        filterDialog.add(endDateSpinner);
        filterDialog.add(new JLabel("Section:"));
        filterDialog.add(sectionComboBox);

        JButton confirmButton = new JButton("Apply Filters");
        filterDialog.add(confirmButton);

        confirmButton.addActionListener(e -> {
            Date startDate = startDateCheckBox.isSelected() ? (Date) startDateSpinner.getValue() : null;
            Date endDate = endDateCheckBox.isSelected() ? (Date) endDateSpinner.getValue() : null;
            Calendar startCal = startDate != null ? Calendar.getInstance() : null;
            Calendar endCal = endDate != null ? Calendar.getInstance() : null;
            if (startCal != null) startCal.setTime(startDate);
            if (endCal != null) endCal.setTime(endDate);

            if (companyComboBox.getSelectedItem() != null){
                voyPanel.setCompany(modelCom.getCompanyById((String)companyComboBox.getSelectedItem()));
            }
            else{
                voyPanel.setCompany(null);
            }
            if (sectionComboBox.getSelectedItem() != null){
                voyPanel.setSection((section)sectionComboBox.getSelectedItem());
            }
            else{
                voyPanel.setSection(null);
            }
            if (startCal != null) {
                voyPanel.setStartDate(startCal);
            } else {
                voyPanel.setStartDate(null);
            }
            if (endCal != null) {
                voyPanel.setEndDate(endCal);
            } else {
                voyPanel.setEndDate(null);
            }
            modelVoy.notifyObservers();
            filterDialog.dispose();
        });

        filterDialog.setVisible(true);
    }

    private void showBookDialog(TravelType type) {
        JDialog bookDialog = new JDialog();
        bookDialog.setTitle("Book Voyage");
        bookDialog.setSize(300, 200);

        JTextField idField = new JTextField();

        var ref = new Object() {
            JComboBox<Enum<?>> sectionComboBox = null;
        };

        switch (type) {
            case FlightType:
                ref.sectionComboBox = new JComboBox<>();
                for (PlaneSection section : PlaneSection.values()) {
                    ref.sectionComboBox.addItem(section);
                }
                ref.sectionComboBox.insertItemAt(null, 0);
                break;
            case CruiseType:
                ref.sectionComboBox = new JComboBox<>();
                for (CruiseSection section : CruiseSection.values()) {
                    ref.sectionComboBox.addItem(section);
                }
                ref.sectionComboBox.insertItemAt(null, 0);
                break;
            case TrainType:
                ref.sectionComboBox = new JComboBox<>();
                for (TrainSection section : TrainSection.values()) {
                    ref.sectionComboBox.addItem(section);
                }
                ref.sectionComboBox.insertItemAt(null, 0);
                break;
            default:
                throw new IllegalArgumentException("Unknown transport type");
        }
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("ID (with iata):"));
        panel.add(idField);
        panel.add(new JLabel("Section:"));
        panel.add(ref.sectionComboBox);

        JButton bookButton = new JButton("Confirm Booking");
        panel.add(bookButton);
        bookDialog.add(panel);

        bookButton.addActionListener(e -> {
            AbstractVoyage voyage = modelVoy.getVoyagesById(idField.getText().toUpperCase());
            if (voyage != null){
                Optional<Seat> availableSeat = voyage.getSeats().stream()
                        .flatMap(List::stream)
                        .filter(seat -> ref.sectionComboBox.getSelectedItem() != null
                                && ref.sectionComboBox.getSelectedItem().equals(seat.getSection())
                                && seat.getState() instanceof AvailableState)
                        .findFirst();
                if (availableSeat.isPresent()) {
                    availableSeat.get().getState().book(availableSeat.get(), this.instanceId);
                    modelVoy.notifyObservers();
                    JOptionPane.showMessageDialog(null, "Booking successful!");
                } else {
                    JOptionPane.showMessageDialog(null, "No section available or no seats available.");
                }
                bookDialog.dispose();
            }
            else{
                JOptionPane.showMessageDialog(null, "ID Error.");
            }
        });

        bookDialog.setVisible(true);
    }

    private JPanel createBookedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel instructionLabel = new JLabel("Click on a booked seat to pay or unbook.");
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(instructionLabel, BorderLayout.NORTH);

        DefaultListModel<Seat> modelSeat = new DefaultListModel<>();
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(list);
        panel.add(scrollPane, BorderLayout.CENTER);

        modelVoy.getVoyages().forEach(voyage -> {
            voyage.getSeats().stream()
                    .flatMap(List::stream)
                    .filter(seat -> Objects.equals(seat.getUser(), this.instanceId) && seat.getState() instanceof BookedState)
                    .forEach(seat -> {
                        modelSeat.addElement(seat);
                        model.addElement("Seat Section: " + seat.getSection() + " on Voyage: " + voyage.getId());
                    });
        });

        JButton payButton = new JButton("Pay");
        JButton unbookButton = new JButton("Unbook");
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(payButton);
        buttonPanel.add(unbookButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        payButton.addActionListener(e -> {
            int ind = list.getSelectedIndex();
            if (ind != -1) {
                Seat selected = modelSeat.getElementAt(ind);
                selected.getState().pay(selected);
                model.removeElementAt(ind);
                modelSeat.removeElementAt(ind);
                JOptionPane.showMessageDialog(null, "Payment processed for: " + selected.getSection());
            } else {
                JOptionPane.showMessageDialog(null, "Please select a seat to pay.");
            }
        });

        unbookButton.addActionListener(e -> {
            int ind = list.getSelectedIndex();
            if (ind != -1) {
                Seat selected = modelSeat.getElementAt(ind);
                selected.getState().unbook(selected);
                model.removeElementAt(ind);
                modelSeat.removeElementAt(ind);
                modelVoy.notifyObservers();
                JOptionPane.showMessageDialog(null, "Unbooked: Seat in " + selected.getSection());
            } else {
                JOptionPane.showMessageDialog(null, "Please select a seat to unbook.");
            }
        });

        return panel;
    }
    private JPanel createPaidPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(list);
        panel.add(scrollPane, BorderLayout.CENTER);

        modelVoy.getVoyages().forEach(voyage -> {
            voyage.getSeats().stream()
                    .flatMap(List::stream)
                    .filter(seat -> Objects.equals(seat.getUser(), instanceId) && seat.getState() instanceof PaidState)
                    .forEach(seat -> {
                        model.addElement("Seat Section: " + seat.getSection() + " on Voyage: " + voyage.getId());
                    });
        });

        return panel;
    }
}

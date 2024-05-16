import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AdminUI {
    private ConcreteLocalisationFactory factoryLoc;
    private ConcreteCompanyFactory factoryCom;
    private ConcreteVoyageFactory factoryVoy;
    private LocalisationModel modelLoc;
    private CompanyModel modelCom;
    private VoyageModel modelVoy;
    private LocalisationPanel locPanel;
    private CompanyPanel comPanel;
    private VoyageAdminPanel voyPanel;

    public AdminUI(ConcreteLocalisationFactory factoryLoc, ConcreteCompanyFactory factoryCom, ConcreteVoyageFactory factoryVoy) {
        this.factoryLoc = factoryLoc;
        this.factoryCom = factoryCom;
        this.factoryVoy = factoryVoy;
        this.modelLoc = factoryLoc.getModel();
        this.modelCom = factoryCom.getModel();
        this.modelVoy = factoryVoy.getModel();
        initializeUI();
    }


    private void initializeUI() {
        JFrame adminFrame = new JFrame("Admin Interface");
        adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        adminFrame.setSize(new Dimension(800, 600));

        JTabbedPane mainTabbedPane = new JTabbedPane();
        Map<String, JPanel> panels = new HashMap<>();

        mainTabbedPane.addTab("Air", createTypePanel(TravelType.FlightType));
        mainTabbedPane.addTab("Sea", null);
        mainTabbedPane.addTab("Land", null);

        mainTabbedPane.addChangeListener(e -> {
            int selectedIndex = mainTabbedPane.getSelectedIndex();
            if (selectedIndex != -1) {
                String title = mainTabbedPane.getTitleAt(selectedIndex);
                if (panels.get(title) == null) {
                    TravelType type = getTravelTypeByTitle(title);
                    JPanel panel = createTypePanel(type);
                    panels.put(title, panel);
                    mainTabbedPane.setComponentAt(selectedIndex, panel);
                }
            }
        });

        adminFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                modelLoc.removeObserver(locPanel);
                modelCom.removeObserver(comPanel);
                modelVoy.removeObserver(voyPanel);
            }
        });
        adminFrame.add(mainTabbedPane);
        adminFrame.setVisible(true);
    }

    private static TravelType getTravelTypeByTitle(String title) {
        switch (title) {
            case "Air":
                return TravelType.FlightType;
            case "Sea":
                return TravelType.CruiseType;
            case "Land":
                return TravelType.TrainType;
            default:
                throw new IllegalArgumentException("Unknown tab selected");
        }
    }

    private JPanel createTypePanel(TravelType type) {
        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane typeTabbedPane = new JTabbedPane();

        typeTabbedPane.addTab("Localisation", createLocalisationPanel(type));
        typeTabbedPane.addTab("Company", null);
        typeTabbedPane.addTab("Voyage", null);

        typeTabbedPane.addChangeListener(e -> {
            int selectedIndex = typeTabbedPane.getSelectedIndex();
            if (selectedIndex != -1) {
                switch (typeTabbedPane.getTitleAt(selectedIndex)) {
                    case "Localisation":
                        typeTabbedPane.setComponentAt(selectedIndex, createLocalisationPanel(type));
                        break;
                    case "Company":
                        typeTabbedPane.setComponentAt(selectedIndex, createCompanyPanel(type));
                        break;
                    case "Voyage":
                        typeTabbedPane.setComponentAt(selectedIndex, createVoyagePanel(type));
                        break;
                }
            }
        });

        panel.add(typeTabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLocalisationPanel(TravelType type) {
        JPanel panel = new JPanel(new BorderLayout());
        if (locPanel != null){
            modelLoc.removeObserver(locPanel);
        }
        locPanel = new LocalisationPanel(modelLoc, type);
        panel.add(locPanel.getPanel(), BorderLayout.CENTER);

        addLocalisationButtons(panel, type);
        modelLoc.addObserver(locPanel);
        return panel;
    }

    private void addLocalisationButtons(JPanel panel, TravelType type) {
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton undoButton = new JButton("Undo");

        AtomicReference<Command> lastCommand = new AtomicReference<>();

        addButton.addActionListener(e -> {
            JTextField idField = new JTextField();
            JTextField nameField = new JTextField();
            JTextField cityField = new JTextField();
            Object[] message = {
                    "ID:", idField,
                    "Name:", nameField,
                    "City:", cityField
            };

            while (true) {
                int option = JOptionPane.showConfirmDialog(null, message, "Enter Localisation Details", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String id = idField.getText().trim();
                    String name = nameField.getText().trim();
                    String city = cityField.getText().trim();

                    if (!id.isEmpty() && !name.isEmpty() && !city.isEmpty()) {
                        try {
                            Command addCommand = new AddLocalisationCommand(factoryLoc, type, id.toUpperCase(), name.toUpperCase(), city.toUpperCase());
                            addCommand.execute();
                            lastCommand.set(addCommand);
                        } catch (IllegalArgumentException err) {
                            JOptionPane.showMessageDialog(null, err.getMessage());
                        }
                        break;
                    } else {
                        JOptionPane.showMessageDialog(null, "No empty value");
                    }
                } else {
                    break;
                }
            }
        });

        updateButton.addActionListener(e -> {
            JTextField idField = new JTextField();
            JTextField nameField = new JTextField();
            JTextField cityField = new JTextField();
            Object[] message = {
                    "ID:", idField,
                    "Name:", nameField,
                    "City:", cityField
            };

            while (true) {
                int option = JOptionPane.showConfirmDialog(null, message, "Enter Localisation Details", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String id = idField.getText().trim();
                    String name = nameField.getText().trim();
                    String city = cityField.getText().trim();

                    if (!id.isEmpty()) {
                        try {
                            Command updateCommand = new UpdateLocalisationCommand(factoryLoc, id.toUpperCase(), name.toUpperCase(), city.toUpperCase());
                            updateCommand.execute();
                            lastCommand.set(updateCommand);
                        } catch (IllegalArgumentException err) {
                            JOptionPane.showMessageDialog(null, err.getMessage());
                        }
                        break;
                    } else {
                        JOptionPane.showMessageDialog(null, "No empty ID");
                    }
                } else {
                    break;
                }
            }
        });

        deleteButton.addActionListener(e -> {
            JTextField idField = new JTextField();
            Object[] message = {
                    "ID:", idField
            };

            while (true) {
                int option = JOptionPane.showConfirmDialog(null, message, "Enter Localisation ID", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String id = idField.getText().trim();

                    if (!id.isEmpty()) {
                        try {
                            Command updateCommand = new DeleteLocalisationCommand(factoryLoc, id.toUpperCase());
                            updateCommand.execute();
                            lastCommand.set(updateCommand);
                        } catch (IllegalArgumentException err) {
                            JOptionPane.showMessageDialog(null, err.getMessage());
                        }
                        break;
                    } else {
                        JOptionPane.showMessageDialog(null, "No empty ID");
                    }
                } else {
                    break;
                }
            }
        });

        undoButton.addActionListener(e -> {
            if (lastCommand.get() != null) {
                lastCommand.get().undo();
                lastCommand.set(null);
            }
        });

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.add(addButton);
        buttonsPanel.add(updateButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(undoButton);

        panel.add(buttonsPanel, BorderLayout.SOUTH);
    }

    private JPanel createCompanyPanel(TravelType type) {
        JPanel panel = new JPanel(new BorderLayout());
        if (comPanel != null){
            modelLoc.removeObserver(comPanel);
        }
        comPanel = new CompanyPanel(modelCom, type);
        panel.add(comPanel.getPanel(), BorderLayout.CENTER);

        addCompanyButtons(panel, type);
        modelCom.addObserver(comPanel);
        return panel;
    }

    private void addCompanyButtons(JPanel panel, TravelType type) {
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton undoButton = new JButton("Undo");

        AtomicReference<Command> lastCommand = new AtomicReference<>();

        addButton.addActionListener(e -> {
            JTextField idField = new JTextField();
            JTextField iataField = new JTextField();
            JTextField nameField = new JTextField();
            Object[] message = {
                    "ID:", idField,
                    "Iata:", iataField,
                    "Name:", nameField
            };

            while (true) {
                int option = JOptionPane.showConfirmDialog(null, message, "Enter Company Details", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String id = idField.getText().trim();
                    String iata = iataField.getText().trim();
                    String name = nameField.getText().trim();

                    if (!id.isEmpty() && !iata.isEmpty() && !name.isEmpty()) {
                        try {
                            Command addCommand = new AddCompanyCommand(factoryCom, type, id.toUpperCase(), iata.toUpperCase(), name.toUpperCase());
                            addCommand.execute();
                            lastCommand.set(addCommand);
                        } catch (IllegalArgumentException err) {
                            JOptionPane.showMessageDialog(null, err.getMessage());
                        }
                        break;
                    } else {
                        JOptionPane.showMessageDialog(null, "No empty value");
                    }
                } else {
                    break;
                }
            }
        });

        updateButton.addActionListener(e -> {
            JTextField idField = new JTextField();
            JTextField nameField = new JTextField();
            Object[] message = {
                    "ID:", idField,
                    "Name:", nameField,
            };

            while (true) {
                int option = JOptionPane.showConfirmDialog(null, message, "Enter Company Details", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String id = idField.getText().trim();
                    String name = nameField.getText().trim();

                    if (!id.isEmpty()) {
                        try {
                            Command addCommand = new UpdateCompanyCommand(factoryCom, id.toUpperCase(), name.toUpperCase());
                            addCommand.execute();
                            lastCommand.set(addCommand);
                        } catch (IllegalArgumentException err) {
                            JOptionPane.showMessageDialog(null, err.getMessage());
                        }
                        break;
                    } else {
                        JOptionPane.showMessageDialog(null, "No empty ID");
                    }
                } else {
                    break;
                }
            }
        });

        deleteButton.addActionListener(e -> {
            JTextField idField = new JTextField();
            Object[] message = {
                    "ID:", idField
            };

            while (true) {
                int option = JOptionPane.showConfirmDialog(null, message, "Enter Localisation ID", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String id = idField.getText().trim();

                    if (!id.isEmpty()) {
                        try {
                            Command addCommand = new DeleteCompanyCommand(factoryCom, id.toUpperCase());
                            addCommand.execute();
                            lastCommand.set(addCommand);
                        } catch (IllegalArgumentException err) {
                            JOptionPane.showMessageDialog(null, err.getMessage());
                        }
                        break;
                    } else {
                        JOptionPane.showMessageDialog(null, "No empty ID");
                    }
                } else {
                    break;
                }
            }
        });

        undoButton.addActionListener(e -> {
            if (lastCommand.get() != null) {
                lastCommand.get().undo();
                lastCommand.set(null);
            }
        });

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.add(addButton);
        buttonsPanel.add(updateButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(undoButton);

        panel.add(buttonsPanel, BorderLayout.SOUTH);
    }

    private JPanel createVoyagePanel(TravelType type) {
        JPanel panel = new JPanel(new BorderLayout());
        if (voyPanel != null){
            modelVoy.removeObserver(voyPanel);
        }
        voyPanel = new VoyageAdminPanel(modelVoy, type);
        panel.add(voyPanel.getPanel(), BorderLayout.CENTER);

        addVoyageButtons(panel, type);
        modelVoy.addObserver(voyPanel);
        return panel;
    }

    private void addVoyageButtons(JPanel panel, TravelType type) {
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton undoButton = new JButton("Undo");

        AtomicReference<Command> lastCommand = new AtomicReference<>();

        addButton.addActionListener(e -> {
            JTextField idField = new JTextField();
            JSpinner priceField = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
            JComboBox<String> companyComboBox = new JComboBox<>();
            modelCom.getCompanies().forEach(com -> companyComboBox.addItem(com.getId()));

            JTextField vehicleField = new JTextField();

            DefaultListModel<String> availableLocsModel = new DefaultListModel<>();
            modelLoc.getLocalisations().forEach(loc -> {
                if (loc.getType() == type) {
                    availableLocsModel.addElement(loc.getId());
                }
            });
            JList<String> availableLocsList = new JList<>(availableLocsModel);
            availableLocsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            JButton addLocButton = new JButton(">>");
            JButton removeLocButton = new JButton("<<");
            DefaultListModel<String> selectedLocsModel = new DefaultListModel<>();
            JList<String> selectedLocsList = new JList<>(selectedLocsModel);

            addLocButton.addActionListener(ev -> {
                availableLocsList.getSelectedValuesList().forEach(selectedLocsModel::addElement);
            });

            removeLocButton.addActionListener(ev -> {
                selectedLocsList.getSelectedValuesList().forEach(selectedLocsModel::removeElement);
            });

            JPanel PAN = new JPanel(new GridLayout(1, 2));
            PAN.add(new JScrollPane(availableLocsList));
            JPanel buttonsPanel = new JPanel(new GridLayout(2, 1));
            buttonsPanel.add(addLocButton);
            buttonsPanel.add(removeLocButton);
            PAN.add(buttonsPanel);
            PAN.add(new JScrollPane(selectedLocsList));

            JSpinner startDateSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner endDateSpinner = new JSpinner(new SpinnerDateModel());
            startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy.MM.dd:HH.mm"));
            endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy.MM.dd:HH.mm"));

            JComboBox<section> sectionComboBox = new JComboBox<>();
            JComboBox<Taille> sizeComboBox = new JComboBox<>();
            JSpinner rowSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
            JButton addSeatButton = new JButton(">>");
            JButton removeSeatButton = new JButton("<<");
            DefaultListModel<String> seatsListModel = new DefaultListModel<>();
            JList<String> seatsList = new JList<>(seatsListModel);

            switch (type) {
                case FlightType:
                    sectionComboBox.setModel(new DefaultComboBoxModel<>(PlaneSection.values()));
                    sizeComboBox.setModel(new DefaultComboBoxModel<>(Arrays.stream(Taille.values())
                            .filter(t -> t != Taille.NULL)
                            .toArray(Taille[]::new)));
                    break;
                case CruiseType:
                    sectionComboBox.setModel(new DefaultComboBoxModel<>(CruiseSection.values()));
                    sizeComboBox.setModel(new DefaultComboBoxModel<>(new Taille[]{Taille.NULL}));
                    sizeComboBox.setEnabled(false);
                    break;
                case TrainType:
                    sectionComboBox.setModel(new DefaultComboBoxModel<>(TrainSection.values()));
                    sizeComboBox.setModel(new DefaultComboBoxModel<>(new Taille[]{Taille.ETROIT}));
                    sizeComboBox.setEnabled(false);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown transport type");
            }

            addSeatButton.addActionListener(ev -> {
                section selectedSection = (section) sectionComboBox.getSelectedItem();
                Taille selectedSize = (Taille) sizeComboBox.getSelectedItem();
                int rows = (int) rowSpinner.getValue();

                String seatDetail = selectedSection + "-" + selectedSize + "-" + rows;
                seatsListModel.addElement(seatDetail);

                sectionComboBox.removeItem(selectedSection);
            });

            removeSeatButton.addActionListener(ev -> {
                String selectedSeat = seatsList.getSelectedValue();
                if (selectedSeat != null) {
                    seatsListModel.removeElement(selectedSeat);
                }
            });

            JLabel sectionLabel = new JLabel("Section:");
            JLabel sizeLabel = new JLabel("Size:");
            JLabel rowLabel = new JLabel("Row/Cabin:");

            JPanel seatConfigPanel = new JPanel();
            seatConfigPanel.add(sectionLabel);
            seatConfigPanel.add(sectionComboBox);
            seatConfigPanel.add(sizeLabel);
            seatConfigPanel.add(sizeComboBox);
            seatConfigPanel.add(rowLabel);
            seatConfigPanel.add(rowSpinner);
            seatConfigPanel.add(addSeatButton);
            seatConfigPanel.add(removeSeatButton);
            seatConfigPanel.add(new JScrollPane(seatsList));

            JPanel PAN2 = new JPanel(new GridLayout(2, 2));
            PAN2.add(seatConfigPanel);

            Object[] message = {
                    "ID (no iata):", idField,
                    "Price:", priceField,
                    "Company:", companyComboBox,
                    "Vehicle:", vehicleField,
                    "Localisations:", PAN,
                    "Start Date:", startDateSpinner,
                    "End Date:", endDateSpinner,
                    "Seats:", PAN2,
            };

            while (true) {
                int option = JOptionPane.showConfirmDialog(null, message, "Enter Voyage Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (option == JOptionPane.OK_OPTION) {
                    String id = idField.getText().trim();
                    Integer price = (Integer) priceField.getValue();
                    String company = (String) companyComboBox.getSelectedItem();
                    String vehicle = vehicleField.getText().trim();

                    if (id.isEmpty() || company == null || vehicle.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "All fields must be filled out.");
                        continue;
                    }
                    Date startDate = (Date) startDateSpinner.getValue();
                    Date endDate = (Date) endDateSpinner.getValue();
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(startDate);
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(endDate);
                    if (selectedLocsModel.isEmpty() || seatsListModel.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Please select at least one location and one seat configuration.");
                        continue;
                    }

                    try {
                        List<AbstractLocalisation> selectedLocs = new ArrayList<>();
                        for (int i = 0; i < selectedLocsModel.size(); i++) {
                            selectedLocs.add(modelLoc.getLocalisationById(selectedLocsModel.getElementAt(i)));
                        }
                        List<Calendar> date = new ArrayList<>();
                        date.add(startCal);
                        date.add(endCal);
                        List<List<Seat>> selectedSeats = new ArrayList<>();
                        for (int i = 0; i < seatsListModel.size(); i++) {
                            List<Seat> sectionSeat = new ArrayList<>();
                            List<String> sectionValue = Arrays.asList(seatsListModel.getElementAt(i).split("-"));
                            for (int x = 0; x < (Integer.parseInt(sectionValue.get(2))*Taille.valueOf(sectionValue.get(1)).getValue()); x++){
                                switch (type) {
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
                            selectedSeats.add(sectionSeat);
                        }

                        Command addCommand = new AddVoyageCommand(factoryVoy, type, modelCom.getCompanyById(company).getIata() + id.toUpperCase(), price, modelCom.getCompanyById(company),
                                vehicle, selectedLocs, date, selectedSeats);

                        addCommand.execute();
                        lastCommand.set(addCommand);
                        break;
                    } catch (IllegalArgumentException err) {
                        JOptionPane.showMessageDialog(null, err.getMessage());
                    }
                } else {
                    break;
                }
            }
        });

        updateButton.addActionListener(e -> {
            JTextField idField = new JTextField();
            JSpinner priceField = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));

            JTextField vehicleField = new JTextField();

            DefaultListModel<String> availableLocsModel = new DefaultListModel<>();
            modelLoc.getLocalisations().forEach(loc -> {
                if (loc.getType() == type) {
                    availableLocsModel.addElement(loc.getId());
                }
            });            JList<String> availableLocsList = new JList<>(availableLocsModel);
            availableLocsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            JButton addLocButton = new JButton(">>");
            JButton removeLocButton = new JButton("<<");
            DefaultListModel<String> selectedLocsModel = new DefaultListModel<>();
            JList<String> selectedLocsList = new JList<>(selectedLocsModel);

            addLocButton.addActionListener(ev -> {
                availableLocsList.getSelectedValuesList().forEach(selectedLocsModel::addElement);
            });

            removeLocButton.addActionListener(ev -> {
                selectedLocsList.getSelectedValuesList().forEach(selectedLocsModel::removeElement);
            });

            JPanel PAN = new JPanel(new GridLayout(1, 2));
            PAN.add(new JScrollPane(availableLocsList));
            JPanel buttonsPanel = new JPanel(new GridLayout(2, 1));
            buttonsPanel.add(addLocButton);
            buttonsPanel.add(removeLocButton);
            PAN.add(buttonsPanel);
            PAN.add(new JScrollPane(selectedLocsList));

            JSpinner startDateSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner endDateSpinner = new JSpinner(new SpinnerDateModel());
            startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy.MM.dd:HH.mm"));
            endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy.MM.dd:HH.mm"));

            Object[] message = {
                    "ID (with iata):", idField,
                    "Price:", priceField,
                    "Vehicle:", vehicleField,
                    "Localisations:", PAN,
                    "Start Date (if no change keep the same):", startDateSpinner,
                    "End Date (if no change keep the same):", endDateSpinner,
            };

            while (true) {
                int option = JOptionPane.showConfirmDialog(null, message, "Enter Voyage Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (option == JOptionPane.OK_OPTION) {
                    String id = idField.getText().trim();
                    Integer price = (Integer) priceField.getValue();
                    String vehicle = vehicleField.getText().trim();

                    if (id.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "ID must be filled out.");
                        continue;
                    }
                    Date startDate = (Date) startDateSpinner.getValue();
                    Date endDate = (Date) endDateSpinner.getValue();
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(startDate);
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(endDate);

                    try {
                        List<AbstractLocalisation> selectedLocs = new ArrayList<>();
                        for (int i = 0; i < selectedLocsModel.size(); i++) {
                            selectedLocs.add(modelLoc.getLocalisationById(selectedLocsModel.getElementAt(i)));
                        }
                        List<Calendar> date = new ArrayList<>();
                        date.add(startCal);
                        date.add(endCal);


                        Command addCommand = new UpdateVoyageCommand(factoryVoy, id.toUpperCase(), price, vehicle, selectedLocs, date);

                        addCommand.execute();
                        lastCommand.set(addCommand);
                        break;
                    } catch (IllegalArgumentException err) {
                        JOptionPane.showMessageDialog(null, err.getMessage());
                    }
                } else {
                    break;
                }
            }
        });

        deleteButton.addActionListener(e -> {
            JTextField idField = new JTextField();
            Object[] message = {
                    "ID:", idField
            };

            while (true) {
                int option = JOptionPane.showConfirmDialog(null, message, "Enter Voyage ID", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String id = idField.getText().trim();

                    if (!id.isEmpty()) {
                        try {
                            Command addCommand = new DeleteVoyageCommand(factoryVoy, id.toUpperCase());
                            addCommand.execute();
                            lastCommand.set(addCommand);
                        } catch (IllegalArgumentException err) {
                            JOptionPane.showMessageDialog(null, err.getMessage());
                        }
                        break;
                    } else {
                        JOptionPane.showMessageDialog(null, "No empty ID");
                    }
                } else {
                    break;
                }
            }
        });

        undoButton.addActionListener(e -> {
            if (lastCommand.get() != null) {
                lastCommand.get().undo();
                lastCommand.set(null);
            }
        });

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.add(addButton);
        buttonsPanel.add(updateButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(undoButton);

        panel.add(buttonsPanel, BorderLayout.SOUTH);
    }
}
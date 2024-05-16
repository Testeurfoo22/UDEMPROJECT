import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

interface LocalisationVisitor {
    String visit(Airport airport);
    String visit(Port port);
    String visit(Station station);
}

class DisplayLocalisationVisitor implements LocalisationVisitor {
    @Override
    public String visit(Airport airport) {
        return String.format("%s / %s / %s", airport.getId(), airport.getName(), airport.getCity());
    }
    @Override
    public String visit(Port port) {
        return String.format("%s / %s / %s", port.getId(), port.getName(), port.getCity());
    }
    @Override
    public String visit(Station station) {
        return String.format("%s / %s / %s", station.getId(), station.getName(), station.getCity());
    }
}

interface CompanyVisitor {
    String visit(AirCompany airCompany);
    String visit(CruiseCompany cruiseCompany);
    String visit(TrainCompany trainCompany);
}

class DisplayCompanyVisitor implements CompanyVisitor {
    @Override
    public String visit(AirCompany airCompany) {
        return String.format("%s / %s / %s", airCompany.getId(), airCompany.getIata(), airCompany.getName());
    }
    @Override
    public String visit(CruiseCompany cruiseCompany) {
        return String.format("%s / %s / %s", cruiseCompany.getId(), cruiseCompany.getIata(), cruiseCompany.getName());
    }
    @Override
    public String visit(TrainCompany trainCompany) {
        return String.format("%s / %s / %s", trainCompany.getId(), trainCompany.getIata(), trainCompany.getName());
    }
}

interface VoyageVisitor {
    String visit(Flight flight, section section);
    String visit(Cruise cruise, section section);
    String visit(Train train, section section);
}

class DisplayVoyageAdminVisitor implements VoyageVisitor {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd:HH.mm");

    @Override
    public String visit(Flight flight, section section) {
        return formatVoyage(flight);
    }

    @Override
    public String visit(Cruise cruise, section section) {
        return formatVoyage(cruise);
    }

    @Override
    public String visit(Train train, section section) {
        return formatVoyage(train);
    }

    private String formatVoyage(AbstractVoyage voyage) {
        List<String> locationIds = voyage.getLocalisations().stream()
                .map(AbstractLocalisation::getId)
                .collect(Collectors.toList());
        String localisationStr = String.join("-", locationIds);

        String companyStr = String.format("[%s]%s",
                voyage.getCompany().getId(),
                voyage.getId());

        String dateStr = String.format("%s-%s",
                dateFormat.format(voyage.getDates().get(0).getTime()),
                dateFormat.format(voyage.getDates().get(1).getTime()));

        String seatStr = voyage.getSeats().stream()
                .map(seatList -> formatSeatSection(seatList, voyage.getPrice()))
                .collect(Collectors.joining("|"));

        return String.format("%s:%s(%s)|%s", localisationStr, companyStr, dateStr, seatStr);
    }

    private String formatSeatSection(List<Seat> seats, Integer basePrice) {
        if (seats.isEmpty()) return "";

        section sectionId = seats.get(0).getSection();
        Taille sectionTaille = seats.get(0).getTaille();
        long countTotal = seats.size();
        long countBooked = seats.stream()
                .filter(seat -> seat.getState() instanceof BookedState || seat.getState() instanceof PaidState)
                .count();

        // Calculate the adjusted price for each seat, then average them
        double priceAverage = seats.stream()
                .mapToDouble(seat -> (double)basePrice * ((double)seat.getSection().getValue() / 100.0))
                .average()
                .orElse(0.0);
        if (sectionTaille == Taille.NULL){
            return String.format("%s(%d/%d)%.2f", sectionId.getID(), countBooked, countTotal, priceAverage);
        }
        return String.format("%s(%d/%d)%.2f", sectionId.getID()+sectionTaille.getID(), countBooked, countTotal, priceAverage);
    }
}
class DisplayVoyageClientVisitor implements VoyageVisitor {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd:HH.mm");

    @Override
    public String visit(Flight flight, section section) {
        return formatVoyage(flight, section);
    }

    @Override
    public String visit(Cruise cruise, section section) {
        return formatVoyage(cruise, section);
    }

    @Override
    public String visit(Train train, section section) {
        return formatVoyage(train, section);
    }

    private String formatVoyage(AbstractVoyage voyage, section section) {
        List<String> locationIds = voyage.getLocalisations().stream()
                .map(AbstractLocalisation::getId)
                .collect(Collectors.toList());
        String localisationStr = String.join("-", locationIds);

        String companyStr = String.format("[%s]%s", voyage.getCompany().getId(), voyage.getId());

        String dateStr = String.format("%s-%s",
                dateFormat.format(voyage.getDates().get(0).getTime()),
                dateFormat.format(voyage.getDates().get(1).getTime()));

        String seatStr = voyage.getSeats().stream()
                .flatMap(List::stream)
                .filter(seat -> section == null || seat.getSection().equals(section))
                .collect(Collectors.groupingBy(Seat::getSection))
                .entrySet().stream()
                .map(entry -> formatSeatSection(entry.getValue(), voyage.getPrice()))
                .collect(Collectors.joining("|"));

        return String.format("%s:%s(%s)|%s", localisationStr, companyStr, dateStr, seatStr);
    }

    private String formatSeatSection(List<Seat> seats, Integer basePrice) {
        if (seats.isEmpty()) return "";

        section sectionId = seats.get(0).getSection();
        long countTotal = seats.size();
        long countBooked = seats.stream()
                .filter(seat -> seat.getState() instanceof BookedState || seat.getState() instanceof PaidState)
                .count();

        double priceAverage = seats.stream()
                .mapToDouble(seat -> (double)basePrice * ((double)seat.getSection().getValue() / 100.0))
                .average()
                .orElse(0.0);

        return String.format("%.2f|%s%s", priceAverage, sectionId.getID(), countTotal - countBooked);
    }
}
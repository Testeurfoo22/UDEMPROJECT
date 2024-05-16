public enum TravelType {FlightType, CruiseType, TrainType}
interface section{
    String getID();
    int getValue();
}
enum PlaneSection implements section{
    PREMIERE("F", 100),
    AFFAIRE("A", 75),
    PREMIUM("P", 60),
    ECO("E", 50);

    private final String code;
    private final int value;

    PlaneSection(String code, int value) {
        this.code = code;
        this.value = value;
    }

    public String getID() {
        return code;
    }

    public int getValue() {
        return value;
    }
}
enum CruiseSection implements section{
    DELUXE("D", 100),
    FAMILLE("F", 90),
    SUITE("S", 90),
    OCEAN("O", 75),
    INTERIEUR("I", 50);

    private final String code;
    private final int value;

    CruiseSection(String code, int value) {
        this.code = code;
        this.value = value;
    }

    public String getID() {
        return code;
    }

    public int getValue() {
        return value;
    }
}
enum TrainSection implements section{
    PREMIERE("P", 100),
    ECO("E", 50);

    private final String code;
    private final int value;

    TrainSection(String code, int value) {
        this.code = code;
        this.value = value;
    }

    public String getID() {
        return code;
    }

    public int getValue() {
        return value;
    }
}
enum Taille {
    ETROIT("S", 3),
    CONFORT("C", 4),
    MOYEN("M", 6),
    LARGE("L", 10),
    NULL(null, 1);

    private final String code;
    private final Integer value;

    Taille(String code, Integer value) {
        this.code = code;
        this.value = value;
    }

    public String getID() {
        return code;
    }

    public int getValue() {
        return value;
    }
}
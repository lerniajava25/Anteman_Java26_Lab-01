package elpriser;

import elpriser.model.QuarterlyData;
import elpriser.services.ApiService;
import elpriser.services.PriceAnalyzer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Main {
    private static String currentZone = "SE2";
    private static final LocalDate today = LocalDate.now();
    private static final DateTimeFormatter clockFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static QuarterlyData[] currentPrices = null;

    static void main() {

        boolean isRunning = true;

        loadZonePrices(currentZone);

        while (isRunning) {
            IO.println(today + " @ " + LocalTime.now().format(clockFormatter));
            IO.println("Nuvarande valt elområde: " + currentZone);
            showMenu();
            String userChoice = IO.readln("Välj ett alternativ: ").trim();

            switch (userChoice.toLowerCase()) {
                case "1" -> zoneChoice(); //Välj elområde
                case "2" -> {
                    if (checkDataIsLoaded()) PriceAnalyzer.viewMinMaxAv(today, currentPrices);
                }
                case "3" -> {
                    if (checkDataIsLoaded()) PriceAnalyzer.sortPrices(currentPrices);
                }
                case "4" -> {
                    if (checkDataIsLoaded()) PriceAnalyzer.bestChargePeriod(currentPrices);
                    }
                case "e" -> isRunning = false;
                default -> IO.println("Felaktigt val! Vänligen välj igen...");
            }
        }
    }
    private static void showMenu() {
        IO.println("*==========================*");
        IO.println("| Elpriser - Analysverktyg |");
        IO.println("*==========================*" + "\n");
        IO.println("1. Välj elområde (SE1, SE2, SE3, SE4)");
        IO.println("2. Min, Max och Medelpris");
        IO.println("3. Sortera priser (lägst till högst)");
        IO.println("4. Bästa laddningstid (4h sammanhängande)");
        IO.println("e. Avsluta");
    }

    private static void loadZonePrices(String zoneToLoad) {
        try {
            currentPrices = ApiService.getQuarterlyData(today, zoneToLoad);
        } catch (Exception e) {
            currentPrices = null;
            IO.println("Kunde inte hämta priser. Statuskod: " + e.getMessage());
        }


    }

    private static void zoneChoice() {
        IO.println("Nuvarande elområde: " + currentZone);
        String chosenZone;
        do {
            chosenZone = IO.readln("Välj elområde (SE1, SE2, SE3, SE4): ").trim().toUpperCase();
            if (!chosenZone.matches("SE1|SE2|SE3|SE4")) {
                IO.println("Felaktigt format! Måste anges som SE1, SE2, SE3 eller SE4...");
            }
        } while (!chosenZone.matches("SE1|SE2|SE3|SE4"));
        IO.println("Valt elområde: " + chosenZone);
        currentZone = chosenZone;
        loadZonePrices(currentZone);
    }

    private static boolean checkDataIsLoaded() {
        if (currentPrices == null || currentPrices.length == 0) {
            IO.println("Ingen data tillgänglig för nuvarande elområde.");
            return false;
        }
        return true;
    }
}

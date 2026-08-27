package elpriser;

import elpriser.model.HourlyData;
import elpriser.services.ApiService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

public class Main {
    private static String currentZone = "SE2";
    private static final LocalDate today = LocalDate.now();
    private static final LocalTime currentTime = LocalTime.now();
    private static final DateTimeFormatter clockFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static HourlyData[] currentPrices = null;

    static void main() {

        boolean isRunning = true;

        loadZonePrices(currentZone);

        while (isRunning) {
            IO.println(today + " @ " + currentTime.format(clockFormatter));
            IO.println("Nuvarande valt elområde: " + currentZone);
            showMenu();
            String userChoice = IO.readln("Välj ett alternativ: ").trim();

            switch (userChoice.toLowerCase()) {
                case "1" -> zoneChoice(); //Välj elområde
                case "2" -> {
                    if (checkDataIsLoaded()) viewMinMaxAv(currentPrices);
                }
                case "3" -> {
                    if (checkDataIsLoaded()) sortPrices(currentPrices);
                }
                case "4" -> {
                    if (checkDataIsLoaded()) bestChargePeriod(currentPrices);
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

    private static void loadZonePrices(String chosenZone) {
        try {
            currentPrices = ApiService.getHourlyData(today, currentZone);
        } catch (Exception e) {
            IO.println("Kunde inte hämta priser. Statuskod: " + e.getMessage());
        }


    }

    private static void zoneChoice() {
        IO.println("Nuvarande elområde: " + currentZone);
        String chosenZone = IO.readln("Välj elområde (SE1, SE2, SE3, SE4): ").trim().toUpperCase();
        if (chosenZone.matches("SE1|SE2|SE3|SE4")) {
            IO.println("Valt elområde: " + chosenZone);
            currentZone = chosenZone;
            loadZonePrices(currentZone);
        } else {
            IO.println("Felaktigt format! Måste anges som SE1, SE2, SE3 eller SE4...");
            zoneChoice();
        }
    }

    private static void viewMinMaxAv(HourlyData[] prices) {
        if (prices == null || prices.length == 0) return;

        HourlyData min = prices[0];
        HourlyData max = prices[0];
        double total = 0;

        for (HourlyData h : prices) {
            if (h.getSekPerKwh() < min.getSekPerKwh()) min = h;
            if (h.getSekPerKwh() > max.getSekPerKwh()) max = h;
            total += h.getSekPerKwh();
        }

        double avg = total / prices.length;

        IO.println("\n" + "--- Prisanalys för " + today + " ---");
        IO.println(String.format("Lägsta pris: %s (%.2f kr/kWh)", min.getHourFormatted(),  min.getSekPerKwh()));
        IO.println(String.format("Högsta pris: %s (%.2f kr/kWh)", max.getHourFormatted(),  max.getSekPerKwh()));
        IO.println(String.format("Genomsnittspris: %.2f kr/kWh\n\n", avg));
    }

    private static void sortPrices(HourlyData[] prices) {
        if (prices == null || prices.length == 0) return;
        HourlyData[] sortedPrices = prices.clone();
        Arrays.sort(sortedPrices, Comparator.comparingDouble(HourlyData::getSekPerKwh));

        IO.println("\n" + "--- Priser sorterade från lägst till högst ---");
        for (HourlyData h : sortedPrices) {
            IO.println(h.getSekPerKwh() + " kr/kWh - kl. " + h.getHourFormatted());
        }
        IO.println("*=----------**----------=*\n\n");
    }

    private static void bestChargePeriod(HourlyData[] prices) {
        int windowSize = 16;
        if (prices == null || prices.length < windowSize) {
            IO.println("För lite data för att beräkna laddning i 4 timmar");
            return;
        }

        double currentWindowSum = 0;
        for (int i = 0; i < windowSize; i++) {
            currentWindowSum += prices[i].getSekPerKwh();
        }

        double minSum = currentWindowSum;
        int bestStartIndex = 0;

        for (int i = windowSize; i < prices.length; i++) {
            currentWindowSum += prices[i].getSekPerKwh() - prices[i - windowSize].getSekPerKwh();
            if (currentWindowSum < minSum) {
                minSum = currentWindowSum;
                bestStartIndex = i - windowSize + 1;
            }
        }

        double avgWindowCost = minSum / windowSize;
        HourlyData startindex = prices[bestStartIndex];

        IO.println("\n" +
                "--- Bästa laddningstid, 4 timmar sammanhängande ---");
        IO.println(String.format("Starttid: %s", startindex.getHourFormatted()));
        IO.println(String.format("Medelpris: %.2f kr/kWh", avgWindowCost));
        IO.println(String.format("Totalpris 4 timmar: %.2f kr vid förbrukning 1 kW/h \n\n", (minSum * 0.25)));
    }

    private static boolean checkDataIsLoaded() {
        if (currentPrices == null || currentPrices.length == 0) {
            IO.println("Ingen data tillgänglig för nuvarande elområde.");
            return false;
        }
        return true;
    }
}

package elpriser.services;

import elpriser.model.QuarterlyData;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;

public class PriceAnalyzer {
    public static void viewMinMaxAv(LocalDate analyzedDate, QuarterlyData[] prices) {
        if (prices == null || prices.length == 0) return;

        QuarterlyData min = prices[0];
        QuarterlyData max = prices[0];
        double total = 0;

        for (QuarterlyData h : prices) {
            if (h.getSekPerKwh() < min.getSekPerKwh()) min = h;
            if (h.getSekPerKwh() > max.getSekPerKwh()) max = h;
            total += h.getSekPerKwh();
        }

        double avg = total / prices.length;

        IO.println("\n" + "--- Prisanalys för " + analyzedDate + " ---");
        IO.println(String.format("Lägsta pris: %s (%.2f kr/kWh)", min.getHourFormatted(),  min.getSekPerKwh()));
        IO.println(String.format("Högsta pris: %s (%.2f kr/kWh)", max.getHourFormatted(),  max.getSekPerKwh()));
        IO.println(String.format("Genomsnittspris: %.2f kr/kWh\n\n", avg));
    }

    public static void sortPrices(QuarterlyData[] prices) {
        if (prices == null || prices.length == 0) return;
        QuarterlyData[] sortedPrices = prices.clone();
        Arrays.sort(sortedPrices, Comparator.comparingDouble(QuarterlyData::getSekPerKwh));

        IO.println("\n" + "--- Priser sorterade från lägst till högst ---");
        for (QuarterlyData h : sortedPrices) {
            IO.println(h.getSekPerKwh() + " kr/kWh - kl. " + h.getHourFormatted());
        }
        IO.println("*=----------**----------=*\n\n");
    }

    public static void bestChargePeriod(QuarterlyData[] prices) {
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
        QuarterlyData startindex = prices[bestStartIndex];

        IO.println("\n" +
                "--- Bästa laddningstid, 4 timmar sammanhängande ---");
        IO.println(String.format("Starttid: %s", startindex.getHourFormatted()));
        IO.println(String.format("Medelpris: %.2f kr/kWh", avgWindowCost));
        IO.println(String.format("Totalpris 4 timmar: %.2f kr vid förbrukning 1 kW/h \n\n", (minSum * 0.25)));
    }
}

package elpriser.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import elpriser.model.HourlyData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public class ApiService {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CACHE_DIR = "cache";

    private ApiService() {}

    public static HourlyData[] getHourlyData(LocalDate date, String zone) throws IOException, InterruptedException {
        String year = String.valueOf(date.getYear());
        String month = String.format("%02d", date.getMonthValue());
        String day = String.format("%02d", date.getDayOfMonth());

        Path pathToCache = Path.of(CACHE_DIR, String.format("%s_%s-%s_%s.json",year,month,day,zone));

        if (Files.exists(pathToCache)) {
            String hourlyRatesJson = Files.readString(pathToCache);
            return objectMapper.readValue(hourlyRatesJson, new TypeReference<HourlyData[]>() {});
        }

        String url = String.format("https://www.elprisetjustnu.se/api/v1/prices/%s/%s-%s_%s.json", year, month, day, zone);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Kunde inte hämta data från API. Statuskod: " + response.statusCode());

        }

        String responseBody = response.body();
        saveToCache(pathToCache, responseBody);
        return objectMapper.readValue(responseBody, new TypeReference<HourlyData[]>() {});
    }

    private static void saveToCache(Path path, String content) {
        try {
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, content);
            IO.println("Data sparat till lokal fil: " + path);
        } catch (IOException e) {
            IO.println("[Varning!] Kunde inte sparat till lokal fil: " + e.getMessage());
        }
    }
}

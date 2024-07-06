package org.example.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.example.model.Url;
import org.example.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    private static final String BASE_URL = "http://localhost:8080/";
    private static final String CSV_FILE = "src/main/resources/urls.csv";

    private Map<String, String> urlMap = new HashMap<>();

    public UrlService() {
        loadUrlsFromCsv();
    }

    private void loadUrlsFromCsv() {
        try (CSVReader csvReader = new CSVReader(new FileReader(CSV_FILE))) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                String shortUrl = values[0];
                String destinationUrl = values[1];
                urlMap.put(shortUrl, destinationUrl);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public String shortenUrl(String destinationUrl) {
        String shortUrl = BASE_URL + generateShortUrl();
        urlMap.put(shortUrl, destinationUrl);
        // Saving to repository for persistence
        Url url = new Url(shortUrl, destinationUrl, LocalDateTime.now().plusMonths(10));
        urlRepository.save(url);
        return shortUrl;
    }

    public boolean updateShortUrl(String shortUrl, String newDestinationUrl) {
        if (urlMap.containsKey(shortUrl)) {
            urlMap.put(shortUrl, newDestinationUrl);
            // Updating in repository for persistence
            Url url = urlRepository.findByShortUrl(shortUrl).orElseThrow();
            url.setDestinationUrl(newDestinationUrl);
            urlRepository.save(url);
            return true;
        }
        return false;
    }

    public String getFullUrl(String shortenString) {
        String shortUrl = BASE_URL + shortenString;
        if (urlMap.containsKey(shortUrl)) {
            return urlMap.get(shortUrl);
        }
        throw new NoSuchElementException("URL not found");
    }

    public boolean updateExpiry(String shortUrl, int daysToAdd) {
        Url url = urlRepository.findByShortUrl(shortUrl).orElseThrow();
        url.setExpiryDate(url.getExpiryDate().plusDays(daysToAdd));
        urlRepository.save(url);
        return true;
    }

    private String generateShortUrl() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}

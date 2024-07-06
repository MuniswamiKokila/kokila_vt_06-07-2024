package org.example.controller;

import org.example.model.Url;
import org.example.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.NoSuchElementException;

@RestController
public class UrlController {

    @Autowired
    private UrlService urlService;

    @PostMapping("/shorten")
    public String shortenUrl(@RequestBody String destinationUrl) {
        return urlService.shortenUrl(destinationUrl);
    }

    @PostMapping("/update")
    public boolean updateShortUrl(@RequestParam String shortUrl, @RequestParam String newDestinationUrl) {
        return urlService.updateShortUrl(shortUrl, newDestinationUrl);
    }

    @GetMapping("/{shortenString}")
    public void redirectToFullUrl(HttpServletResponse response, @PathVariable String shortenString) {
        try {
            String fullUrl = urlService.getFullUrl(shortenString);
            response.sendRedirect(fullUrl);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Url not found", e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not redirect to the full url", e);
        }
    }

    @PostMapping("/update-expiry")
    public boolean updateExpiry(@RequestParam String shortUrl, @RequestParam int daysToAdd) {
        return urlService.updateExpiry(shortUrl, daysToAdd);
    }
}

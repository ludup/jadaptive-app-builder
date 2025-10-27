package com.jadaptive.app.ip;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.ip.IPStackConfiguration;
import com.jadaptive.utils.Utils;

/**
 * A simple Java program to find the closest IP address from a list using the IPstack API.
 * This program demonstrates how to fetch geolocation data and calculate distances asynchronously.
 */
@Component
public class ClosestIpFinder {

	static Logger log = LoggerFactory.getLogger(ClosestIpFinder.class);
	
	@Autowired
	private SingletonObjectDatabase<IPStackConfiguration> config;
    
    public String getClosestIPAddress(String SOURCE_IP, List<String> TARGET_IPS ) {
        try {
            CompletableFuture<IpLocation> sourceFuture = getIpLocationAsync(SOURCE_IP);
   
           List<CompletableFuture<IpLocation>> targetFutures = TARGET_IPS.stream()
                    .map(this::getIpLocationAsync)
                    .collect(Collectors.toList());

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    targetFutures.toArray(new CompletableFuture[0])
            );
            
            MutableObject<String> res = new MutableObject<>();

            allFutures.thenRun(() -> {
                try {
                    IpLocation sourceLocation = sourceFuture.get();
                    if (sourceLocation == null) {
                        log.error("Could not find IP record for source location {}", SOURCE_IP);
                        return;
                    }

                    String closestIp = null;
                    double minDistance = Double.MAX_VALUE;

                    if(log.isDebugEnabled()) {
                    	log.debug("Calculating IP distances");
                    }
                    for (int i = 0; i < TARGET_IPS.size(); i++) {
                        String targetIp = TARGET_IPS.get(i);
                        IpLocation targetLocation = targetFutures.get(i).get();
                        
                        if (targetLocation != null) {
                        	log.debug("IP {} is located in {}/{} ", 
                        			targetLocation.ip,
                        			targetLocation.city, 
                        			targetLocation.country);
                        	
                            double distance = haversine(
                                    sourceLocation.latitude, sourceLocation.longitude,
                                    targetLocation.latitude, targetLocation.longitude
                            );
                            log.debug(String.format("Distance to %s: %.2f km\n", targetIp, distance));

                            if (distance < minDistance) {
                                minDistance = distance;
                                closestIp = targetIp;
                            }
                        }
                    }

                    res.setValue(closestIp);

                } catch (InterruptedException | ExecutionException e) {
                    log.error("An error occurred while processing results", e);
                }
            }).join(); // Wait for the final result to be processed

            return res.getValue();
        } catch (Exception e) {
            log.error("An unexpected error occurred", e);
        }
        
        throw new IllegalStateException(String.format("The closest IP address to %s from %s could not be determined",
        		SOURCE_IP,
        		Utils.csv(TARGET_IPS)));
    }

    /**
     * Fetches geolocation data for a given IP address using the IPstack API asynchronously.
     * @param ipAddress The IP address to look up.
     * @return A CompletableFuture that will contain an IpLocation object, or null if the request fails.
     */
    private CompletableFuture<IpLocation> getIpLocationAsync(String ipAddress) {
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("http://api.ipstack.com/%s?access_key=%s", ipAddress, config.getObject(IPStackConfiguration.class).getApiKey());
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        Gson gson = new Gson();
                        return gson.fromJson(response.body(), IpLocation.class);
                    } else {
                        log.error("API request failed for " + ipAddress + " with status code " + response.statusCode());
                        log.error("Response body: " + response.body());
                        return null;
                    }
                })
                .exceptionally(e -> {
                    log.error("An error occurred during async API call for " + ipAddress + ": " + e.getMessage());
                    return null;
                });
    }

    /**
     * Calculates the great-circle distance between two points on the Earth
     * (specified in decimal degrees) using the Haversine formula.
     * @param lat1 Latitude of the first point.
     * @param lon1 Longitude of the first point.
     * @param lat2 Latitude of the second point.
     * @param lon2 Longitude of the second point.
     * @return The distance in kilometers.
     */
    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        // Radius of the Earth in kilometers
        final int R = 6371;
        
        // Convert degrees to radians
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        // Apply the Haversine formula
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in kilometers
    }

    /**
     * A simple class to represent the IPstack API response for location data.
     */
    private static class IpLocation {
        @SerializedName("ip")
        String ip;
        @SerializedName("country_name")
        String country;
        @SerializedName("city")
        String city;
        @SerializedName("latitude")
        double latitude;
        @SerializedName("longitude")
        double longitude;
        @SerializedName("success")
        boolean success;
        @SerializedName("error")
        ApiError error;
    }

    /**
     * A simple class to represent an error response from the IPstack API.
     */
    private static class ApiError {
        @SerializedName("code")
        int code;
        @SerializedName("info")
        String info;
    }

}


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class EarthquakeTracker {

    private static final String API_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.print("Enter the country: ");
            String country = reader.readLine();

            System.out.print("Enter the number of days: ");
            int days = Integer.parseInt(reader.readLine());

            LocalDate startDate = LocalDate.now().minusDays(days);
            LocalDate endDate = LocalDate.now();

            String startDateString = startDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
            String endDateString = endDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT));

            String apiUrl = API_URL + "?format=geojson&starttime=" + startDateString + "&endtime=" + endDateString;

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader apiReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = apiReader.readLine()) != null) {
                    response.append(line);
                }
                apiReader.close();

                List<Earthquake> earthquakes = parseEarthquakeData(response.toString(), country);
                if (earthquakes.isEmpty()) {
                    System.out.println("No earthquakes were recorded in the past " + days + " days.");
                } else {
                    for (Earthquake earthquake : earthquakes) {
                        System.out.println(earthquake);
                    }
                }
            } else {
                System.out.println("Error: HTTP response code - " + responseCode);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while retrieving earthquake data: " + e.getMessage());
        }
    }

    private static List<Earthquake> parseEarthquakeData(String jsonData, String country) {
    	Gson gson = new Gson();
        EarthquakeData earthquakeData = gson.fromJson(jsonData, EarthquakeData.class);

        List<Earthquake> earthquakes = new ArrayList<>();
        List<Feature> features = earthquakeData.getFeatures();

        for (Feature feature : features) {
            Properties properties = feature.getProperties();
            String place = properties.getPlace();
            double magnitude = properties.getMag();
            String dateTime = properties.getTime();

            Earthquake earthquake = new Earthquake(country, place, magnitude, dateTime);
            earthquakes.add(earthquake);
        }

        return earthquakes;
    }

    private static class Earthquake {
        private String country;
        private String place;
        private double magnitude;
        private String dateTime;

        public Earthquake(String country, String place, double magnitude, String dateTime) {
            this.country = country;
            this.place = place;
            this.magnitude = magnitude;
            this.dateTime = dateTime;
        }

        @Override
        public String toString() {
            return "Country: " + country + "\n" +
                    "Place: " + place + "\n" +
                    "Magnitude: " + magnitude + "\n" +
                    "Date and Time: " + dateTime + "\n";
        }
    }

    private static class EarthquakeData {
        private List<Feature> features;

        public List<Feature> getFeatures() {
            return features;
        }
    }

    private static class Feature {
        private Properties properties;

        public Properties getProperties() {
            return properties;
        }
    }

    private static class Properties {
        private String place;
        private double mag;
        private String time;

        public String getPlace() {
            return place;
        }

        public double getMag() {
            return mag;
        }

        public String getTime() {
            return time;
        }
    }
}


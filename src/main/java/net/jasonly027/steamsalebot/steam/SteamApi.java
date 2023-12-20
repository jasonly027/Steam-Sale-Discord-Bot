package net.jasonly027.steamsalebot.steam;

/*
Importing steam API
 */
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
public class SteamApi {

    /*APP POJO*/
    public static class AppInfo {
        final String BANNER;
        final int OG_PRICE;
        final int SALE_PRICE;
        final int DISCOUNT_PERCENT;
        final int REVIEW_COUNT;
        final String LINK;

        public AppInfo(String BANNER, int OG_PRICE, int SALE_PRICE, int DISCOUNT_PERCENT, int REVIEW_COUNT, String LINK) {
            this.BANNER = BANNER;
            this.OG_PRICE = OG_PRICE;
            this.SALE_PRICE = SALE_PRICE;
            this.DISCOUNT_PERCENT = DISCOUNT_PERCENT;
            this.REVIEW_COUNT = REVIEW_COUNT;
            this.LINK = LINK;
        }
    }

    /*Takes in App ID of steam game and returns its information as AppInfo POJO*/
    public static AppInfo getAPPInfo(long APP_ID) {
        String APP_URL = "https://store.steampowered.com/api/appdetails?appids=" + APP_ID;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APP_URL))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            //Convert response body to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.body());
            String STRING_APPID = String.valueOf(APP_ID);

            if(jsonNode.get(STRING_APPID).get("success").asBoolean()){
                String banner = jsonNode.get(STRING_APPID).get("data").get("header_image").toString();
                int reviewCount = jsonNode.get(STRING_APPID).get("data").get("recommendations").get("total").asInt();

                int ogPrice = 0;
                int discountPrice = 0;
                int discountPercent = 0;
                //free vs not free
                if(!jsonNode.get(STRING_APPID).get("data").get("is_free").asBoolean()) {
                    ogPrice = jsonNode.get(STRING_APPID).get("data").get("price_overview").get("initial").asInt();
                    discountPrice = jsonNode.get(STRING_APPID).get("data").get("price_overview").get("final").asInt();
                    discountPercent = jsonNode.get(STRING_APPID).get("data").get("price_overview").get("discount_percent").asInt();
                }
                return new AppInfo(banner, ogPrice, discountPrice, discountPercent, reviewCount, APP_URL);

            } else {
                System.out.println("ERROR: INVALID APP ID");
            }

        } catch (Exception e) {
            System.out.println("ERROR: INVALID INPUT");
        }
        return null;
    }

    /*Takes in App ID as input as returns the name of the App as a string.*/
    public static String getName(long APP_ID){
        String APP_URL = "https://store.steampowered.com/api/appdetails?appids=" + APP_ID;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APP_URL))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            //Convert response body to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.body());
            String STRING_APPID = String.valueOf(APP_ID);

            if(jsonNode.get(STRING_APPID).get("success").asBoolean()){
                return jsonNode.get(STRING_APPID).get("data").get("name").toString();
            }
        } catch (Exception e) {
            System.out.println("ERROR: INVALID INPUT");
        }
        return null;
    }

    /*FOR TESTING PURPOSES, feel free to remove*/
    public static void main(String[] arg){
        SteamApi.getAPPInfo(440);
        System.out.println(SteamApi.getName(440));
    }
}

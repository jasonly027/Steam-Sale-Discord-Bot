package net.jasonly027.steamsalebot.steam;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SteamApi {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private SteamApi() {}

    private static HttpClient getClient() {
        return client;
    }

    private static ObjectMapper getMapper() {
        return objectMapper;
    }

    private static HttpRequest createHttpGetRequest(String url) {
        return HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();
    }

    private static HttpResponse<String> sendHttpRequest(HttpRequest request)
            throws  IOException, InterruptedException {
        HttpResponse<String> response = getClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException();
        }
        return response;
    }

    /**
     * Get a POJO of an app's details, specified by <b>appId</b>. If the query was successful,
     * i.e., a valid <b>appId</b> was supplied, the POJO's success field will be true. If the
     * field is false, the other fields will not be set. If the POJO's isFree field is true,
     * the fields related to price will not be set, otherwise they will be set.
     * @param appId id of the app to which details are requested
     * @return a POJO of the app's details
     * @throws IOException If the response status code was not 200, or if an I/O error
     * occurs when sending or receiving
     * @throws InterruptedException if the operation is interrupted
     */
    public static AppInfo getAppInfo(long appId) throws IOException, InterruptedException {
        String appUrl = "https://store.steampowered.com/api/appdetails?appids="
                + appId
                + "&cc=US";
        HttpResponse<String> response = sendHttpRequest(createHttpGetRequest(appUrl));

        return getMapper().readValue(response.body(), AppInfo.class);
    }

    /**
     * Get the app name of the specified <b>appId</b>.
     * @param appId id of the app
     * @return the name of the app or <b>null</b> if either the appId was invalid or
     * the app is free.
     * @throws IOException If the response status code was not 200, or if an I/O error
     * occurs when sending or receiving
     * @throws InterruptedException if the operation is interrupted
     */
    public static String getAppName(long appId) throws IOException, InterruptedException {
        String appUrl = "https://store.steampowered.com/api/appdetails?appids=" + appId;
        HttpResponse<String> response = sendHttpRequest(createHttpGetRequest(appUrl));
        JsonNode root = getMapper().readTree(response.body());
        String appField = root.fieldNames().next();
        root = root.get(appField);

        boolean isSuccess = root.get("success").asBoolean();
        if (!isSuccess) {
            return null;
        }

        JsonNode data = root.get("data");
        boolean isFree = data.get("is_free").asBoolean();
        if (isFree) {
            return null;
        }

        return data.get("name").textValue();
    }
}

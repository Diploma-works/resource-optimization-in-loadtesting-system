package org.ledokol.testrunner.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ledokol.testrunner.exceptions.IncorrectStoppingTestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApiService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    @Value("${main-service.baseurl}")
    private  String BASE_URL;
    //= "http://main-service.default.svc.cluster.local:8080/api";
//    private static final String BASE_URL = "http://localhost:8080/api";

    public String createScript(String scriptJson) throws Exception {
        String url = BASE_URL + "/scripts";
        return postRequest(url, scriptJson);
    }

    public String createTest(String testJson) throws Exception {
        String url = BASE_URL + "/tests";
        return postRequest(url, testJson);
    }

    public String startTest(String testId) throws Exception {
        String url = BASE_URL + "/tests/" + testId + "/run";
        return postRequest(url, ""); // Предполагаем, что тело запроса не требуется
    }

    public String stopTest(String runId) throws IncorrectStoppingTestException  {
        String url = BASE_URL + "/testruns/" + runId + "?action=STOP";
        try {
            return postRequest(url, ""); // Предполагаем, что тело запроса не требуется
        } catch (Exception e) {
            throw new IncorrectStoppingTestException("failed to stop test");
        }
    }

    private String postRequest(String url, String requestBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return response.body();
        } else {
            throw new RuntimeException("Failed to execute request: " + response.body());
        }
    }

    public String startTestWithGenerator(String testId, String generatorId) throws Exception {
        String url = BASE_URL + "/tests/" + testId + "/run/" + generatorId;
        return postRequest(url, ""); // Пустое тело, поскольку для запуска теста данные в теле не требуются
    }

    public List<String> getGenerators() throws Exception {
        String url = BASE_URL + "/generators";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONArray jsonArray = new JSONArray(response.body());
            List<String> generatorIds = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject generatorObj = jsonArray.getJSONObject(i);
                String instanceId = generatorObj.getString("instanceId");
                generatorIds.add(instanceId);
            }
            return generatorIds;
        } else {
            throw new RuntimeException("Failed to get generators: " + response.body());
        }
    }

    public int getGeneratorsAmount() throws Exception {
        String url = BASE_URL + "/generators";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new JSONArray(response.body()).length();
        } else {
            throw new RuntimeException("Failed to get generators: " + response.body());
        }
    }
}

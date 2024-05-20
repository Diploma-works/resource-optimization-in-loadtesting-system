package org.ledokol.testrunner.service.Impl;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ledokol.testrunner.model.PrometheusResponse;
import org.ledokol.testrunner.service.PrometheusService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class PrometheusServiceImpl implements PrometheusService {

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    @Value("${prometheus.url}")
    private String prometheusUrl;

    public PrometheusServiceImpl() {}

    public Double getMeanValueFromQuery(String query, long start, long end, String generatorId){
        try {
            return getMeanValueFromQuery(queryPrometheus(query, start, end, 20), generatorId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Double getMaxValueFromQuery(String query, long start, long end, String generatorId){
        try {
            return getMaxValueFromQuery(queryPrometheus(query, start, end, 20), generatorId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Double getMaxValueFromQuery(PrometheusResponse prometheusResponse, String generatorId){
        for (PrometheusResponse.Result result: prometheusResponse.data.result) {
            List<List<Object>> values = result.values;
            if (generatorId.equals(result.metric.get("pod"))) {
                double max = -1;
                for (List<Object> valuePair : values) {
                    double value = Double.parseDouble((String) valuePair.get(1));
                    if (value > max) max = value;
                }
                return max;
            }
        }
        return -1.0;
    }

    private Double getMeanValueFromQuery(PrometheusResponse prometheusResponse, String generatorId){
        for (PrometheusResponse.Result result: prometheusResponse.data.result) {
            String pod = result.metric.get("pod");
            List<List<Object>> values = result.values;
            if (generatorId.equals(result.metric.get("pod"))) {
                double sum = 0;
                for (List<Object> valuePair : values) {
                    double value = Double.parseDouble((String) valuePair.get(1));
                    sum += value;
                }
                double average = sum / values.size();
//                System.out.printf("Pod: %s, Average CPU usage: %f\n", pod, average);
                return average;
            }
        }
        return -1.0;
    }

    private PrometheusResponse queryPrometheus(String query, long start, long end, long step) throws IOException {
        String url = String.format("http://localhost:9090/api/v1/query_range?query=%s&start=%d&end=%d&step=%d", query, start, end, step);
        System.out.println("final URL: " + url);
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = null;
            if (response.body() != null) {
                responseBody = response.body().string();
            }
            return gson.fromJson(responseBody, PrometheusResponse.class);
        }
    }
}



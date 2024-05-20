package org.ledokol.testrunner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PrometheusResponse {
    public String status;
    public Data data;

    @lombok.Data
    public static class Data {
        @JsonProperty("resultType")
        public String resultType;
        public List<Result> result;
    }

    @lombok.Data
    public static class Result {
        public Map<String, String> metric;
        public List<List<Object>> values;
    }
}


package org.ledokol.testrunner.service;

import org.ledokol.testrunner.model.PrometheusResponse;

public interface PrometheusService {
    Double getMeanValueFromQuery(String query, long start, long end, String generatorId);
    Double getMaxValueFromQuery(String query, long start, long end, String generatorId);
}

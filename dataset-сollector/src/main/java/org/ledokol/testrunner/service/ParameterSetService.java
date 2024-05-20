package org.ledokol.testrunner.service;

import org.ledokol.testrunner.model.ParameterSet;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ParameterSetService {
    List<ParameterSet> getSetList(Pageable pageable);
}

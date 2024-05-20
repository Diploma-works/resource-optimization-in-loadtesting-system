package org.ledokol.testrunner.service.Impl;

import org.ledokol.testrunner.model.ParameterSet;
import org.ledokol.testrunner.repository.ParameterSetRepository;
import org.ledokol.testrunner.service.ParameterSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class ParameterSetServiceImpl implements ParameterSetService {

    @Autowired
    ParameterSetRepository parameterSetRepository;
    @Override
    public List<ParameterSet> getSetList(Pageable pageable) {
        return parameterSetRepository.findAll(pageable).getContent();
    }
}

package org.ledokol.testrunner;

import org.ledokol.testrunner.model.ParameterSet;
import org.ledokol.testrunner.service.ParameterSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("paramset")
public class ParameterSetController {

    @Autowired
    ParameterSetService parameterSetService;

    @GetMapping()
    public ResponseEntity<List<ParameterSet>> getParameterSet(Pageable pageable){
        return new ResponseEntity<>(parameterSetService.getSetList(pageable), HttpStatus.OK);
    }
}

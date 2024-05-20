package org.ledokol.testrunner.controllers;

import org.ledokol.testrunner.service.TestsRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RunnerController {

    private boolean isStarted = false;
    @Autowired
    private TestsRunner testsRunner;

    @PostMapping("start")
    public ResponseEntity<String> startTestsAutoRun(){
        if (!isStarted) {
            isStarted = true;
            testsRunner.runAutoTesting();
            return new ResponseEntity<>("started", HttpStatus.OK);
        }
        else return new ResponseEntity<>("autonomization already started ", HttpStatus.OK);
    }
}

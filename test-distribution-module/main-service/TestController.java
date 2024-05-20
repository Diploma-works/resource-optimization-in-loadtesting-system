package com.ledokol.ledokolmain.controller;

import com.ledokol.ledokolmain.db.model.test.Scenario;
import com.ledokol.ledokolmain.db.model.test.Test;
import com.ledokol.ledokolmain.db.model.test.TestRun;
import com.ledokol.ledokolmain.service.TestService;
import com.ledokol.ledokolmain.service.testrun.TestRunOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/tests")
public class TestController {

    private final TestService testService;

    @Autowired
    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping
    public Flux<Test> getAllTests() {
        return testService.getAllTests();
    }

    @GetMapping("/field/name")
    public Mono<List<String>> getAllTestsNames() {
        return testService.getAllTests().map(Test::getName).collectList();
    }

    @GetMapping("/{id}")
    public Mono<Test> getTestById(@PathVariable("id") String testId) {
        return testService.getTestById(testId);
    }

    @GetMapping("/field/name/{name}")
    public Mono<Test> getTestByName(@PathVariable("name") String name) {
        return testService.getTestByName(name);
    }

    @PostMapping
    public Mono<Test> addTest(@RequestBody @Valid Test test) {
        return testService.addTest(test);
    }

    @PutMapping("/{id}")
    public Mono<Test> updateTestById(@RequestBody @Valid Test test, @PathVariable("id") String testId) {
        return testService.updateTest(test, testId);
    }

    @PostMapping("/{id}/scenarios")
    public Mono<Test> addScenarioToTest(@PathVariable("id") String testId, @RequestBody @Valid Scenario scenario) {
        return testService.addScenarioToTest(testId, scenario);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteTest(@PathVariable("id") String testId) {
        return testService.deleteTest(testId);
    }

    @DeleteMapping("/{id}/scenarios/{name}")
    public Mono<Test> deleteScenarioFromTest(@PathVariable("id") String testId, @PathVariable("name") String scenarioName) {
        return testService.deleteScenarioFromTest(testId, scenarioName);
    }

    @GetMapping("/{id}/scenarios/{name}")
    public Mono<Scenario> getScenarioFromTest(@PathVariable("id") String testId, @PathVariable("name") String scenarioName) {
        return testService.getScenarioFromTest(testId, scenarioName);
    }


    @PostMapping("/{id}/run")
    public Mono<TestRun> runTest(@PathVariable("id") String testId, @RequestBody(required = false) @Valid TestRunOptions testRunOptions) {
        return testService.runTest(testId, testRunOptions);
    }

    @PostMapping("/{id}/run/{generatorId}")
    public Mono<TestRun> runTestOnSpecificGenerator(@PathVariable("id") String testId,
                                 @RequestBody(required = false)
                                 @Valid TestRunOptions testRunOptions,
                                 @PathVariable("generatorId") String generatorInstanceId) {
        return testService.runTestOnSpecificGenerator(testId, testRunOptions, generatorInstanceId);
    }

    @GetMapping("/field/directory/{name}")
    public Flux<Test> getAllTestsInDirectory(@PathVariable("name") String directoryName) {
        return testService.getAllTestsInDirectory(directoryName);
    }

    @GetMapping("/field/directory")
    public Mono<Object> getAllDirectories() {
        return testService.getAllTests()
                .collectMultimap(Test::getDirectoryName, Test::getName).flatMap(Mono::just);
    }

    @GetMapping("/debug")
    public String debug() {
        return "this is debug message from debug method from debug controller for debug";
    }
}

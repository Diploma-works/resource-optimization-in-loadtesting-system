package com.ledokol.ledokolmain.service;

import com.ledokol.ledokolmain.db.DBHelper;
import com.ledokol.ledokolmain.db.model.test.*;
import com.ledokol.ledokolmain.db.repository.TestRepository;
import com.ledokol.ledokolmain.service.discovery.Generator;
import com.ledokol.ledokolmain.service.discovery.GeneratorResponse;
import com.ledokol.ledokolmain.service.discovery.GeneratorService;
import com.ledokol.ledokolmain.service.exception.*;
import com.ledokol.ledokolmain.service.testrun.TestRunService;
import com.ledokol.ledokolmain.validation.TestValidator;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ValidationException;
import java.time.Instant;

@Slf4j
@Service
public class TestService {

    private final TestRepository testRepository;

    private final GeneratorService generatorService;

    private final TestDistributorService testDistributorService;

    private final ROGService rogService;

    private final ScriptService scriptService;

    private final TestRunService testRunService;

    private final TestValidator testValidator;

    @Autowired
    public TestService(TestRepository testRepository, GeneratorService generatorService, TestDistributorService testDistributorService, TestRunService testRunService, ScriptService scriptService,
                       TestValidator testValidator, ROGService rogService) {
        this.testRepository = testRepository;
        this.generatorService = generatorService;
        this.testDistributorService = testDistributorService;
        this.testRunService = testRunService;
        this.scriptService = scriptService;
        this.testValidator = testValidator;
        this.rogService = rogService;
    }

    public Flux<Test> getAllTests() {
        return testRepository.findAll();
    }

    public Flux<Test> getAllTestsInDirectory(String directoryName) {
        return testRepository.findByDirectoryName(directoryName);
    }

    public Mono<Test> getTestById(String id) {
        return testRepository.findById(DBHelper.createObjectId(id))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new TestNotFoundException(id))));
    }

    public Mono<Test> getTestByName(String name) {
        return testRepository.findByName(name)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new TestNotFoundException("name", name))));
    }

    public Mono<Test> addTest(Test test) {
        return Mono.just(test)
                .doOnNext(testInMono -> {
                    BindingResult bindingResult = new BeanPropertyBindingResult(testInMono, "test");
                    testValidator.validate(testInMono, bindingResult);
                    if (bindingResult.hasErrors()) {
                        throw new ValidationException(bindingResult.getAllErrors().get(0).getDefaultMessage());
                    }
                }).flatMap(testRepository::save);
    }

    public Mono<Test> updateTest(Test test, String testId) {
        return getTestById(testId).flatMap(foundedTest -> {
            test.setId(foundedTest.getId());
            return addTest(test);
        });
    }

    public Mono<Test> addScenarioToTest(String testId, Scenario scenario) {
        return scriptService.getScriptById(scenario.getScriptId())
                .flatMap(script -> getTestById(testId))
                .flatMap(test -> validateScenario(test, scenario))
                .flatMap(test -> {
                    test.getScenarios().add(scenario);
                    return testRepository.save(test);
                });
    }

    public Mono<Test> deleteScenarioFromTest(String testId, String scenarioName) {
        return getTestById(testId).flatMap(test -> {
            int targetIndex = -1;
            for (int i = 0; i < test.getScenarios().size(); ++i) {
                if (test.getScenarios().get(i).getName().equals(scenarioName)) {
                    targetIndex = i;
                    break;
                }
            }

            if (targetIndex == -1) {
                return Mono.error(new ScenarioNotFoundException(scenarioName));
            } else {
                test.getScenarios().remove(targetIndex);
                return testRepository.save(test);
            }
        });
    }

    public Mono<Scenario> getScenarioFromTest(String testId, String scenarioName) {
        return getTestById(testId).flatMapIterable(Test::getScenarios)
                .filter(scenario -> scenario.getName().equals(scenarioName))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new ScenarioNotFoundException(scenarioName))))
                .next();
    }

    public Mono<Void> deleteTest(String testId) {
        return testRunService.existsRunningTestRunForTest(testId)
                .filter(exist -> !exist)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new TestIsRunningException(testId))))
                .then(testRepository.deleteById(DBHelper.createObjectId(testId)));
    }


    public Mono<TestRun> runTestOnSpecificGenerator(String testId, String generatorId) {
        ObjectId testRunId = new ObjectId();
        return ensureNoRunningTestRunsForTest(testId)
                .then(calculateTotalUsersCountInTest(getTestById(testId)))
                .flatMap(this::validateTestBeforeRun)
                .flatMap(test -> rogService.createROGForSpecGenerator(test, testRunId.toString(), generatorId)
                        .flatMap(rog -> runTestOnGeneratorAndReturnRog(test, testRunId, rog))
                        .then(Mono.just(TestRun.builder()
                                .startTime(Instant.now())
                                .testId(testId)
                                .id(testRunId)
                                .build())))
                .flatMap(testRun -> {
                    log.info("Сохранение testRun с id={}",
                            testRun.getId());
                    return testRunService.addTestRun(testRun).thenReturn(testRun);
                });
    }


    public Mono<TestRun> runTest(String testId) {
        ObjectId testRunId = new ObjectId();
        return ensureNoRunningTestRunsForTest(testId)
                .then(calculateTotalUsersCountInTest(getTestById(testId)))
                .flatMap(this::validateTestBeforeRun)
                .flatMap(test -> testDistributorService.getTestPlanForTest(test)
                        .flatMap(rog -> runTestOnGeneratorAndReturnRog(test, testRunId, rog))
                        .then(Mono.just(TestRun.builder()
                                .startTime(Instant.now())
                                .testId(testId)
                                .id(testRunId)
                                .build())))
                .flatMap(testRun -> {
                    log.info("Сохранение testRun с id={}",
                            testRun.getId());
                    return testRunService.addTestRun(testRun).thenReturn(testRun);
                });
    }

    private Mono<Boolean> ensureNoRunningTestRunsForTest(String testId) {
        return testRunService.existsRunningTestRunForTest(testId)
                .filter(exist -> !exist)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new TestIsRunningException(testId))));
    }

    private Mono<Void> runTestOnGeneratorAndReturnRog(Test test, ObjectId testRunId, RunOnGenerator rog) {
        return Mono.just(Test.builder()
                        .id(testRunId)
                        .name(test.getName())
                        .scenarios(rog.getSpecificScenarios())
                        .build())
                .flatMap(this::loadScriptsInTestObject)
                .zipWith(generatorService.getGeneratorById(rog.getGeneratorId()))
                .flatMap(tuple -> {
                    Test testForGenerator = tuple.getT1();
                    Generator generatorForRog = tuple.getT2();

                    return generatorService.sendRunTestRequestToGenerator(generatorForRog, testForGenerator)
                            .flatMap(response -> handleGeneratorResponseForRunTest(response, testForGenerator, rog));
                });
    }

    private Mono<Void> handleGeneratorResponseForRunTest(GeneratorResponse response, Test testForGenerator, RunOnGenerator rog) {
        if (response.status() == HttpStatus.OK) {
            log.info("Успешный запуск теста {} id запуска = {} на генераторе {}",
                    testForGenerator.getName(), testForGenerator.getId(), rog.getGeneratorId());
            return Mono.empty();
        } else {
            return Mono.error(new RunTestOnGeneratorException(testForGenerator, response, rog.getGeneratorId()));
        }
    }

    private Mono<Test> loadScriptsInTestObject(Test test) {
        return Flux.fromIterable(test.getScenarios())
                .flatMap(scenario -> scriptService
                        .getScriptById(scenario.getScriptId())
                        .map(script -> {
                            scenario.setScript(script);
                            return scenario;
                        }))
                .collectList().map(scenarios -> {
                    test.setScenarios(scenarios);
                    return test;
                });
    }

    public Mono<Test> calculateTotalUsersCountInTest(Mono<Test> test) {
        return test
                .flatMapIterable(Test::getScenarios)
                .flatMapIterable(Scenario::getSteps)
                .filter(scenarioStep -> scenarioStep.getAction().equals("start"))
                .map(ScenarioStep::getTotalUsersCount)
                .collectList()
                .zipWith(test, ((userCountOnScenario, test1) -> {
                    test1.setTotalUserCount(userCountOnScenario.stream().mapToInt(i -> i).sum());
                    return test1;
                }));
    }

    private Mono<Test> validateScenario(Test test, Scenario scenario) {
        BindingResult bindingResult = new BeanPropertyBindingResult(test, "test");
        testValidator.validateForNewScenario(test, scenario, bindingResult);
        if (bindingResult.hasErrors()) {
            return Mono.error(new ValidationException(bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }
        return Mono.just(test);
    }

    private Mono<Test> validateTestBeforeRun(Test test) {
        if(test.getScenarios() != null && !test.getScenarios().isEmpty()){
            return Mono.just(test);
        } else {
            return Mono.error(new ScenariosInTestNotFoundException(test.getId().toString()));
        }
    }
}


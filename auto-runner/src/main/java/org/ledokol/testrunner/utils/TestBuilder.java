package org.ledokol.testrunner.utils;

import org.ledokol.testrunner.model.script.Script;
import org.ledokol.testrunner.model.script.ScriptStep;
import org.ledokol.testrunner.model.test.Scenario;
import org.ledokol.testrunner.model.test.ScenarioStep;
import org.ledokol.testrunner.model.test.Test;
import org.ledokol.testrunner.utils.ParameterCombinationsGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestBuilder {

    @Value("${target-system.baseurl}")
    private  String BASE_TARGET_URL;
    public Script createScriptFromParams(int vusersAmount, int pacing, int stepsAmount, int requestSize, String postfix){
        Script script = new Script();
        script.setName("GeneratedScriptFor_" + vusersAmount + "_" + pacing + "_" + stepsAmount + "_" +  requestSize + "_"+postfix);
        script.setDirectoryName("GeneratedScriptsDirectory");

        List<ScriptStep> steps = new ArrayList<>();
        int bodySizePerPostRequest = stepsAmount > 0 ? (requestSize * 1024) / stepsAmount : 0;
        String postRequestBody = generatePostRequestBody(bodySizePerPostRequest);

        for (int i = 0; i < stepsAmount; i++) {
            ScriptStep step = getScriptStep(i, postRequestBody);
            steps.add(step);
        }
        script.setSteps(steps);

        return script;
    }

    private ScriptStep getScriptStep(int i, String postRequestBody) {
        ScriptStep step = new ScriptStep();
        if (i % 2 == 0) {
            step.setUrl(BASE_TARGET_URL + "/api/nodes");
            step.setMethod(HttpMethod.POST);
        } else {
            step.setUrl(BASE_TARGET_URL + "/api/nodes");
            step.setMethod(HttpMethod.GET);
        }
        step.setName("Step_" + (i + 1));
        step.setBody(postRequestBody);

        step.setTimeout(2000L);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        step.setHeaders(headers);
        return step;
    }

    public Test createTestFromParams(int vusersAmount, int pacing, int stepsAmount, int requestSize, String postfix){
        Test test = new Test();
        test.setName("GeneratedTestFor_" + vusersAmount + "_" + pacing + "_" + stepsAmount + "_" +  requestSize + "_" + postfix);
        test.setDirectoryName("GeneratedTestsDirectory");

        List<Scenario> scenarios = new ArrayList<>();
        Scenario scenario = new Scenario();
        scenario.setName("ScenarioFor_" + vusersAmount + "_" + stepsAmount + "_" +  requestSize);
        scenario.setPacing((double) pacing);
        scenario.setPacingDelta(0.1); // Пример значения, может быть адаптировано

        List<ScenarioStep> steps = new ArrayList<>();
        steps.add(createTestStep("start", vusersAmount));
        steps.add(createDurationTestStep()); // Пример длительности
        steps.add(createTestStep("stop", vusersAmount));

        scenario.setSteps(steps);
        scenarios.add(scenario);
        test.setScenarios(scenarios);

        return test;
    }

    public Script createScriptFromParameterSet(ParameterCombinationsGenerator.ParameterSet parameterSet, String postfix) {
        return createScriptFromParams(parameterSet.getVuser(), parameterSet.getPacing(), parameterSet.getSteps(), parameterSet.getRequestSize(), postfix);
    }
    public Script createScriptFromParameterSet(ParameterCombinationsGenerator.ParameterSet parameterSet) {
        return createScriptFromParams(parameterSet.getVuser(), parameterSet.getPacing(), parameterSet.getSteps(), parameterSet.getRequestSize(), "");
    }

    public Test createTestFromParameterSet(ParameterCombinationsGenerator.ParameterSet parameterSet) {
        return createTestFromParams(parameterSet.getVuser(), parameterSet.getPacing(), parameterSet.getSteps(), parameterSet.getRequestSize(), "");
    }
    public Test createTestFromParameterSet(ParameterCombinationsGenerator.ParameterSet parameterSet, String postfix) {
        return createTestFromParams(parameterSet.getVuser(), parameterSet.getPacing(), parameterSet.getSteps(), parameterSet.getRequestSize(), postfix);
    }

    public String generatePostRequestBody(int sizeInBytes) {
        int jsonFormattingLength = "{\"id\": 1, \"content\": \"\"}".length();
        int dataSize = sizeInBytes - jsonFormattingLength;

        if (dataSize <= 0) {
            return "{}";
        }

        StringBuilder dataContent = new StringBuilder(dataSize);
        for (int i = 0; i < dataSize; i++) {
            dataContent.append("x");
        }

        return String.format("{\"id\": 1, \"content\": \"%s\"}", dataContent.toString());
    }

    private ScenarioStep createTestStep(String action, int totalUsersCount) {
        ScenarioStep step = new ScenarioStep();
        step.setAction(action);
        step.setTotalUsersCount(totalUsersCount);
        step.setCountUsersByPeriod(2); // Для упрощения используем 1
        step.setPeriod((double) 1);
        return step;
    }

    private ScenarioStep createDurationTestStep() {
        ScenarioStep step = new ScenarioStep();
        step.setAction("duration");
        step.setPeriod((double) 540);
        return step;
    }
}

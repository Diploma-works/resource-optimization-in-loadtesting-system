package org.ledokol.testrunner.model.test;

import lombok.Data;
import org.ledokol.testrunner.model.script.Script;

import java.util.List;

@Data
public class Scenario {

    private String name;

    private List<ScenarioStep> steps;

    private Double pacing;

    private Double pacingDelta = 0.0;

    private String scriptId;

    private Script script;


    public int getActionAmountByName(String actionName){
        return (int) steps.stream().filter(step -> step.getAction().equals(actionName)).count();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ScenarioStep> getSteps() {
        return steps;
    }

    public void setSteps(List<ScenarioStep> steps) {
        this.steps = steps;
    }

    public Double getPacing() {
        return pacing;
    }

    public void setPacing(Double pacing) {
        this.pacing = pacing;
    }

    public Double getPacingDelta() {
        return pacingDelta;
    }

    public void setPacingDelta(Double pacingDelta) {
        this.pacingDelta = pacingDelta;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }
}

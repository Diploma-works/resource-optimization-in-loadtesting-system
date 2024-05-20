package org.ledokol.testrunner.model.script;

import lombok.Data;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;

@Data
public class Script {

    private ObjectId id;

    private String name;

    private String directoryName;

    private List<ScriptStep> steps;

    private HashMap<String, Variable> variables;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public List<ScriptStep> getSteps() {
        return steps;
    }

    public void setSteps(List<ScriptStep> steps) {
        this.steps = steps;
    }

    public HashMap<String, Variable> getVariables() {
        return variables;
    }

    public void setVariables(HashMap<String, Variable> variables) {
        this.variables = variables;
    }
}

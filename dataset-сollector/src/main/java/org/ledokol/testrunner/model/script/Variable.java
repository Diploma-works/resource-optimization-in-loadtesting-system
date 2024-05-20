package org.ledokol.testrunner.model.script;

import lombok.Data;

@Data
public class Variable {

    private String name;

    private VariableScope scope;

    private String generationRegex;

    private String insertingRegex;
}
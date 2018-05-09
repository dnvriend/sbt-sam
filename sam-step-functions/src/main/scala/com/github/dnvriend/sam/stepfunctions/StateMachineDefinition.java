package com.github.dnvriend.sam.stepfunctions;

public interface StateMachineDefinition {
    /**
     * Returns the name of the state machine
     */
    String name();

    /**
     * Returns the state machine definition as JSON
     */
    String apply();
}

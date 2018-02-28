package io.codextension.boot.command;

import io.codextension.boot.config.Algorithm;
import org.springframework.context.annotation.Scope;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import javax.validation.constraints.NotNull;

@ShellComponent()
@Scope("singleton")
@ShellCommandGroup("Common command")
public class CommonCmds {
    private Algorithm algorithm = null;

    @ShellMethod(key = "algorithm", value = "Select an algorithm to evaluate")
    public void algorithm(@ShellOption(value = "name", help = "Algorithm name") @NotNull Algorithm algorithms) {
		this.algorithm = algorithms;
	}

    public Algorithm getAlgorithm() {
		return algorithm;
	}
}

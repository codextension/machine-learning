package io.codextension.boot.command;

import io.codextension.algorithm.regression.LinearRegression;
import io.codextension.algorithm.regression.LogisticRegression;
import io.codextension.boot.config.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.*;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

@ShellComponent
@ShellCommandGroup("Regression commands")
public class RegressionCmds {

	@Autowired
	private CommonCmds commonCmds;

    public Availability isRegression() {
        return commonCmds.getAlgorithm() != null && commonCmds.getAlgorithm() == Algorithm.REGRESSION ? Availability.available() : Availability.unavailable("You did not select this algorithm");
    }

    @ShellMethod(key = "linear", value = "Calculate the linear regression equation")
    @ShellMethodAvailability({"isRegression"})
    public String linearRegression(@ShellOption(value = "file", help = "File name of the dataset") @NotNull File file,
                                   @ShellOption(value = "regularised", help = "is it regularised?", defaultValue = "false") boolean regularised,
                                   @ShellOption(value = "scaled", help = "is it scaled?", defaultValue = "false") boolean scaled) {
		LinearRegression linearRegression = new LinearRegression();
		linearRegression.setRegularised(regularised);
		linearRegression.setScaled(scaled);

		try {
			return linearRegression.evaluate(file);
		} catch (FileNotFoundException | URISyntaxException e) {
			return e.getMessage();
		}
	}

    @ShellMethod(key = "logistic", value = "Calculate the logistic regression equation")
    @ShellMethodAvailability({"isRegression"})
    public String logisticRegression(@ShellOption(value = "file", help = "File name of the dataset") @NotNull File file,
                                     @ShellOption(value = "regularised", help = "is it regularised?", defaultValue = "false") boolean regularised,
                                     @ShellOption(value = "scaled", help = "is it scaled?", defaultValue = "false") boolean scaled) {
		LogisticRegression logisticRegression = new LogisticRegression();
		logisticRegression.setRegularised(regularised);
		logisticRegression.setScaled(scaled);

		try {
			return logisticRegression.evaluate(file);
		} catch (FileNotFoundException | URISyntaxException e) {
			return e.getMessage();
		}
	}

}

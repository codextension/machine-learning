package io.codextension.boot.config;

import io.codextension.boot.command.CommonCmds;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

/**
 * Created by elie on 23.04.17.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AlgoPromptProvider implements PromptProvider {

    @Autowired
    private CommonCmds commonCmds;

    public AttributedString getPrompt() {
        String prompt = "algo: ";
        if (commonCmds.getAlgorithm() != null) {
            switch (commonCmds.getAlgorithm()) {
                case NAIVE_BAYES:
                    prompt = "NaiveBayes: ";
                    break;
                case REGRESSION:
                    prompt = "Regression: ";
                    break;
                case NEURAL_NETWORK:
                    prompt = "NeuralNets: ";
                    break;
                default:
                    prompt = "algo: ";
            }
        }
        AttributedString attributedString = new AttributedString(prompt, AttributedStyle.BOLD.foreground(AttributedStyle.YELLOW));
        return attributedString;
    }
}

package io.codextension.boot.config;

import org.springframework.boot.Banner;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

/**
 * Created by elie on 23.04.17.
 */
public class AlgoBannerProvider implements Banner {

    private static final String[] BANNER = {"",
            "=======================================",
            "*                                     *",
            "*     Machine learning algorithms     *",
            "*                                     *",
            "======================================="};

    private static final String SPRING_BOOT = " :: Machine Learning :: ";

    private static final int STRAP_LINE_SIZE = 39;

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass,
                            PrintStream printStream) {
        for (String line : BANNER) {
            printStream.println(line);
        }
        String version = " (v1.0.0)";
        String padding = "";
        while (padding.length() < STRAP_LINE_SIZE
                - (version.length() + SPRING_BOOT.length())) {
            padding += " ";
        }

        printStream.println(AnsiOutput.toString(AnsiColor.GREEN, SPRING_BOOT,
                AnsiColor.DEFAULT, padding, AnsiStyle.FAINT, version));
        printStream.println();
    }
}

package io.codextension.boot;

import io.codextension.boot.config.AlgoBannerProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

/**
 * Created by elie on 22.04.17.
 */
@SpringBootApplication(scanBasePackages = "io.codextension.**", exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class MainRunner {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(MainRunner.class);
        application.setBanner(new AlgoBannerProvider());
        application.run(args);
    }
}

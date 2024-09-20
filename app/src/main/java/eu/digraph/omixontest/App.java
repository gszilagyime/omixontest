/*
 * This source file was generated by the Gradle 'init' task
 */
package eu.digraph.omixontest;

import eu.digraph.omixontest.config.Config;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
public class App {

    String sequencingFilePath;
    String configFilePath;
    Config.AlignmentTypes alignmentType;

    public App(String sequencingFilePath,
               String configFilePath,
               String alignmentTypeString) {
        this.sequencingFilePath = sequencingFilePath;
        this.configFilePath = configFilePath;
        try {
            this.alignmentType = Config.AlignmentTypes.valueOf(
                    alignmentTypeString);
        } catch (IllegalArgumentException e) {
            final StringBuilder sb = new StringBuilder(
                    "Valid alignment_modes are: ");

            for (Config.AlignmentTypes possibleValue
                 : Config.AlignmentTypes.values()) {
                sb.append(possibleValue.name()).append(' ');
            }
            var msg = sb.toString();
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }

        readConfig();
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            var msg = "Need 3 parameters: sequencing_file configuration_file alignment_mode";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        var app = new App(args[0], args[1], args[2]);
        log.info(app.toString());

    }

    private void readConfig() {
        try {
            var content = Files.readString(Path.of(configFilePath));
            log.trace(content);
            var config = Config.of(content);
            log.debug(config.toString());
        } catch (IOException e) {
            var msg = "Error processing config file: " + configFilePath;
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }
    }
}

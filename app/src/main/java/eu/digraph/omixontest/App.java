package eu.digraph.omixontest;

import eu.digraph.omixontest.config.AlignmentType;
import static eu.digraph.omixontest.config.AlignmentType.bestAlignment;
import static eu.digraph.omixontest.config.AlignmentType.endsAlignment;
import static eu.digraph.omixontest.config.AlignmentType.midAlignment;
import eu.digraph.omixontest.config.Config;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
public class App {

    private static final char WILDCARD = '?';
    AlignmentType alignmentType;
    Config config;
    Map<String, List<String>> buckets;

    public static void main(String[] args) {
        if (args.length != 3) {
            var msg = "Need 3 parameters: sequencing_file configuration_file alignment_mode";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        var app = new App(args[0], args[1], args[2]);
        log.info(app.toString());

    }

    public App(String sequencingFilePath,
               String configFilePath,
               String alignmentTypeString) {
        try {
            this.alignmentType = AlignmentType.valueOf(
                    alignmentTypeString);
        } catch (IllegalArgumentException e) {
            final StringBuilder sb = new StringBuilder(
                    "Valid alignment_modes are: ");

            for (AlignmentType possibleValue
                 : AlignmentType.values()) {
                sb.append(possibleValue.name()).append(' ');
            }
            var msg = sb.toString();
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }

        try {
            config = new Config(alignmentType,
                                Files.readString(Path.of(configFilePath)));
        } catch (IOException e) {
            var msg = "Error processing config file: " + configFilePath;
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }

        buckets = switch (alignmentType) {
            case endsAlignment, midAlignment ->
                processSequenceForEndsOrMids(sequencingFilePath);
            case bestAlignment ->
                processSequenceForBest(sequencingFilePath);
        };
    }

    private Map<String, List<String>> processSequenceForEndsOrMids(final String sequencingFilePath) {
        try {
            Map<String, List<String>> buckets = new TreeMap<>();
            buckets.put("unmatched", new ArrayList<>());
            config.getGroups().forEach(group -> buckets.put(group.getName(),
                                                            new ArrayList<>()));

            for (var line : Files.readAllLines(Path.of(sequencingFilePath))) {
                var sanitized = line.trim();
                var found = false;

                for (var group : config.getGroups()) {
                    switch (alignmentType) {
                        case endsAlignment -> {
                            if (sanitized.startsWith(group.getPrefix())
                                && sanitized.endsWith(group.getPostfix())) {
                                buckets.get(group.getName()).add(sanitized);
                                found = true;
                            }
                        }
                        case midAlignment -> {
                            if (sanitized.contains(group.getInfix())) {
                                buckets.get(group.getName()).add(sanitized);
                                found = true;
                            }
                        }
                    }
                }
                if (!found) {
                    buckets.get("unmatched").add(sanitized);
                }
            }
            return buckets;
        } catch (IOException e) {
            var msg = "Error processing sequencing file: " + sequencingFilePath;
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);

        }
    }

    private Map<String, List<String>> processSequenceForBest(final String sequencingFilePath) {
        try {
            Map<String, List<String>> buckets = new TreeMap<>();
            List<String> unmatchedBucket = new ArrayList<>();
            buckets.put("unmatched", unmatchedBucket);

            config.getGroups().forEach(group -> buckets.put(group.getName(),
                                                            new ArrayList<>()));

            for (var group : config.getGroups()) {
                String infix = group.getInfix();
                String bestSequence = "";
                int maxHits = 0;

                for (var line : Files.readAllLines(Path.of(sequencingFilePath))) {
                    var sanitized = line.trim();

                    for (int shift = 0; shift <= sanitized.length() - infix.length(); shift++) {
                        int hits = 0;
                        for (int i = 0; i < infix.length(); i++) {
                            if (WILDCARD == infix.charAt(i) || sanitized.charAt(i + shift) == infix.charAt(i)) {
                                hits++;
                            }
                        }
                        if (hits > maxHits) {
                            maxHits = hits;
                            bestSequence = sanitized;
                        }
                    }
                }
                if (maxHits != 0) {
                    buckets.get(group.getName()).add(bestSequence);
                }
            }

            final List<String> bestSequences = buckets.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

            for (var line : Files.readAllLines(Path.of(sequencingFilePath))) {
                var sanitized = line.trim();
                if (!bestSequences.contains(sanitized)) {
                    unmatchedBucket.add(sanitized);
                }

            }
            unmatchedBucket.clear();
            return buckets;
        } catch (IOException e) {
            var msg = "Error processing sequencing file: " + sequencingFilePath;
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);

        }
    }

}

package eu.digraph.omixontest;

import eu.digraph.omixontest.config.AlignmentType;
import static eu.digraph.omixontest.config.AlignmentType.BEST_ALIGNMENT;
import static eu.digraph.omixontest.config.AlignmentType.ENDS_ALIGNMENT;
import static eu.digraph.omixontest.config.AlignmentType.MID_ALIGNMENT;
import eu.digraph.omixontest.config.Config;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author gszilagyi
 */
@Slf4j
public final class DemuxUtil {

    private DemuxUtil() {
    }

    public static Map<String, List<String>> demux(String sequencingFilePath,
                                                  String configFilePath,
                                                  String outputGroupFilenameBase,
                                                  String alignmentTypeString) {

        // Check alignment type parameter
        var alignmentType = AlignmentType.valueOfLabel(alignmentTypeString);
        if (alignmentType == null) {
            var msg = "Valid alignment_modes are: "
                  + Stream.of(AlignmentType.values()).map(t -> t.label).collect(Collectors.joining(" "));
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        Config config;

        // Read the config and parse the section belonging to the selected alignment type
        try {
            config = new Config(alignmentType,
                                Files.readString(Path.of(configFilePath)));
        } catch (IOException e) {
            var msg = "Error processing config file: " + configFilePath;
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }

        // run the relevant search algo
        return Collections.unmodifiableMap(
                switch (alignmentType) {
            case ENDS_ALIGNMENT, MID_ALIGNMENT ->
                processSequenceForEndsOrMids(sequencingFilePath, config);
            case BEST_ALIGNMENT ->
                processSequenceForBest(sequencingFilePath, config);
        });
    }

    private static Map<String, List<String>> processSequenceForEndsOrMids(final String sequencingFilePath, final Config config) {
        try {
            // prepare buckets
            final Map<String, List<String>> buckets = new TreeMap<>();
            buckets.put("unmatched", new ArrayList<>());
            config.getGroups().forEach(group -> buckets.put(group.getName(),
                                                            new ArrayList<>()));

            // read patterns from config
            // use prefix and postfix for ends_alignment
            // infix for mid_alignment
            final Map<String, Pattern> patterns = new TreeMap<>();
            config.getGroups().forEach(group -> {
                patterns.put(group.getName(), config.getAlignmentType() == AlignmentType.ENDS_ALIGNMENT
                                              ? Pattern.compile("^" + group.getPrefix() + ".*" + group.getPostfix() + "$",
                                                                Pattern.MULTILINE)
                                              : Pattern.compile(group.getInfix(), Pattern.MULTILINE));
            });

            // the actual search, line by line
            for (var line : Files.readAllLines(Path.of(sequencingFilePath))) {
                // just to be on the safe side with silly platform specific line endings and whitespace chars
                var sanitized = line.trim();

                // find the first match (if any) among our compiled patterns
                // TODO: maybe we need to report all matches not just the first one?
                var found = patterns.entrySet().
                        stream().
                        filter(e -> e.getValue().matcher(sanitized).find()).
                        findFirst();

                // add the current sequence to ...
                buckets.get(found.isPresent()
                            ? found.get().getKey() // ... the bucket for the first match or
                            : "unmatched") // ... the unmatched bucket
                        .add(sanitized);
            }
            return buckets;
        } catch (IOException e) {
            var msg = "Error processing sequencing file: " + sequencingFilePath;
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);

        }
    }

    private static Map<String, List<String>> processSequenceForBest(final String sequencingFilePath, final Config config) {
        try {
            // prepare buckets
            Map<String, List<String>> buckets = new TreeMap<>();
            List<String> unmatchedBucket = new ArrayList<>();
            buckets.put("unmatched", unmatchedBucket);

            config.getGroups().forEach(group -> buckets.put(group.getName(),
                                                            new ArrayList<>()));

            // reading and scanning the sequences for each pattern in the config file
            // downside is O(m*n) complexity, upside is no need to hold the sequence data in memory
            // actual performance tests might reveal a bit more
            for (var group : config.getGroups()) {
                var infix = group.getInfix();
                var bestSequence = "";
                int maxHits = 0;

                // for each line ...
                for (var line : Files.readAllLines(Path.of(sequencingFilePath))) {
                    var sanitized = line.trim();

                    // ... try every possible shift of the pattern
                    for (int shift = 0; shift <= sanitized.length() - infix.length(); shift++) {
                        int hits = 0;
                        for (int i = 0; i < infix.length(); i++) {
                            // ... and increase hit count if we have a match between the pattern and the sequence characters
                            // or the current pattern character is a wildcard
                            if ('.' == infix.charAt(i)
                                || sanitized.charAt(i + shift) == infix.charAt(i)) {
                                hits++;
                            }
                        }

                        // are we the best match so far?
                        if (hits > maxHits) {
                            maxHits = hits;
                            bestSequence = sanitized;
                        }
                    }
                }

                // have we found a match at all?
                if (maxHits != 0) {
                    buckets.get(group.getName()).add(bestSequence);
                }
            }

            // now grab all the sequences that have matched with at least one sequence pattern to a nice list
            final var bestSequences = buckets.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

            // read the input one last time and put every sequence into unmatched that isn't in the bestsequence list
            for (var line : Files.readAllLines(Path.of(sequencingFilePath))) {
                var sanitized = line.trim();
                if (!bestSequences.contains(sanitized)) {
                    unmatchedBucket.add(sanitized);
                }

            }
            return buckets;
        } catch (IOException e) {
            var msg = "Error processing sequencing file: " + sequencingFilePath;
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);

        }
    }

    // this version changes the inner and outer loops. In theory this might improve performance a bit
    // but performance testing on MacOS revealed this one to be a tiny bit slower than the original function with
    // sequence files in the range of 1K to 1M lines with 1024 long sequences
    // (seems caching is pretty much optimal on MacOS)
    private static Map<String, List<String>> processSequenceForBestAlternateVersion(final String sequencingFilePath,
                                                                                    final Config config) {
        try {
            // prepare buckets
            Map<String, List<String>> buckets = new TreeMap<>();
            List<String> unmatchedBucket = new ArrayList<>();
            buckets.put("unmatched", unmatchedBucket);

            config.getGroups().forEach(group -> buckets.put(group.getName(),
                                                            new ArrayList<>()));

            final Map<Config.Group, Integer> maxHits = config.getGroups().stream().collect(Collectors.toMap(g -> g, g -> 0));
            final Map<Config.Group, String> bestSequence = config.getGroups().stream().collect(Collectors.toMap(g -> g, g -> ""));

            // for each line ...
            for (var line : Files.readAllLines(Path.of(sequencingFilePath))) {
                var sanitized = line.trim();

                // and for each pattern ...
                for (var group : config.getGroups()) {
                    var infix = group.getInfix();

                    // ... try every possible shift of the pattern
                    for (int shift = 0; shift <= sanitized.length() - infix.length(); shift++) {
                        int hits = 0;
                        for (int i = 0; i < infix.length(); i++) {

                            // ... and increase hit count if we have a match between the pattern and the sequence characters
                            // or the current pattern character is a wildcard
                            if ('.' == infix.charAt(i)
                                || sanitized.charAt(i + shift) == infix.charAt(i)) {
                                hits++;
                            }
                        }

                        // are we the best match so far?
                        if (hits > maxHits.get(group)) {
                            maxHits.put(group, hits);
                            bestSequence.put(group, sanitized);
                        }
                    }
                }

            }

            for (var group : config.getGroups()) {
                // have we found a match at all?
                if (maxHits.get(group) != 0) {
                    buckets.get(group.getName()).add(bestSequence.get(group));
                }
            }
            // now grab all the sequences that have matched with at least one sequence pattern to a nice list
            final var bestSequences = buckets.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

            // read the input one last time and put every sequence into unmatched that isn't in the bestsequence list
            for (var line : Files.readAllLines(Path.of(sequencingFilePath))) {
                var sanitized = line.trim();
                if (!bestSequences.contains(sanitized)) {
                    unmatchedBucket.add(sanitized);
                }

            }
            return buckets;
        } catch (IOException e) {
            var msg = "Error processing sequencing file: " + sequencingFilePath;
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);

        }
    }

}

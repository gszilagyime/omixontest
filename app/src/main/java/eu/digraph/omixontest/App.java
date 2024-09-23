package eu.digraph.omixontest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
public class App {

    private static final int SEQUENCING_FILE_PARAM = 0;
    private static final int CONFIG_FILE_PARAM = SEQUENCING_FILE_PARAM + 1;
    private static final int OUTPUT_FILE_PARAM = CONFIG_FILE_PARAM + 1;
    private static final int ALIGNMENT_TYPE_PARAM = OUTPUT_FILE_PARAM + 1;

    private static final int GENERATE_TEST_FILES_PARAM = 0;
    private static final int GENERATE_TEST_LINE_COUNT_PARAM = GENERATE_TEST_FILES_PARAM + 1;
    private static final int GENERATE_TEST_LINE_LENGTH_PARAM = GENERATE_TEST_LINE_COUNT_PARAM + 1;
    private static final String SEQ_FILE_EXTENSION = ".seq";

    public static void main(String[] args) {
        if (args.length == GENERATE_TEST_LINE_LENGTH_PARAM + 1 && "generateRandomTestSequences".equals(
                args[GENERATE_TEST_FILES_PARAM])) {
            generateRandomTestSequences(Integer.parseInt(args[GENERATE_TEST_LINE_COUNT_PARAM]),
                                        Integer.parseInt(args[GENERATE_TEST_LINE_LENGTH_PARAM]));
        } else {
            if (args.length != ALIGNMENT_TYPE_PARAM + 1) {
                var msg = "Need 4 parameters: sequencing_file configuration_file output_group_filename_base alignment_mode";
                log.error(msg);
                throw new IllegalArgumentException(msg);
            }
            final var buckets = DemuxUtil.demux(args[SEQUENCING_FILE_PARAM],
                                            args[CONFIG_FILE_PARAM],
                                            args[OUTPUT_FILE_PARAM],
                                            args[ALIGNMENT_TYPE_PARAM]);

            log.info(
                    buckets.
                            entrySet().
                            stream().
                            map(e -> e.getKey() + ": " + e.getValue().size()).
                            collect(Collectors.joining(" "))
            );

            try {
                for (var bucket : buckets.entrySet()) {
                    Files.write(Path.of(args[OUTPUT_FILE_PARAM], bucket.getKey() + SEQ_FILE_EXTENSION),
                                bucket.getValue());
                }
            } catch (IOException e) {
                final var msg = "Error saving buckets";
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }

        }
    }

    private static void generateRandomTestSequences(int count, int lineLength) {
        var sequences = new HashSet<String>(count);

        if (count > (Math.pow(4.0, lineLength))) {
            final var msg = String.
                    format("Impossible to generate %d unique random sequences with %d length lines.", count, lineLength);
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        final Random rand = Random.from(RandomGenerator.getDefault());

        while (sequences.size() < count) {
            var sb = new StringBuilder(lineLength);
            for (int i = 0; i < lineLength; i++) {
                sb.append(switch (rand.nextInt(4)) {
                    case 0 ->
                        'A';
                    case 1 ->
                        'C';
                    case 2 ->
                        'G';
                    case 3 ->
                        'T';
                    default ->
                        'X'; // this shouldn't happen...
                });
            }
            sequences.add(sb.toString());
        }
        System.out.print(sequences.stream().collect(Collectors.joining("\n")));
    }

}

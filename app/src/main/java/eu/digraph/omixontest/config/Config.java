package eu.digraph.omixontest.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

/**
 *
 * @author gszilagyi
 */
@Slf4j
public class Config {

    private static final char WILDCARD = '?';
    @Getter
    private final List<Group> groups;
    @Getter
    private final AlignmentType alignmentType;

    private final Pattern patternForValidSequence = Pattern.compile("^[ACTG\\?]+$",
                                                                    Pattern.MULTILINE);
    // Just a quick and dirty check
    private final Pattern patternForValidFileName = Pattern.compile("[\\p{Alnum}\\.:\\\\/\\ ]*",
                                                                    Pattern.UNICODE_CHARACTER_CLASS);

    public Config(AlignmentType alignmentType, String source) {
        this.alignmentType = alignmentType;

        // use a temporary collection as we want the groups to be unmodifiable
        var tmp = new ArrayList<Group>();

        // only parse the relevant part from the config
        var relevantConfig = new JSONObject(source).getJSONObject(alignmentType.label);

        relevantConfig.keys().
                forEachRemaining((String groupName) -> {
                    var group = relevantConfig.getJSONObject(groupName);
                    tmp.add(switch (alignmentType) {
                        case ENDS_ALIGNMENT ->
                            new Group(sanitizeGroupName(groupName),
                                      sanitizeSequencePattern(group.getString("prefix")),
                                      sanitizeSequencePattern(group.getString("postfix")),
                                      null);
                        case MID_ALIGNMENT, BEST_ALIGNMENT ->
                            new Group(sanitizeGroupName(groupName),
                                      null,
                                      null,
                                      sanitizeSequencePattern(group.getString("infix")));
                    });
                });
        groups = Collections.unmodifiableList(tmp);

    }

    private String sanitizeSequencePattern(String line) {
        // a bit of validation
        if (!patternForValidSequence.matcher(line).matches()) {
            var msg = "Invalid content in the config file: " + line;
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        // and replacement of the '?' char to '.' as it makes life easier with regexp patterns
        return line.replace(WILDCARD, '.');
    }

    private String sanitizeGroupName(String groupName) {
        // the group names will be used as part of the output filenames, it is prudent to do some basic checks on them
        if (!patternForValidFileName.matcher(groupName).matches()) {
            var msg = "Invalid content in the config file: " + groupName;
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        return groupName;
    }

    @Value
    public static class Group {

        String name, prefix, postfix, infix;

    }

}

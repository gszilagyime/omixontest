package eu.digraph.omixontest.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

/**
 *
 * @author gszilagyi
 */
@Value
@Slf4j
public class Config {

    private static final char WILDCARD = '?';

    List<Group> groups;
    private final Pattern pattern;

    public Config(AlignmentType alignmentTypes, String source) {
        // production version
        // pattern = Pattern.compile("^[ACTG\\?]+$", Pattern.MULTILINE);

        // development version
        pattern = Pattern.compile("^[A-Z1-9\\?]+$", Pattern.MULTILINE);
        var tmp = new ArrayList<Group>();

        var relevantConfig = new JSONObject(source).
                getJSONObject(alignmentTypes.name());

        relevantConfig.keys().
                forEachRemaining(groupName -> {
                    var group = relevantConfig.getJSONObject(groupName);
                    tmp.add(switch (alignmentTypes) {
                        case endsAlignment ->
                            new Group(groupName,
                                      checkedConfig(group.getString("prefix")),
                                      checkedConfig(group.getString("postfix")),
                                      null);
                        case midAlignment, bestAlignment ->
                            new Group(groupName,
                                      null,
                                      null,
                                      checkedConfig(group.getString("infix")));
                    });
                });
        groups = Collections.unmodifiableList(tmp);

    }

    private String checkedConfig(String line) {
        if (!pattern.matcher(line).matches()) {
            var msg = "Invalid content in the config file: " + line;
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        return line.replace(WILDCARD, '.');
    }

    @Value
    public static class Group {

        String name, prefix, postfix, infix;

    }

}

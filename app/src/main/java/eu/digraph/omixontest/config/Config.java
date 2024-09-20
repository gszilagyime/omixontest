package eu.digraph.omixontest.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Value;
import org.json.JSONObject;

/**
 *
 * @author gszilagyi
 */
@Value
public class Config {

    List<Group> groups;

    public Config(AlignmentType alignmentTypes, String source) {
        var tmp = new ArrayList<Group>();

        var relevantConfig = new JSONObject(source).
                getJSONObject(alignmentTypes.name());

        relevantConfig.keys().
                forEachRemaining(groupName -> {
                    var group = relevantConfig.getJSONObject(groupName);
                    tmp.add(switch (alignmentTypes) {
                        case endsAlignment ->
                            new Group(groupName,
                                      group.getString("prefix"),
                                      group.getString("postfix"),
                                      null);
                        case midAlignment, bestAlignment ->
                            new Group(groupName,
                                      null,
                                      null,
                                      group.getString("infix"));
                    });
                });
        groups = Collections.unmodifiableList(tmp);

    }

    @Value
    public static class Group {

        String name, prefix, postfix, infix;

    }

}

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

    private static final String ENDS_ALIGNMENT = "endsAlignment";
    private static final String MID_ALIGNMENT = "midAlignment";
    private static final String BEST_ALIGNMENT = "bestAlignment";

    public static Config of(String source) {
        var content = new JSONObject(source);
        return new Config(
                Collections.unmodifiableList(
                        parseEnds(content.getJSONObject(ENDS_ALIGNMENT))),
                Collections.unmodifiableList(
                        parseMid(content.getJSONObject(MID_ALIGNMENT))),
                Collections.unmodifiableList(
                        parseBest(content.getJSONObject(BEST_ALIGNMENT)))
        );
    }

    private static List<endsAlignmentGroup> parseEnds(JSONObject alignmentGroup) {
        final List<endsAlignmentGroup> groups = new ArrayList<>();

        alignmentGroup.keys().forEachRemaining(groupName -> {
            var group = alignmentGroup.getJSONObject(groupName);
            groups.add(new endsAlignmentGroup(groupName,
                                              group.getString("prefix"),
                                              group.getString("postfix")));
        });
        return groups;
    }

    private static List<midAlignmentGroup> parseMid(JSONObject alignmentGroup) {
        final List<midAlignmentGroup> groups = new ArrayList<>();

        alignmentGroup.keys().forEachRemaining(groupName -> {
            var group = alignmentGroup.getJSONObject(groupName);
            groups.add(new midAlignmentGroup(groupName,
                                             group.getString("infix")));
        });
        return groups;
    }

    private static List<bestAlignmentGroup> parseBest(JSONObject alignmentGroup) {
        final List<bestAlignmentGroup> groups = new ArrayList<>();

        alignmentGroup.keys().forEachRemaining(groupName -> {
            var group = alignmentGroup.getJSONObject(groupName);
            groups.add(new bestAlignmentGroup(groupName,
                                              group.getString("infix")));
        });
        return groups;
    }
    List<endsAlignmentGroup> endsAlignmentGroups;
    List<midAlignmentGroup> midAlignmentGroups;
    List<bestAlignmentGroup> bestAlignmentGroups;

    public enum AlignmentTypes {
        endsAlignment,
        midAlignment,
        bestAlignment
    }

    @Value
    public static class endsAlignmentGroup {

        String name, prefix, postfix;
    }

    @Value
    public static class midAlignmentGroup {

        String name, infix;
    }

    @Value
    public static class bestAlignmentGroup {

        String name, infix;
    }

}

package eu.digraph.omixontest.config;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author gszilagyi
 */
public enum AlignmentType {
    ENDS_ALIGNMENT("endsAlignment"),
    MID_ALIGNMENT("midAlignment"),
    BEST_ALIGNMENT("bestAlignment");

    private static final Map<String, AlignmentType> LABEL_CACHE = new HashMap<>();

    static {
        for (AlignmentType e : values()) {
            LABEL_CACHE.put(e.label, e);
        }
    }

    public final String label;

    AlignmentType(String label) {
        this.label = label;
    }

    public static AlignmentType valueOfLabel(String label) {
        return LABEL_CACHE.get(label);
    }

}

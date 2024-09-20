package eu.digraph.omixontest.config;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author gszilagyi
 */
public class ConfigIT {

    @Test(expected = JSONException.class)
    public void testInvalidSource() {
        var instance = new Config(AlignmentType.midAlignment, "");
    }

    @Test(expected = JSONException.class)
    public void testEmptySource() {
        var instance = new Config(AlignmentType.midAlignment, "{}");
    }

    @Test
    public void testMid() {
        var instance = new Config(AlignmentType.endsAlignment,
                              """
                                                                  {
                                                                  "endsAlignment" : {
                                                                      "group1" : {
                                                                          "prefix" : "ACTCACG",
                                                                          "postfix" : "ACGATCG"
                                                                      },
                                                                      "group2" : {
                                                                          "prefix" : "CAGTAAG",
                                                                          "postfix" : "ACGTACA"
                                                                      }
                                                                  },
                                                                  "midAlignment" : {
                                                                      "group1" : {
                                                                          "infix" : CACTAACT
                                                                      },
                                                                      "group2" : {
                                                                          "infix" : CAGACAGT
                                                                      }
                                                                  },
                                                                  "bestAlignment" : {
                                                                      "group1" : {
                                                                          "infix" : CTATCTAGCAAT
                                                                      }
                                                                  }
                                                              }
                                                              """);
        Assert.assertTrue(instance.getGroups().size() == 2);

        instance = new Config(AlignmentType.midAlignment,
                              """
                                                                  {
                                                                  "endsAlignment" : {
                                                                      "group1" : {
                                                                          "prefix" : "ACTCACG",
                                                                          "postfix" : "ACGATCG"
                                                                      },
                                                                      "group2" : {
                                                                          "prefix" : "CAGTAAG",
                                                                          "postfix" : "ACGTACA"
                                                                      }
                                                                  },
                                                                  "midAlignment" : {
                                                                      "group1" : {
                                                                          "infix" : CACTAACT
                                                                      },
                                                                      "group2" : {
                                                                          "infix" : CAGACAGT
                                                                      }
                                                                  },
                                                                  "bestAlignment" : {
                                                                      "group1" : {
                                                                          "infix" : CTATCTAGCAAT
                                                                      }
                                                                  }
                                                              }
                                                              """);
        Assert.assertTrue(instance.getGroups().size() == 2);
        instance = new Config(AlignmentType.bestAlignment,
                              """
                                                                  {
                                                                  "endsAlignment" : {
                                                                      "group1" : {
                                                                          "prefix" : "ACTCACG",
                                                                          "postfix" : "ACGATCG"
                                                                      },
                                                                      "group2" : {
                                                                          "prefix" : "CAGTAAG",
                                                                          "postfix" : "ACGTACA"
                                                                      }
                                                                  },
                                                                  "midAlignment" : {
                                                                      "group1" : {
                                                                          "infix" : CACTAACT
                                                                      },
                                                                      "group2" : {
                                                                          "infix" : CAGACAGT
                                                                      }
                                                                  },
                                                                  "bestAlignment" : {
                                                                      "group1" : {
                                                                          "infix" : CTATCTAGCAAT
                                                                      }
                                                                  }
                                                              }
                                                              """);
        Assert.assertTrue(instance.getGroups().size() == 1);
    }

}

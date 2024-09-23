package eu.digraph.omixontest.config;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author gszilagyi
 */
@SuppressWarnings("unused")
public class ConfigIT {

    @Test(expected = JSONException.class)
    public void testInvalidSource() {
        var instance = new Config(AlignmentType.MID_ALIGNMENT, "");
    }

    @Test(expected = JSONException.class)
    public void testEmptySource() {
        var instance = new Config(AlignmentType.MID_ALIGNMENT, "{}");
    }

    @Test
    public void testParse() {
        var instance = new Config(AlignmentType.ENDS_ALIGNMENT,
                              """
                                                                  {
                                                                  "endsAlignment" : {
                                                                      "group1" : {
                                                                          "prefix" : "ACTCACG",
                                                                          "postfix" : "ACGATCG"
                                                                      },
                                                                      "group2" : {
                                                                          "prefix" : "CAGTAAG",
                                                                          "postfix" : "AC?TACA"
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
        Assert.assertEquals(instance.getAlignmentType(), AlignmentType.ENDS_ALIGNMENT);
        Assert.assertEquals("AC.TACA",
                            instance.getGroups().stream().
                                    filter(g -> g.getName().equalsIgnoreCase("group2")).
                                    findFirst().
                                    get().
                                    getPostfix());

        instance = new Config(AlignmentType.MID_ALIGNMENT,
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
                                                                          "infix" : C??T?AACT
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
        Assert.assertEquals(instance.getAlignmentType(), AlignmentType.MID_ALIGNMENT);
        Assert.assertEquals("C..T.AACT",
                            instance.getGroups().stream().
                                    filter(g -> g.getName().equalsIgnoreCase("group1")).
                                    findFirst().
                                    get().
                                    getInfix());

        instance = new Config(AlignmentType.BEST_ALIGNMENT,
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
                                                                      },
                                                                      "group2" : {
                                                                          "infix" : TACA???TACC
                                                                      }
                                                                  }
                                                              }
                                                              """);
        Assert.assertTrue(instance.getGroups().size() == 2);
        Assert.assertEquals(instance.getAlignmentType(), AlignmentType.BEST_ALIGNMENT);
        Assert.assertEquals("TACA...TACC",
                            instance.getGroups().stream().
                                    filter(g -> g.getName().equalsIgnoreCase("group2")).
                                    findFirst().
                                    get().
                                    getInfix());
    }

}

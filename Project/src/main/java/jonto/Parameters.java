package jonto;

import jonto.reasoning.ReasonerManager;

public class Parameters {
    // Core parameters.
    public static int reasoner = ReasonerManager.HERMIT;

    // ISUB parameters.
    public static double good_isub_anchors = 0.98;
    public static double good_isub_candidates = 0.95;
    public static double bad_score_scope = 0.0;
    public static double good_sim_coocurrence = 0.08;
    public static double good_confidence = 0.50;
    public static double min_isub_instances = 0.75;
}

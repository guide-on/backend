package com.guideon.plan.policy;

public final class GradePolicy {
    private GradePolicy() {}
    public static String toGrade(double score) {
        if (score >= 95) return "A+";
        if (score >= 90) return "A";
        if (score >= 85) return "B+";
        if (score >= 75) return "B";
        if (score >= 65) return "C";
        if (score >= 55) return "D";
        return "F";
    }
}

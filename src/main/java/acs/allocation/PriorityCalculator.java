package acs.allocation;

public final class PriorityCalculator {

    private PriorityCalculator() {
    }

    public static int calculatePriority(
            int severityScore,
            double waitingMinutes
    ) {
        if (severityScore < 0) {
            throw new IllegalArgumentException(
                    "Severity score cannot be negative."
            );
        }

        if (!Double.isFinite(waitingMinutes)
                || waitingMinutes < 0) {
            throw new IllegalArgumentException(
                    "Waiting time must be non-negative."
            );
        }

        int waitingBonus = (int) Math.min(
                2 * waitingMinutes,
                30
        );

        return severityScore + waitingBonus;
    }
}
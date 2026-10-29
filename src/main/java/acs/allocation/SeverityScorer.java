package acs.allocation;

public final class SeverityScorer{

    private SeverityScorer() {
    }

    public static int scoreOf(String severity) {
        if (severity == null || severity.isBlank()) {
            throw new IllegalArgumentException(
                    "Severity cannot be empty."
            );
        }

        return switch (severity.trim().toUpperCase()) {
            case "CRITICAL" -> 100;
            case "HIGH" -> 70;
            case "MEDIUM" -> 40;
            case "LOW" -> 10;
            default -> throw new IllegalArgumentException(
                    "Unknown severity: " + severity
            );
        };
    }
}
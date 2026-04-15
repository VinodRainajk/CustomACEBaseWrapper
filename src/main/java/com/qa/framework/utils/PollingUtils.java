package com.qa.framework.utils;

import com.qa.framework.exceptions.WrapperException;

/**
 * Shared wait and polling helpers for step definitions.
 */
public final class PollingUtils {

    private PollingUtils() {
    }

    @FunctionalInterface
    public interface PollCheck {
        PollOutcome check() throws Exception;
    }

    public static final class PollOutcome {
        private final boolean matched;
        private final String observed;

        private PollOutcome(boolean matched, String observed) {
            this.matched = matched;
            this.observed = observed;
        }

        public static PollOutcome of(boolean matched, String observed) {
            return new PollOutcome(matched, observed);
        }

        public boolean isMatched() {
            return matched;
        }

        public String getObserved() {
            return observed;
        }
    }

    public static final class PollReport {
        private final int attempts;
        private final long elapsedMillis;
        private final String lastObserved;

        public PollReport(int attempts, long elapsedMillis, String lastObserved) {
            this.attempts = attempts;
            this.elapsedMillis = elapsedMillis;
            this.lastObserved = lastObserved;
        }

        public int getAttempts() {
            return attempts;
        }

        public long getElapsedMillis() {
            return elapsedMillis;
        }

        public String getLastObserved() {
            return lastObserved;
        }
    }

    public static void sleepSeconds(int seconds) {
        if (seconds <= 0) {
            throw new WrapperException("Sleep duration must be greater than 0 seconds.");
        }
        sleepMillisInternal(seconds * 1000L);
    }

    public static PollReport pollUntil(
            int timeoutSeconds,
            int intervalMillis,
            String operationDescription,
            PollCheck check
    ) {
        if (timeoutSeconds <= 0) {
            throw new WrapperException("Timeout must be greater than 0 seconds.");
        }
        if (intervalMillis <= 0) {
            throw new WrapperException("Polling interval must be greater than 0 milliseconds.");
        }

        long start = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;
        long deadline = start + timeoutMillis;
        int attempts = 0;
        String lastObserved = "no observation";
        Exception lastError = null;

        while (true) {
            attempts++;
            try {
                PollOutcome outcome = check.check();
                if (outcome.getObserved() != null && !outcome.getObserved().isBlank()) {
                    lastObserved = outcome.getObserved();
                }
                if (outcome.isMatched()) {
                    long elapsed = System.currentTimeMillis() - start;
                    return new PollReport(attempts, elapsed, lastObserved);
                }
            } catch (Exception e) {
                lastError = e;
                lastObserved = "error: " + e.getMessage();
            }

            long now = System.currentTimeMillis();
            if (now >= deadline) {
                break;
            }
            long remaining = deadline - now;
            sleepMillisInternal(Math.min(intervalMillis, remaining));
        }

        long elapsed = System.currentTimeMillis() - start;
        StringBuilder message = new StringBuilder()
                .append("Polling failed for ").append(operationDescription)
                .append(". Timed out after ").append(timeoutSeconds).append(" seconds")
                .append(" with interval ").append(intervalMillis).append(" ms")
                .append(", attempts=").append(attempts)
                .append(", lastObserved=").append(lastObserved).append(".");
        if (lastError != null) {
            message.append(" Last error: ").append(lastError.getMessage());
        }
        throw new WrapperException(message.toString(), lastError);
    }

    private static void sleepMillisInternal(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WrapperException("Thread interrupted while waiting.", e);
        }
    }
}

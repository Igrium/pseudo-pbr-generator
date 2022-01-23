package com.igrium.pseudo_pbr.pipeline;

/**
 * Accepts progress to display in the UI.
 */
public interface ProgressListener {

    public static ProgressListener DUMMY = new ProgressListener() {
        public void progress(float percentage, String message) {} 
    };

    /**
     * A progress listener that reports its value to a range of another progress listener.
     */
    public static class SubProgressListener implements ProgressListener {

        private float min;
        private float max;
        private ProgressListener parent;

        /**
         * Create a SubProgressListener.
         * @param parent Parent progress listener.
         * @param min Minimum value of the parent.
         * @param max Maximum value of the parent.
         */
        public SubProgressListener(ProgressListener parent, float min, float max) {
            this.parent = parent;
            this.min = min;
            this.max = max;
        }

        /**
         * Create a SubProgressListener. Equivalent to
         * <code>new SubProgressListener(parent, min / total, max / total)</code>
         * 
         * @param parent Parent progress listener.
         * @param min    Minimum step of the parent.
         * @param max    Maximim step of the parent.
         * @param total  Total number of steps in the parent.
         */
        public SubProgressListener(ProgressListener parent, int min, int max, int total) {
            this.parent = parent;
            this.min = min / (float) total;
            this.max = max / (float) total;
        }

        @Override
        public void progress(float percentage, String message) {
            float remapped = min + percentage * (max - min);
            parent.progress(remapped, message);
        }

        @Override
        public void warn(String message) {
            parent.warn(message);
        }
        
    }

    /**
     * Update the progress bar.
     * 
     * @param percentage A value from 0-1 indicating what percentage of the task is
     *                   complete.
     * @param message    Feeback message about the current step.
     */
    void progress(float percentage, String message);
    
    /**
     * Send a warning to the ui. For situations where something is wrong but it's
     * not fatal.
     * 
     * @param message Warning message.
     */
    default void warn(String message) {};

    /**
     * Update the progress bar. Equivalent to <code>progress(step / total, message)</code>
     * @param step The current progress step number.
     * @param total The total amount of steps.
     * @param message Feedback message about the current step.
     */
    default void progress(int step, int total, String message) {
        progress((float)step / (float)total, message);
    }
}

package com.igrium.pseudo_pbr.pipeline;

/**
 * Accepts progress to display in the UI.
 */
public interface ProgressListener {

    public static ProgressListener DUMMY = new ProgressListener() {
        public void progress(float percentage, String message) {} 
    };

    /**
     * Update the progress bar.
     * 
     * @param percentage A value from 0-1 indicating what percentage of the task is
     *                   complete.
     * @param message    Feeback message about the task at hand.
     */
    void progress(float percentage, String message);
    
    /**
     * Send a warning to the ui. For situations where something is wrong but it's
     * not fatal.
     * 
     * @param message Warning message.
     */
    default void warn(String message) {};
}

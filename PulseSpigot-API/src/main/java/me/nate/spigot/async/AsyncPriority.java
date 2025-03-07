package me.nate.spigot.async;

/**
 * Enum representing in which order asynchronous tasks will be executed.
 */
public enum AsyncPriority {

    /**
     * Tasks will be executed in the order they were added.
     */
    NORMAL,

    /**
     * Tasks will be executed before NORMAL tasks.
     */
    HIGH,

    /**
     * Tasks will be executed before HIGH and NORMAL tasks.
     * Shouldn't be used often - only when you really need to execute task as soon as possible.
     */
    URGENT

}

package com.github.rfqu.df4j.codec.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;

/** 
 * Messages are already Runnables.
 */
public class ManualExecutor implements Executor, Runnable {
    /** current task */
    private Runnable active=null;
    /** rest of tasks */
    private final Queue<Runnable> tasks  = new LinkedList<Runnable>();

    /** 
     * Frontend method which may be called from other Thread or Actor.
     * Saves the message and initiates Actor's execution.
     */
    public void execute(Runnable task) {
        if (task==null) {
            throw new IllegalArgumentException("task may not be null"); 
        }
        synchronized(tasks ) {
            if (active != null) {
                tasks.add(task);
                return;
            }
            active=task;
        }
    }

    @Override
    public final void run() {
        for (;;) {
            active.run();
            synchronized(tasks) {
                if ((active = tasks.poll())==null) {
                    return;
                }
            }
        }
    }
}

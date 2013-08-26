package com.github.rfqu.pipeline.core;

import java.util.concurrent.Executor;

import com.github.rfqu.df4j.core.Callback;
import com.github.rfqu.df4j.core.DataflowNode;

/**
 * @author kaigorodov
 */
public abstract class BoltNode extends DataflowNode
    implements Bolt
{
    protected Callback<Object> context;
    protected Lockup lockUp = new Lockup();

    public BoltNode() {
    }

    public BoltNode(Executor executor) {
        super(executor);
    }

    public void setContext(Callback<Object> context) {
        this.context = context;
    }

    public void start() {
        lockUp.on();
    }

    public void stop() {
        lockUp.off();
    }

    @Override
    protected void handleException(Throwable exc) {
        context.postFailure(exc);
    }
}
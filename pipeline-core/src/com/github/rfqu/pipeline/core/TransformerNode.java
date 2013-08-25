package com.github.rfqu.pipeline.core;

import java.util.concurrent.Executor;

import com.github.rfqu.df4j.core.Callback;
import com.github.rfqu.df4j.core.DataflowNode;
import com.github.rfqu.df4j.core.StreamPort;

/**
 * {@link mySinkPort} and {@link myreturnPort} must be initialized in subclasses
 * 
 * @author kaigorodov
 *
 * @param <I> type of input messages
 * @param <O> type of output messages
 */
public abstract class TransformerNode<I, O> extends DataflowNode
    implements Transformer<I, O>
{
    //----------------- Bolt part
    
    protected Callback<Object> context;
    protected Lockup lockUp = new Lockup();

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

    //----------------- Sink part
    
    /** there input messages return */
    protected StreamPort<I> returnPort;

    @Override
    public void setReturnPort(StreamPort<I> returnPort) {
        this.returnPort=returnPort;
    }

    //----------------- Source part

    /** there output messages go */
    protected StreamPort<O> sinkPort;
    
    @Override
    public void setSinkPort(StreamPort<O> sinkPort) {
        this.sinkPort=sinkPort;
    }
    
    //-------------------------
	
    public TransformerNode() {
    }

    public TransformerNode(Executor executor) {
        super(executor);
    }

	/**
	 * allows post() after close()
	 *
	 * @param <T>
	 */
    protected class StreamOutput<T> extends StreamInput<T> {
        protected void overflow(T token) {
        }
    }
}
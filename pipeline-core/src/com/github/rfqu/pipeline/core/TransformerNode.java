package com.github.rfqu.pipeline.core;

import java.util.concurrent.Executor;

import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.df4j.core.StreamPort;

/**
 * 
 * @author kaigorodov
 *
 * @param <I> type of input messages
 * @param <O> type of output messages
 */
public abstract class TransformerNode<I, O> extends BoltNode
    implements Transformer<I, O>
{
    //----------------- Sink part
    
    /** there input messages return */
    private Port<I> returnPort;

    @Override
    public void setReturnPort(Port<I> returnPort) {
        this.returnPort=returnPort;
    }

    protected void free(I item) {
        returnPort.post(item);
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
}
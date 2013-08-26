package com.github.rfqu.pipeline.core;

import java.util.concurrent.Executor;

import com.github.rfqu.df4j.core.Port;

/**
 * 
 * @author kaigorodov
 *
 * @param <I> type of input messages
 */
public abstract class SinkNode<I> extends BoltNode
    implements Sink<I>
{
    //----------------- Sink part
    
    /** there input messages return */
    private Port<I> returnPort;

    @Override
    public void setReturnPort(Port<I> returnPort) {
        this.returnPort=returnPort;
    }

    public void free(I item) {
        if (returnPort!=null) {
            returnPort.post(item);
        }
    }
    
    //-------------------------
	
    public SinkNode() {
    }

    public SinkNode(Executor executor) {
        super(executor);
    }
}
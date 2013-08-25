package com.github.rfqu.pipeline.core;

import com.github.rfqu.df4j.core.Callback;
import com.github.rfqu.df4j.core.StreamPort;

/**
 * passes input messages through
 * 
 * Left connection should be estatblished before right one
 */
public class CopyTransformer<IO> implements Transformer<IO, IO> {

    //------------------ Bolt part
    protected Callback<Object> context;


    @Override
    public void setContext(Callback<Object> context) {
        this.context = context;
    }

    public void start() {
    }

    public void stop() {
    }

    //----------------- Sink part
    
    /**  here input messages arrive */
    StreamPort<IO> myInputPort=new StreamPort<IO>() {
        @Override
        public void post(IO m) {
            sinkPort.post(m);
        }

        @Override
        public void close() {
            sinkPort.close();
        }

        @Override
        public boolean isClosed() {
            return sinkPort.isClosed();
        }
    };

    @Override
    public StreamPort<IO> getInputPort() {
        return myInputPort;
    }
    
    /** there input messages return */
    protected StreamPort<IO> returnPort;

    @Override
    public void setReturnPort(StreamPort<IO> returnPort) {
        this.returnPort=returnPort;
    }
    
    //----------------- Source part

    /** there output messages go */
    protected StreamPort<IO> sinkPort;

    @Override
    public void setSinkPort(StreamPort<IO> sinkPort) {
        this.sinkPort=sinkPort;
    }
    
    /** here output messages return */
    StreamPort<IO> myReturnPort=new StreamPort<IO>() {
        @Override
        public void post(IO m) {
            returnPort.post(m);
        }

        @Override
        public void close() {
            returnPort.close();
        }

        @Override
        public boolean isClosed() {
            return returnPort.isClosed();
        }
    };

    @Override
    public StreamPort<IO> getReturnPort() {
        return myReturnPort;
    }
}
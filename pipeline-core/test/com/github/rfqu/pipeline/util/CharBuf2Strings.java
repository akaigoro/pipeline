package com.github.rfqu.pipeline.util;

import java.nio.CharBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.pipeline.core.SinkNode;

public abstract class CharBuf2Strings
    extends SinkNode<CharBuffer>
{
    /**  here input messages arrive */
    protected StreamInput<CharBuffer> myInput=new StreamInput<CharBuffer>();
    LinkedBlockingQueue<String> output=new LinkedBlockingQueue<String>();
    
    public CharBuf2Strings() {
    }

    public CharBuf2Strings(Executor executor) {
        super(executor);
    }

    @Override
    public StreamPort<CharBuffer> getInputPort() {
        return myInput;
    }
    
    public boolean isClosed() {
        return myInput.isClosed();
    }
    
    //--------------- Dataflow backend
    
    StringBuilder sb=new StringBuilder();

    void formString() {
        String s=sb.toString();
        try {
            output.put(s);
        } catch (InterruptedException e) {
            // cannot happen for LinkedBlockingQueue
        }
        sb=new StringBuilder();
    }

    @Override
    protected void act() {
        while (!myInput.isClosed()) {
            CharBuffer inbuf=myInput.get();
            while (inbuf.hasRemaining()) {
                char c = inbuf.get();
                if (c=='\n') {
                    formString();
                } else {
                    sb.append(c);
                }
            }
            free(inbuf);
            if (!myInput.moveNext()) {
                return;
            }
        }
        formString();
    }
}
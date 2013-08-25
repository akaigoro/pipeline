package com.github.rfqu.pipeline.core;

import java.nio.Buffer;
import java.nio.charset.CoderResult;
import java.util.concurrent.Executor;

import com.github.rfqu.df4j.core.StreamPort;

public abstract class BufTransformer<I extends Buffer, O extends Buffer>
    extends TransformerNode<I, O>
{
    /**  here input messages arrive */
    protected StreamInput<I> myInput=new StreamInput<I>();

    /** here output messages return */
    protected StreamOutput<O> myOutput=new StreamOutput<O>();

    public BufTransformer() {
    }

    public BufTransformer(Executor executor) {
        super(executor);
    }

    @Override
    public StreamPort<I> getInputPort() {
        return myInput;
    }
    
    @Override
    public StreamPort<O> getReturnPort() {
        return myOutput;
    }
    
    @Override
    protected void act() {
        
        I inbuf=myInput.get();
        O outbuf=myOutput.get();
        outbuf.clear();
        // processing loop:
        for (;;) {
            if (inbuf==null) {
                break;
            }
            
            CoderResult res=transform(inbuf, outbuf);

            if (res.isUnderflow()) {
                returnPort.post(inbuf);  // free inbuf
                if (!myInput.moveNext()) {
                    if (outbuf.position()==0) {
                        myOutput.pushback();
                    } else {
                        // send outbuf
                        outbuf.flip();
                        sinkPort.post(outbuf);
                    }
                    return;
                }
                inbuf=myInput.get();
            }
            if (res.isOverflow()) {
                // send outbuf
                outbuf.flip();
                sinkPort.post(outbuf);
                if (!myOutput.moveNext()) {
                    myInput.pushback();
                    return;
                }
                outbuf=myOutput.get();
                outbuf.clear();
            }
        }
        // completing loop
        for (;;) {
            CoderResult res = complete(outbuf);
            if (res.isUnderflow()) {
                // failed to complete
                myInput.pushback();
                // send outbuf
                outbuf.flip();
                sinkPort.post(outbuf);
                if (!myOutput.moveNext()) {
                    myInput.pushback();
                    return;
                }
                outbuf=myOutput.get();
                outbuf.clear();
            }
            break;
        }
        sinkPort.close();
        // delete own buffers
        myOutput.close();
        while (myOutput.moveNext());
    }

    /**
     * gets two usable buffers
     * @param inbuf
     * @param outbuf
     * @return bit mask with at least one bit set
     *    if UNDERFLOW is set, new inbuf required
     *    if OVERFLOW  is set, new outbuf required
     */
    protected abstract CoderResult transform(I inbuf, O outbuf);
    
    
    /**
     * input is closed; pass closing signal further
     * @param outbuf 
     * @param outmessage 
     * @return 
     */
    protected abstract CoderResult complete(O outbuf);

}
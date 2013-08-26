package com.github.rfqu.pipeline.util;

import java.nio.CharBuffer;

import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.pipeline.core.BoltBase;
import com.github.rfqu.pipeline.core.Sink;

public class CharBufSink extends BoltBase
    implements Sink<CharBuffer>
{
    
    /** there input messages return */
    protected Port<CharBuffer> returnPort;

    private boolean isClosed;

    @Override
    public void setReturnPort(Port<CharBuffer> returnPort) {
        this.returnPort=returnPort;
    }

    /**  here input messages arrive */
    StreamPort<CharBuffer> myInput=new StreamPort<CharBuffer>() {
        StringBuilder sb=new StringBuilder();
        
        @Override
        public void post(CharBuffer buf) {
            if (isClosed()) {
                throw new IllegalStateException("Sink already closed");
            }
            while (buf.hasRemaining()) {
                char c = buf.get();
                sb.append(c);
            }
            returnPort.post(buf);
        }

        @Override
        public void close() {
            isClosed=true;
            context.post(sb.toString());
        }

        public boolean isClosed() {
            return isClosed;
        }
    };
    
    @Override
    public StreamPort<CharBuffer> getInputPort() {
        return myInput;
    }
}
package com.github.rfqu.pipeline.util;

import java.nio.CharBuffer;

import com.github.rfqu.df4j.core.Callback;
import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.pipeline.core.Bolt;
import com.github.rfqu.pipeline.core.Sink;

/**  here input messages arrive */
public class CharBufSink 
    implements Bolt, Sink<CharBuffer>, StreamPort<CharBuffer>
{
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

    //----------------------- Sink part
    
    /** there input messages return */
    protected StreamPort<CharBuffer> returnPort;

    private boolean isClosed;

    @Override
    public void setReturnPort(StreamPort<CharBuffer> returnPort) {
        this.returnPort=returnPort;
    }

    @Override
    public StreamPort<CharBuffer> getInputPort() {
        return this;
    }
	
    //------------ StreamPort part
    
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
}
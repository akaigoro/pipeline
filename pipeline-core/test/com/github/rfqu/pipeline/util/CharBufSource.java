package com.github.rfqu.pipeline.util;

import java.nio.CharBuffer;

import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.pipeline.core.BoltBase;
import com.github.rfqu.pipeline.core.Source;

public class CharBufSource extends BoltBase implements Source<CharBuffer>
{
    /** there output messages go */
    protected StreamPort<CharBuffer> sinkPort;
    
    public void setSinkPort(StreamPort<CharBuffer> sinkPort) {
        this.sinkPort=sinkPort;
    }
    
    public StreamPort<CharBuffer> getReturnPort() {
        return null; // no return required
    }

	public void post(String s) {
		CharBuffer buf=CharBuffer.wrap(s);
		sinkPort.post(buf);
	}

	public void close() {
        sinkPort.close();
    }

    public void postFailure(Throwable exc) {
        context.postFailure(exc);
    }
}
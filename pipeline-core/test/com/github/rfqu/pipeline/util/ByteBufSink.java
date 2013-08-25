package com.github.rfqu.pipeline.util;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.ExecutionException;

import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.df4j.core.Callback;
import com.github.rfqu.df4j.core.CompletableFuture;
import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.pipeline.core.Bolt;
import com.github.rfqu.pipeline.core.Sink;

public class ByteBufSink 
    implements Bolt, Sink<ByteBuffer>, StreamPort<ByteBuffer>
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
    protected StreamPort<ByteBuffer> returnPort;

    private boolean isClosed;

    @Override
    public void setReturnPort(StreamPort<ByteBuffer> returnPort) {
        this.returnPort=returnPort;
    }

    @Override
    public StreamPort<ByteBuffer> getInputPort() {
        return this;
    }
    
    //------------ StreamPort part
    ByteArrayOutputStream bac=new ByteArrayOutputStream();
    CompletableFuture<byte[]> futt=new CompletableFuture<byte[]>();
	
	@Override
	public void post(ByteBuffer buf) {
		while (buf.hasRemaining()) {
			bac.write(buf.get());
		}
        returnPort.post(buf);
	}

	@Override
	public void close() {
		futt.post(bac.toByteArray());
	}

	@Override
	public boolean isClosed() {
		return futt.isDone();
	}

	public byte[] get() throws InterruptedException, ExecutionException {
		return futt.get();
	}
}
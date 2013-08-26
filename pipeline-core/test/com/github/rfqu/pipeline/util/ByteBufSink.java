package com.github.rfqu.pipeline.util;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import com.github.rfqu.df4j.core.CompletableFuture;
import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.pipeline.core.BoltBase;
import com.github.rfqu.pipeline.core.Sink;

public class ByteBufSink extends BoltBase implements Sink<ByteBuffer> {
    
    /** there input messages return */
    protected Port<ByteBuffer> returnPort;

    @Override
    public void setReturnPort(Port<ByteBuffer> returnPort) {
        this.returnPort=returnPort;
    }

    CompletableFuture<byte[]> futt=new CompletableFuture<byte[]>();
    ByteArrayOutputStream bac=new ByteArrayOutputStream();

    StreamPort<ByteBuffer> myInput=new StreamPort<ByteBuffer>() {
        
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
    };

    @Override
    public StreamPort<ByteBuffer> getInputPort() {
        return myInput;
    }

	public byte[] get() throws InterruptedException, ExecutionException {
		return futt.get();
	}
}
package com.github.rfqu.pipeline.util;

import java.nio.ByteBuffer;

import com.github.rfqu.df4j.core.Callback;
import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.pipeline.core.Bolt;
import com.github.rfqu.pipeline.core.Source;

public class ByteBufSource 
    implements Bolt, Source<ByteBuffer>, StreamPort<byte[]>
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

    //------------------ Source part

    /** there output messages go */
    protected StreamPort<ByteBuffer> sinkPort;
    
    /** here output messages return */
    protected StreamPort<ByteBuffer> myOutput=new StreamPort<ByteBuffer>(){

        @Override
        public void post(ByteBuffer m) {
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isClosed() {
            return false;
        }
    };

    public void setSinkPort(StreamPort<ByteBuffer> sinkPort) {
        this.sinkPort=sinkPort;
    }
    
    public StreamPort<ByteBuffer> getReturnPort() {
        return myOutput;
    }

    public void post(byte[] data) {
        ByteBuffer buf=ByteBuffer.wrap(data);
        sinkPort.post(buf);
    }

    public void close() {
        sinkPort.close();
    }


	@Override
	public boolean isClosed() {
		return sinkPort.isClosed();
	}
}
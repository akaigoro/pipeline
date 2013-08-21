package com.github.rfqu.df4j.pipeline.util;

import java.nio.ByteBuffer;

import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.df4j.pipeline.BufChunk;

public class ByteChunkSource implements StreamPort<byte[]> {
	private StreamPort<BufChunk<ByteBuffer>> demand;

	public ByteChunkSource(StreamPort<BufChunk<ByteBuffer>> port) {
		demand=port;
	}

	@Override
	public void post(byte[] data) {
		ByteBuffer buf=ByteBuffer.wrap(data);
		BufChunk<ByteBuffer> chunk=new BufChunk<ByteBuffer>(buf, null);
        demand.post(chunk);
	}

	@Override
	public void close() {
	    demand.close();
	}

	@Override
	public boolean isClosed() {
		return demand.isClosed();
	}
}
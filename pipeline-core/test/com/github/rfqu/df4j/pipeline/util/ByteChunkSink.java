package com.github.rfqu.df4j.pipeline.util;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.df4j.core.CompletableFuture;
import com.github.rfqu.df4j.pipeline.BufChunk;

public class ByteChunkSink implements ActorPort<BufChunk<ByteBuffer>> {
	ByteArrayOutputStream bac=new ByteArrayOutputStream();
    CompletableFuture<byte[]> futt=new CompletableFuture<byte[]>();
	
	@Override
	public void post(BufChunk<ByteBuffer> chunk) {
		ByteBuffer it=chunk.getBuffer();
		while (it.hasRemaining()) {
			bac.write(it.get());
		}
		chunk.free();
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

    @Override
    public void postFailure(Throwable exc) {
        futt.postFailure(exc);
    }
}
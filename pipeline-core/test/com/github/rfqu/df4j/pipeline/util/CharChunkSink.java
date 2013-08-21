package com.github.rfqu.df4j.pipeline.util;

import java.nio.CharBuffer;
import java.util.concurrent.ExecutionException;

import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.df4j.core.CompletableFuture;
import com.github.rfqu.df4j.pipeline.BufChunk;

public class CharChunkSink implements ActorPort<BufChunk<CharBuffer>> {
	StringBuilder sb=new StringBuilder();
    CompletableFuture<String> futt=new CompletableFuture<String>();
	
	@Override
	public void post(BufChunk<CharBuffer> chunk) {
		CharBuffer it=chunk.getBuffer();
		while (it.hasRemaining()) {
			sb.append(it.get());
		}
		chunk.free();
	}

	@Override
	public void close() {
	    if (futt.isDone()) {
	        return;
	    }
	    futt.post(sb.toString());
	}

	@Override
	public boolean isClosed() {
		return futt.isDone();
	}

	public String get() throws InterruptedException, ExecutionException {
		return futt.get();
	}
	
    @Override
    public void postFailure(Throwable exc) {
        futt.postFailure(exc);
    }
}
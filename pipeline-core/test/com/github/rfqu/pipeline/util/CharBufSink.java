package com.github.rfqu.pipeline.util;

import java.nio.CharBuffer;
import java.util.concurrent.ExecutionException;

import com.github.rfqu.df4j.core.CompletableFuture;
import com.github.rfqu.pipeline.core.ErrorSink;

public class CharBufSink extends ErrorSink<CharBuffer> {
	StringBuilder sb=new StringBuilder();
    CompletableFuture<String> futt=new CompletableFuture<String>();
	
	@Override
	public void post(CharBuffer buf) {
		if (isClosed()) {
			throw new IllegalStateException("Sink already closed");
		}
		while (buf.hasRemaining()) {
			sb.append(buf.get());
		}
		free(buf);
	}

	protected void free(CharBuffer buf) {
		super.source.recycle(buf);
	}

	@Override
	public void close() {
	    if (futt.isDone()) {
	        return;
	    }
	    futt.post(sb.toString());
	}

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
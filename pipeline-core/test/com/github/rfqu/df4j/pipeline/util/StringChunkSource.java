package com.github.rfqu.df4j.pipeline.util;

import java.nio.CharBuffer;

import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.df4j.pipeline.BufChunk;

public class StringChunkSource implements ActorPort<String> {
	private ActorPort<BufChunk<CharBuffer>> outPort;

	public StringChunkSource(ActorPort<BufChunk<CharBuffer>> outPort) {
		this.outPort=outPort;
	}

	@Override
	public void post(String string) {
		outPort.post(BufChunk.wrap(string, null));
	}

	@Override
	public void close() {
	    outPort.close();
	}

	@Override
	public boolean isClosed() {
		return outPort.isClosed();
	}

    @Override
    public void postFailure(Throwable exc) {
        outPort.postFailure(exc);
    }
}
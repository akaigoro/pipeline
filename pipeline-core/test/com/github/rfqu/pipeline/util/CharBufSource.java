package com.github.rfqu.pipeline.util;

import java.nio.CharBuffer;

import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.pipeline.core.Sink;
import com.github.rfqu.pipeline.core.Source;

public class CharBufSource implements ActorPort<String>, Source<CharBuffer> {
	/** there output messages go */
	protected Sink<CharBuffer> sink;
	protected boolean isShut;

	@Override
	public void setSink(Sink<CharBuffer> sink) {
		if (this.sink != null) {
			return; // avoid infinite recursion
		}
		this.sink = sink;
		sink.setSource(this);
	}

	@Override
	public void post(String s) {
		CharBuffer buf=CharBuffer.wrap(s);
		if (sink==null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sink.post(buf);
	}

	@Override
	public void close() {
		sink.close();
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	public void postFailure(Throwable exc) {
		sink.postFailure(exc);
	}
	
	@Override
	public void recycle(CharBuffer message) {
	}

	@Override
	public void shut() {
		isShut=true;
	}
}
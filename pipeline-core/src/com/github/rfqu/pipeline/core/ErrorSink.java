package com.github.rfqu.pipeline.core;

import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.df4j.core.StreamPort;

/**
 * Example of pipeline end.
 * Prints error messages.
 * 
 * @author kaigorodov
 *
 */
public class ErrorSink<I> implements Sink<I> {
	/** there input messages return */
	protected Source<I> source;
	protected boolean isClosed;

	/** there output messages go */
	protected Sink<I> sink;

	@Override
	public void setSource(Source<I> source) {
		if (this.source != null) {
			return; // avoid infinite recursion
		}
		this.source = source;
		source.setSink(this);
	}


	@Override
	public void post(I m) {
		System.err.println("values not expected");
	}

	@Override
	public void postFailure(Throwable exc) {
		exc.printStackTrace();
	}

	@Override
	public void close() {
		isClosed=true;
	}

}

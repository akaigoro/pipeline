package com.github.rfqu.pipeline.core;

/**
 * passes input messages through
 * 
 * Left connection should be estatblished before right one
 */
public class CopyTransformer<IO> implements Sink<IO>, Source<IO> {
	/** there input messages return */
	protected Source<IO> source;

	/** there output messages go */
	protected Sink<IO> sink;

	@Override
	public void setSource(Source<IO> source) {
		if (this.source != null) {
			return; // avoid infinite recursion
		}
		this.source = source;
		source.setSink(this);
	}

	@Override
	public void post(IO m) {
		sink.post(m);
	}

	@Override
	public void postFailure(Throwable exc) {
		sink.postFailure(exc);
	}

	public void close() {
		sink.close();
	}

	public void setSink(Sink<IO> sink) {
		if (this.sink != null) {
			return; // avoid infinite recursion
		}
		this.sink = sink;
		sink.setSource(this);
	}

	@Override
	public void recycle(IO message) {
		source.recycle(message);
	}

	@Override
	public void shut() {
		source.shut();
	}

}
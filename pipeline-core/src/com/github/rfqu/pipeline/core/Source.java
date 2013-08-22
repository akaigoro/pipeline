package com.github.rfqu.pipeline.core;

public interface Source<O> {
	
	void setSink(Sink<O> sink);

	/** returns processed message */
	public void recycle(O message);

	/** backward close, processed differently than Sink.close() */
	public void shut();

}

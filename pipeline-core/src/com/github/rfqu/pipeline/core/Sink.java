package com.github.rfqu.pipeline.core;

public interface Sink<I> {
	public void setSource(Source<I> source);

	public void post(I message);

	public void close();
	
    /**
     * signals failure 
     */
    public void postFailure(Throwable exc);
}

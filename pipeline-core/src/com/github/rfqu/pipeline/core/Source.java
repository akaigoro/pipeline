package com.github.rfqu.pipeline.core;

import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.df4j.core.StreamPort;

public interface Source<O>  extends Bolt {

    void setSinkPort(StreamPort<O> sinkPort);

    public Port<O> getReturnPort();

	public void close();

}

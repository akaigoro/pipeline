package com.github.rfqu.pipeline.core;

import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.df4j.core.StreamPort;

public interface Sink<I> extends Bolt {

    public void setReturnPort(Port<I> returnPort);

    public StreamPort<I> getInputPort();

}

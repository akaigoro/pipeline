package com.github.rfqu.pipeline.core;

import com.github.rfqu.df4j.core.Callback;

public interface Bolt {

    public void setContext(Callback<Object> context);
    
    public void start();

    public void stop();

}

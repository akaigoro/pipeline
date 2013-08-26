package com.github.rfqu.pipeline.core;

import com.github.rfqu.df4j.core.Callback;

public class BoltBase implements Bolt {
    protected Callback<Object> context;

    @Override
    public void setContext(Callback<Object> context) {
        this.context = context;
    }

    public void start() {
    }

    public void stop() {
    }
}
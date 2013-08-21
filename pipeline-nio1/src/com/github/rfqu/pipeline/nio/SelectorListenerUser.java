package com.github.rfqu.pipeline.nio;

import java.nio.channels.SelectableChannel;

public interface SelectorListenerUser {
    SelectableChannel getChannel();
    void close();
}

package com.github.rfqu.pipeline.nio;

import com.github.rfqu.pipeline.nio.AsyncChannelFactory;
import com.github.rfqu.pipeline.nio.AsyncChannelFactory1;
import com.github.rfqu.pipline.nio.LimitedServerTest;


public class LimitedServerTest1 extends LimitedServerTest {
    {
        AsyncChannelFactory.setCurrentAsyncChannelFactory(new AsyncChannelFactory1());
    }
}
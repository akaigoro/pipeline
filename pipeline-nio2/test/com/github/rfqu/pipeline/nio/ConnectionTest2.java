package com.github.rfqu.pipeline.nio;

import com.github.rfqu.pipeline.nio.AsyncChannelFactory2;
import com.github.rfqu.pipline.nio.ConnectionTest;


public class ConnectionTest2 extends ConnectionTest {
    { channelFactory=new AsyncChannelFactory2();
    }
}
package com.github.rfqu.pipeline.nio;

import com.github.rfqu.pipeline.nio.AsyncChannelFactory;
import com.github.rfqu.pipeline.nio.AsyncChannelFactory1;
import com.github.rfqu.pipline.nio.ConnectionTest;


public class ConnectionTest1 extends ConnectionTest {
   {
       AsyncChannelFactory.setCurrentAsyncChannelFactory(new AsyncChannelFactory1());
   }
}
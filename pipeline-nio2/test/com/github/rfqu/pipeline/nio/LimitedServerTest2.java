package com.github.rfqu.pipeline.nio;

import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.ext.ImmediateExecutor;
import com.github.rfqu.pipeline.nio.AsyncChannelFactory2;
import com.github.rfqu.pipline.nio.LimitedServerTest;

public class LimitedServerTest2 extends LimitedServerTest {
    {channelFactory=new AsyncChannelFactory2();
    }

    
    public static void main(String[] args) throws Exception {
    	DFContext.setCurrentExecutor(new ImmediateExecutor());
    	new LimitedServerTest2().smokeTest();
    }
    
}
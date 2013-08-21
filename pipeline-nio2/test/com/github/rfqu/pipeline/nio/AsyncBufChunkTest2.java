package com.github.rfqu.pipeline.nio;

import com.github.rfqu.pipeline.nio.AsyncChannelFactory;
import com.github.rfqu.pipeline.nio.AsyncChannelFactory2;
import com.github.rfqu.pipline.nio.AsyncBufChunkTest;

public class AsyncBufChunkTest2 extends AsyncBufChunkTest {
    {
        AsyncChannelFactory2 chf = new AsyncChannelFactory2();
        AsyncChannelFactory.setCurrentAsyncChannelFactory(chf);
  }

}
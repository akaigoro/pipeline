package com.github.rfqu.pipeline.nio;

import java.io.IOException;

import com.github.rfqu.pipeline.nio.AsyncChannelFactory;
import com.github.rfqu.pipeline.nio.AsyncServerSocketChannel;
import com.github.rfqu.pipeline.nio.AsyncSocketChannel;

public class AsyncChannelFactory2 extends AsyncChannelFactory {

    @Override
    public AsyncServerSocketChannel newAsyncServerSocketChannel()
        throws IOException
    {
        return new AsyncServerSocketChannel2();
    }

    @Override
    public AsyncSocketChannel newAsyncSocketChannel()
        throws IOException
    {
        return new AsyncSocketChannel2();
    }

}
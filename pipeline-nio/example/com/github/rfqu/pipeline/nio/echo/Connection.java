package com.github.rfqu.pipeline.nio.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.github.rfqu.df4j.core.Callback;
import com.github.rfqu.df4j.core.ListenableFuture;
import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.df4j.core.Timer;
import com.github.rfqu.df4j.core.DataflowVariable.Semafor;
import com.github.rfqu.df4j.core.DataflowVariable.StreamInput;
import com.github.rfqu.df4j.testutil.DoubleValue;
import com.github.rfqu.pipeline.core.Pipeline;
import com.github.rfqu.pipeline.core.SinkNode;
import com.github.rfqu.pipeline.nio.AsyncServerSocketChannel;
import com.github.rfqu.pipeline.nio.AsyncSocketChannel;
import com.github.rfqu.pipeline.nio.echo.EchoServer.EchoPipeline;
import com.github.rfqu.pipeline.nio.echo.EchoServer.Reactor;

/** 2-way socket connection
 * 
 * @author Alexei Kaigorodov
 *
 */
class Connection {
    public static final int defaultPort = 8007;
    public static final int BUF_SIZE = 128;

    AsyncSocketChannel channel;
    Pipeline reader = new Pipeline();
    Pipeline writer = new Pipeline();
    ListenableFuture<Object> future = reader.getFuture();
    
    public Connection(AsyncSocketChannel channel) throws IOException {
        this.channel=channel;
        reader.setSource(channel.reader).setSink(reactor);
        reactor.connCount.up(maxConn);
    }

    public ListenableFuture<Object> start() {
        reader.start();
        return future;
   }

    public void close() {
        reader.stop();
        channel.close();
    }

    public ListenableFuture<Object> getFuture() {
        return future;
    }

    /**
     * accepted connections, formatted as {@link AsyncSocketChannel},
     * arrive to {@link myInput}.
     * For each connection, echoing pipline is created.
     * After connections, they should be returned to the peer {@link AsyncServerSocketChannel}.
     */
    class Reactor extends SinkNode<AsyncSocketChannel> {
        protected Semafor connCount=new Semafor();
        
        protected StreamInput<AsyncSocketChannel> myInput=new StreamInput<AsyncSocketChannel>();

        @Override
        public StreamPort<AsyncSocketChannel> getInputPort() {
            return myInput;
        }

        @Override
        protected void act() {
            AsyncSocketChannel channel=myInput.get();
            EchoPipeline pipeline = new EchoPipeline(channel);
            pipeline.start();
        }
    }

    /** serves one client, echoing input packets
     */
    class EchoPipeline extends Pipeline implements Callback<Object> {
        AsyncSocketChannel channel;
        
        public EchoPipeline(AsyncSocketChannel channel) {
            this.channel=channel;
            setSource(channel.reader).setSink(channel.writer);
            super.getFuture().addListener(this);
        }

        @Override
        public void post(Object message) {
            reactor.free(channel);
            channel=null;
        }

        @Override
        public void postFailure(Throwable exc) {
            exc.printStackTrace();
            reactor.free(channel);
            channel=null;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("classPath=" + System.getProperty("java.class.path"));
        Integer port;
        if (args.length >= 1) {
            port = Integer.valueOf(args[0]);
        } else {
            port = defaultPort;
        }
        int maxConn;
        if (args.length >= 2) {
            maxConn = Integer.valueOf(args[1]);
        } else {
            maxConn = 1000;
        }
        SocketAddress addr = new InetSocketAddress("localhost", port);
        
        EchoServer es = new EchoServer(addr, maxConn);
        ListenableFuture<Object> future = es.start();
        future.get();

        // inet addr is free now
        System.out.println("EchoServer started");
    }
}
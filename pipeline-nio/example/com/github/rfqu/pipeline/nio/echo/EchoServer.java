package com.github.rfqu.pipeline.nio.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.github.rfqu.df4j.core.Callback;
import com.github.rfqu.df4j.core.ListenableFuture;
import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.pipeline.core.Pipeline;
import com.github.rfqu.pipeline.core.SinkNode;
import com.github.rfqu.pipeline.nio.AsyncServerSocketChannel;
import com.github.rfqu.pipeline.nio.AsyncSocketChannel;

/**
* To run tests, {@see EchoServerLockTest} and {@see EchoServerGlobTest}.
*/
public class EchoServer  {
    public static final int defaultPort = 8007;
    public static final int BUF_SIZE = 128;

    AsyncServerSocketChannel acceptor;
    Reactor reactor=new Reactor();
    Pipeline pipeline = new Pipeline();
    ListenableFuture<Object> future = pipeline.getFuture();
    
    public EchoServer(SocketAddress addr, int maxConn) throws IOException {
        acceptor=new AsyncServerSocketChannel(addr, maxConn);
        pipeline.setSource(acceptor).setSink(reactor);
        reactor.connCount.up(maxConn);
    }

    public ListenableFuture<Object> start() {
        pipeline.start();
        return future;
   }

    public void close() {
        pipeline.stop();
        acceptor.close();
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

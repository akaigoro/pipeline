package com.github.rfqu.pipline.nio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.df4j.core.ListenableFuture;
import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.pipeline.core.BufChunk;
import com.github.rfqu.pipeline.core.util.ByteChunkSink;
import com.github.rfqu.pipeline.nio.AsyncBufChunkSink;
import com.github.rfqu.pipeline.nio.AsyncBufChunkSource;
import com.github.rfqu.pipeline.nio.AsyncChannelFactory;
import com.github.rfqu.pipeline.nio.AsyncServerSocketChannel;
import com.github.rfqu.pipeline.nio.AsyncSocketChannel;
import com.github.rfqu.pipeline.nio.SocketIORequest;

public class AsyncBufChunkTest {
    static final int BUF_SIZE = 128;
//  static final InetSocketAddress local9990 = new InetSocketAddress("localhost", 9990);
  static final InetSocketAddress local9990 = new InetSocketAddress("localhost", 8007);

  AsyncChannelFactory channelFactory;
  AsyncServerSocketChannel assc;
  
  @Before
  public void init() throws IOException, InterruptedException, ExecutionException {
      channelFactory=AsyncChannelFactory.getCurrentAsyncChannelFactory();
      assc = channelFactory.newAsyncServerSocketChannel();
      assc.bind(local9990);
      
      ListenableFuture<AsyncSocketChannel> connectionEvent = assc.accept();
      clientConn=channelFactory.newAsyncSocketChannel();
      clientConn.connect(local9990);

      Thread.sleep(50);

      assertTrue(clientConn.getConnEvent().isDone());
      assertTrue(connectionEvent.isDone());
      serverConn=connectionEvent.get();
  }

  @After
  public void close() {
      assc.close();
  }
  
  AsyncSocketChannel serverConn;
  AsyncSocketChannel clientConn;
  
  /**
   * send a message from client to server 
   */
  @Test
  public void smokeTest() throws Exception {
      // server writes here
      AsyncBufChunkSink serverSink=new AsyncBufChunkSink(serverConn);
      
      WriteImitator imitator=new WriteImitator(serverSink);
      // server reads here
      AsyncBufChunkSource serverSource=new AsyncBufChunkSource(serverConn, imitator);
      serverSource.injectChunks(1, 20);
 
      // client writes here 
      AsyncBufChunkSink clientSink=new AsyncBufChunkSink(clientConn);
      
      // result accumulated
      ByteChunkSink res=new ByteChunkSink();
      // client reads here
      AsyncBufChunkSource clientSource=new AsyncBufChunkSource(clientConn, res);
      clientSource.injectChunks(1,10);
      
      postChunk("hello",  clientSink);
      byte[] bytes = res.get();
      String resStr=new String(bytes);
      System.out.println("resStr="+resStr);
  }
  
  static void postChunk(String s, Port<BufChunk<ByteBuffer>> port) {
      byte[] bytes=s.getBytes();
      ByteBuffer buf = ByteBuffer.wrap(bytes);
      BufChunk<ByteBuffer> chunk =  new BufChunk<ByteBuffer>(buf, null);
      buf.position(buf.limit());
      port.post(chunk);
  }
  
  static class WriteImitator implements ActorPort<BufChunk<ByteBuffer>> {
      ActorPort<BufChunk<ByteBuffer>> consumer;

    public WriteImitator(ActorPort<BufChunk<ByteBuffer>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void close() {
        consumer.close();
    }

    @Override
    public boolean isClosed() {
        // TODO Auto-generated method stub
        return consumer.isClosed();
    }

    @Override
    public void post(BufChunk<ByteBuffer> m) {
        ByteBuffer buf=m.getBuffer();
        buf.position(buf.limit());
        consumer.post(m);
    }

    @Override
    public void postFailure(Throwable exc) {
        consumer.postFailure(exc);
    }
      
  }
}
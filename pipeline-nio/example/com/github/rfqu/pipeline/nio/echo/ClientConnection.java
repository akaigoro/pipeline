package com.github.rfqu.pipeline.nio.echo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.df4j.core.Timer;
import com.github.rfqu.df4j.testutil.DoubleValue;
import com.github.rfqu.pipeline.core.BoltBase;
import com.github.rfqu.pipeline.core.Pipeline;
import com.github.rfqu.pipeline.core.Sink;
import com.github.rfqu.pipeline.nio.AsyncSocketChannel;
import com.github.rfqu.pipeline.util.ByteBuf2String;
import com.github.rfqu.pipeline.util.String2ByteBuf;

/**
 * Implements a client of Echo-server.
 * Sends messages and compare replies.
 * 
 * @author kaigorodov
 *
 */
class ClientConnection {
    static final long timeout = 1000;// ms
    static AtomicInteger ids = new AtomicInteger(); // DEBUG
    public static final int BUF_SIZE = 128;

    private final Timer timer = Timer.getCurrentTimer();
    public int id = EchoClient.ids.addAndGet(1);
    public int serverId;
    AtomicLong rounds;
    Random rand = new Random();
    long sum = 0;
    int count1startWrite = 0;
    int count2endWrite = 0;
    int count3endRead = 0;
    long writeTime;
    String lastMessage;
    Port<String> writePort = new Port<String>() {
        public void post(String message) {
            write(message);
        }
    };
    protected AsyncSocketChannel channel;
    Pipeline inbound = new Pipeline();
    Pipeline outbound = new Pipeline();
    String2ByteBuf stringSource = new String2ByteBuf();
    RelpyHandler stringSink = new RelpyHandler();

    public ClientConnection(InetSocketAddress addr, int rounds) throws IOException {
        this.channel=new AsyncSocketChannel(addr);
        
        inbound.setSource(channel.reader)
          .addTransformer(new ByteBuf2String())
          .setSink(stringSink);
        
        outbound.setSource(stringSource)
          .setSink(channel.writer);
        this.rounds = new AtomicLong(rounds);
    }

    public void start() {
        channel.reader.injectBuffers(2, BUF_SIZE);
        inbound.start();
        outbound.start();
        write("firstMessage\n");
   }

    public void close() {
        inbound.close();
        outbound.close();
    }

    public void write(String message) {
        count1startWrite++;
        lastMessage = message;
        writeTime = System.currentTimeMillis();
        stringSource.post(message);
    }

    class RelpyHandler extends BoltBase implements Sink<String> {

        /**  here input messages arrive */
        protected StreamPort<String> myInput=new StreamPort<String>() {

            @Override
            public void post(String message) {
                assertEquals(message+"\n", lastMessage);
                count3endRead++;
                // System.err.println("  client Request read ended; id="+id+" rid="+request.rid+" count="+count);
                long currentTime = System.currentTimeMillis();
                sum += (currentTime - writeTime);
                long r = rounds.decrementAndGet();
                if (r == 0) {
                    // System.out.println("SocketIORequest finished id="+id);
                    ClientConnection.this.close();
                    DoubleValue avg = new DoubleValue(((double) sum) / count3endRead);
                    context.post(avg);
                    // System.out.println("clients="+echoServerTest.clients.size());
                    return;
                }
                long targetTime = writeTime + EchoClient.PERIOD;
                if (targetTime > currentTime) {
                    // if (false) {
                    timer.schedule(writePort, message, EchoClient.PERIOD);
                } else {
                    // write it back immediately
                    write(message+"\n");
                }
            }

            @Override
            public void close() {
            }

            @Override
            public boolean isClosed() {
                return false;
            }
            
        };

        @Override
        public StreamPort<String> getInputPort() {
            return myInput;
        }
        public boolean isClosed() {
            return myInput.isClosed();
        }
     
        @Override
        public void setReturnPort(Port<String> returnPort) {
        }
    }
}

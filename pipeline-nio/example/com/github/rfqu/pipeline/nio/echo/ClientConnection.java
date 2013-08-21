package com.github.rfqu.pipeline.nio.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.github.rfqu.df4j.core.ListenableFuture;
import com.github.rfqu.df4j.core.Timer;
import com.github.rfqu.df4j.testutil.DoubleValue;
import com.github.rfqu.pipeline.nio.AsyncSocketChannel;
import com.github.rfqu.pipeline.nio.IOHandler;
import com.github.rfqu.pipeline.nio.SocketIORequest;

class ClientConnection {
    static final long timeout=1000;// ms
    static AtomicInteger ids=new AtomicInteger(); // DEBUG

    private EchoServerTest echoServerTest;
    private final Timer timer;
    public int id=EchoServerGlobTest.ids.addAndGet(1);
    public int serverId;
    AsyncSocketChannel channel;
    CliRequest request;
    AtomicLong rounds;
    Random rand=new Random();
    long sum=0;
    int count1startWrite=0;
    int count2endWrite=0;
    int count3endRead=0;

    public ClientConnection(EchoServerTest echoServerTest, InetSocketAddress addr, int rounds) throws IOException {
        this.echoServerTest = echoServerTest;
        this.timer = echoServerTest.timer;
        this.rounds=new AtomicLong(rounds);
        channel=echoServerTest.asyncChannelFactory.newAsyncSocketChannel();
        channel.connect(addr);
        ByteBuffer buffer = ByteBuffer.allocate(EchoServerGlobTest.BUF_SIZE);
        request=new CliRequest(buffer);
//        channel.read(request, endRead1, timeout);
        request.post(0);
        request.addListener(startWrite);
    }

	public ListenableFuture<AsyncSocketChannel> getConnEvent() {
		return channel.getConnEvent();
	}

    /** starts write operation
     */
	IOHandler<CliRequest> startWrite = new IOHandler<CliRequest>() {
        @Override
		public void completed(int result, CliRequest request) {// throws Exception {
            count1startWrite++;
            request.start = System.currentTimeMillis();
            request.data = rand.nextInt();
            ByteBuffer buffer = request.getBuffer();
            buffer.clear();
            buffer.putInt(request.data);
            channel.write(request, timeout);
            request.addListener(endWrite);
        }
    };

    IOHandler<CliRequest> endWrite = new IOHandler<CliRequest>() {
        @Override
        public void completed(int result, CliRequest request) {//throws ClosedChannelException {
            count2endWrite++;
//            System.err.println("  client Request write ended, id="+id+" rid="+request.rid);
            channel.read(request, timeout);
            request.addListener(endRead);
//            System.err.println("client Request read started id="+id+" rid="+request.rid);
        }

        @Override
        public void timedOut(CliRequest request) {
            System.err.println("endWrite.timedOut!");
            Thread.dumpStack();
        }       
    };
    
    IOHandler<CliRequest> endRead = new IOHandler<CliRequest>() {
        @Override
        public void completed(int result, CliRequest request) {
            count3endRead++;
//            System.err.println("  client Request read ended; id="+id+" rid="+request.rid+" count="+count);
            // read client's message
            request.checkData();
            long currentTime = System.currentTimeMillis();
            sum+=(currentTime-request.start);
            rounds.decrementAndGet();
            if (rounds.get()==0) {
//                System.out.println("SocketIORequest finished id="+id);
                channel.close();
                DoubleValue avg = new DoubleValue(((double)sum)/count3endRead);
                echoServerTest.clientFinished(ClientConnection.this, avg);
//                System.out.println("clients="+echoServerTest.clients.size());
                return;
            }
            long targetTime=request.start+EchoServerGlobTest.PERIOD;
            if (targetTime>currentTime) {
//            if (false) {
                timer.schedule(startWrite, request, EchoServerGlobTest.PERIOD);
            } else {
                // write it back immediately
                startWrite.post(request);
            }
        }

        @Override
        public void timedOut(CliRequest request) {
            System.err.println("endRead.timedOut!");
            Thread.dumpStack();
        }
    };

    static class CliRequest extends SocketIORequest<CliRequest> {
        long start;
        int data;

        public CliRequest(ByteBuffer buf) {
            super(buf);
        }

        void checkData() {
            int dataFromServer;
            try {
                dataFromServer = getBuffer().getInt();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
            if (dataFromServer!=data) {
                System.err.println("written: "+data+"; read:"+dataFromServer);
                return;
            }
        }
    }
}
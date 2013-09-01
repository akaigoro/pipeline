package com.github.rfqu.pipeline.util;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.rfqu.pipeline.core.Pipeline;
import com.github.rfqu.pipeline.nio.AsyncSocketChannel;

/** 
 * socket connection divided in inbound pipeline:
 *    socket=>stringArrived(String)
 * and outbound pipeline:
 *    write(String)=>socket
 */
public class Connection {
    public static final int BUF_SIZE = 128;

    protected AsyncSocketChannel channel;
    Pipeline inbound = new Pipeline();
    Pipeline outbound = new Pipeline();
    String2ByteBuf stringSource = new String2ByteBuf();
    StringSink stringSink = new StringSink();

    //------------- Sink
    
    public Connection(AsyncSocketChannel channel) throws IOException {
        this.channel=channel;
        
        inbound.setSource(channel.reader)
          .addTransformer(new ByteBuf2String())
          .setSink(stringSink);
        
        outbound.setSource(stringSource)
          .setSink(channel.writer);
    }

    public void start() {
    	channel.reader.injectBuffers(2, BUF_SIZE);
    	inbound.start();
    	outbound.start();
   }

    public void close() {
        inbound.close();
        outbound.close();
    }

    public void write(String message) {
        stringSource.post(message);
    }

    public LinkedBlockingQueue<String> getOutput() {
        return stringSink.getOutput();
    }

    public String read() throws InterruptedException {
        return stringSink.getOutput().take();
    }
    
}
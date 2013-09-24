package com.github.rfqu.pipeline.file.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;

import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.pipeline.core.Pipeline;
import com.github.rfqu.pipeline.core.SinkNode;
import com.github.rfqu.pipeline.core.SourceNode;
import com.github.rfqu.pipeline.file.AsyncFileReader;
import com.github.rfqu.pipeline.file.AsyncFileWriter;

public class AsyncFileChannelTest {
    static final int BUF_SIZE = 32;

    Path dataDir;

    ExecutorService currentExecutorService;

    @Before
    public void init() {
        DFContext.setSingleThreadExecutor();
        currentExecutorService = DFContext.getCurrentExecutorService();
        File dataDirFile=new File("testData");
        dataDirFile.mkdir();
        dataDir = dataDirFile.toPath();
    }

    /**
     * Writes and then reads a file
     */
    @Test
    public void smokeIOTest() throws Exception {
        Path file2write = dataDir.resolve("data1.dat");
        Set<StandardOpenOption> options = new HashSet();
        options.add(StandardOpenOption.CREATE);
        options.add(StandardOpenOption.READ);
        options.add(StandardOpenOption.WRITE);
        options.add(StandardOpenOption.DELETE_ON_CLOSE);
        
        AsynchronousFileChannel asfc = AsynchronousFileChannel.open(file2write, options, currentExecutorService);
        Pipeline writingPipeline = new Pipeline();
        AsyncFileWriter writer = new AsyncFileWriter(asfc);
        long bytes2write = 1;
        writingPipeline.setSource(new DataSource(bytes2write)).setSink(writer);
        writingPipeline.start();
        long bytes=(Long)writingPipeline.get();
        assertEquals(bytes2write, bytes);
        Pipeline readingPipeLine = new Pipeline();
        AsyncFileReader reader = new AsyncFileReader(asfc);
        reader.injectBuffers(2, BUF_SIZE);
        readingPipeLine.setSource(reader).setSink(new DataSink());
        readingPipeLine.start();
        bytes=(Long)readingPipeLine.get();
        assertEquals(bytes2write, bytes);
    }

    /**
     * send a message from server to client, then close connection
     */
    // @Test
    public void smokeIOTest1() throws Exception {
        // assertEquals(message, reply);
    }

    /**
     * send 2 messages in both directions simultaneousely
     */
    // @Test
    public void smokeIOTest2() throws Exception {
    }

    class DataGenerator {
        int data = 137;

        public byte next() {
            return (byte) (data *= 137);
        }
    }

    public class DataSource extends SourceNode<ByteBuffer> {
        /** here output messages return */
        protected StreamInput<ByteBuffer> myOutput = new StreamInput<ByteBuffer>();
        DataGenerator dataGenerator = new DataGenerator();
        long len;

        public DataSource(long bytes2write) {
            this.len = bytes2write;
            injectBuffers(2, BUF_SIZE);
        }

        @Override
        public Port<ByteBuffer> getReturnPort() {
            return myOutput;
        }

        public void injectBuffers(int count, int bufLen) {
            for (int k = 0; k < count; k++) {
                ByteBuffer buf = ByteBuffer.allocate(bufLen);
                myOutput.post(buf);
            }
        }

        @Override
        protected void act() {
            if (len == 0) {
                return;
            }
            ByteBuffer buffer = myOutput.get();
            buffer.clear();
            while (buffer.hasRemaining() && (len > 0)) {
                buffer.put(dataGenerator.next());
                len--;
            }
            buffer.flip();
            sinkPort.post(buffer);
            if (len == 0) {
                sinkPort.close();
            }
        }
    }

    class DataSink extends SinkNode<ByteBuffer> {
        DataGenerator dataGenerator = new DataGenerator();
        long bytesReceived=0;

        protected void act(ByteBuffer buffer) {
            bytesReceived+=buffer.remaining();
            while (buffer.hasRemaining()) {
                int expected = dataGenerator.next();
                int actual = buffer.get();
                assertEquals(expected, actual);
            }
            free(buffer);
        }

        @Override
        protected void complete() throws Exception {
            context.post(bytesReceived);
        }
    }

    public static void main(String[] args) throws Exception {
        AsyncFileChannelTest ct = new AsyncFileChannelTest();
        ct.init();
        ct.smokeIOTest();
    }
}
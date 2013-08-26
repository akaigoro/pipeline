package com.github.rfqu.pipeline.core;

import static org.junit.Assert.*;

import java.nio.CharBuffer;
import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.core.ListenableFuture;
import com.github.rfqu.df4j.ext.ImmediateExecutor;
import com.github.rfqu.pipeline.util.CharBufSink;
import com.github.rfqu.pipeline.util.CharBufSource;

public class PipelineTest {
	final static String string1 = "1";
	final static String string2 = "2";
	final static String string3 = string1+string2;
	
    @BeforeClass
    public static void initClass() {
        DFContext.setCurrentExecutor(new ImmediateExecutor());
    }
    
    void check1(Pipeline pipeLine) throws InterruptedException, ExecutionException {
        CharBufSource source = (CharBufSource) pipeLine.getSource();
        ListenableFuture<Object> future = pipeLine.getFuture();
        source.post("J");
        assertFalse(future.isDone());
        source.close();
        assertTrue(future.isDone());
        assertEquals("J", future.get());
    }

    void check2(Pipeline pipeLine) throws InterruptedException, ExecutionException {
        CharBufSource source = (CharBufSource) pipeLine.getSource();
        ListenableFuture<Object> future = pipeLine.getFuture();
        source.post(string1);
        source.post(string2);
        assertFalse(future.isDone());
        source.close();
        assertTrue(future.isDone());
        assertEquals(string3, future.get());
    }

    public void test(PipelineGenerator pg) throws InterruptedException, ExecutionException {
        check1(pg.make());
        check2(pg.make());
    }

    // @Test
    public void testAll() throws InterruptedException, ExecutionException {
        test0();
        test1();
        test2();
    }

    @Test
    public void test0() throws InterruptedException, ExecutionException {
        PipelineGenerator generator = new PipelineGenerator0();
        test(generator);
    }

    @Test
    public void test1() throws InterruptedException, ExecutionException {
        PipelineGenerator generator = new PipelineGenerator1();
        test(generator);
    }

    @Test
    public void test2() throws InterruptedException, ExecutionException {
        PipelineGenerator generator = new PipelineGenerator2();
        test(generator);
    }

    @Test
    public void testPostFailure() throws InterruptedException {
        Pipeline pipeline = new Pipeline();
        CharBufSource source = new CharBufSource();
        CharBufSink sink = new CharBufSink();
        ListenableFuture<Object> future = pipeline.getFuture();

        MyCopyTransformer tf = new MyCopyTransformer();
        pipeline.setSource(source).addTransformer(tf).setSink(sink);
        assertFalse(future.isDone());

        Throwable exc = new Throwable();
        tf.postFailure(exc);
        assertTrue(future.isDone());
        try {
            future.get();
            fail("exception expected");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            assertEquals(exc, cause);
        }
    }

    abstract class PipelineGenerator {
        abstract Pipeline make() throws InterruptedException, ExecutionException;
    }

    class PipelineGenerator0 extends PipelineGenerator {
        Pipeline make() throws InterruptedException, ExecutionException {
            Pipeline pipeline = new Pipeline();
            CharBufSource source = new CharBufSource();
            CharBufSink sink = new CharBufSink();
            pipeline.setSource(source).setSink(sink);
            pipeline.start();
            return pipeline;
        }
    }

    class PipelineGenerator1 extends PipelineGenerator {
        Pipeline make() throws InterruptedException, ExecutionException {
            Pipeline pipeline = new Pipeline();
            CharBufSource source = new CharBufSource();
            CharBufSink sink = new CharBufSink();

            MyCopyTransformer tf = new MyCopyTransformer();
            pipeline.setSource(source).addTransformer(tf).setSink(sink);
            pipeline.start();
            return pipeline;
        }
    }

    class PipelineGenerator2 extends PipelineGenerator {
        Pipeline make() throws InterruptedException, ExecutionException {
            Pipeline pipeline = new Pipeline();
            CharBufSource source = new CharBufSource();
            CharBufSink sink = new CharBufSink();

            MyCopyTransformer tf = new MyCopyTransformer();
            MyCopyTransformer tf2 = new MyCopyTransformer();
            pipeline.setSource(source)
            .addTransformer(tf)
            .addTransformer(tf2)
            .setSink(sink);
            pipeline.start();
            return pipeline;
        }
    }

    class MyCopyTransformer extends CopyTransformer<CharBuffer> {
        void postFailure(Throwable exc) {
            context.postFailure(exc);
        }
    }
}

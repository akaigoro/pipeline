package com.github.rfqu.pipeline.core;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.core.ListenableFuture;
import com.github.rfqu.df4j.ext.ImmediateExecutor;
import com.github.rfqu.pipeline.util.CharBufSink;
import com.github.rfqu.pipeline.util.CharBufSource;

public class CharBufCopyTransformerTest {
	final static String string1 = "1";
	final static String string2 = "2";
	final static String string3 = string1+string2;
	
	@BeforeClass
	public static void init() {
		DFContext.setCurrentExecutor(new ImmediateExecutor());
	}
	
    void check1(PipeLine pipeLine) throws InterruptedException, ExecutionException {
        CharBufSource source = (CharBufSource) pipeLine.getSource();
        ListenableFuture<Object> future = pipeLine.getFuture();
        source.post("J");
        source.close();
        assertTrue(future.isDone());
        Object res = future.get();
        assertEquals("J", res);
    }

    void check(PipeLine pipeLine) throws InterruptedException, ExecutionException {
        CharBufSource source = (CharBufSource) pipeLine.getSource();
        ListenableFuture<Object> future = pipeLine.getFuture();
        source.post(string1);
        source.post(string2);
        assertFalse(future.isDone());
        source.close();
        assertTrue(future.isDone());
        assertEquals(string3, future.get());
    }

    void checkExc(PipeLine pipeLine) throws InterruptedException {
        CharBufSource source = (CharBufSource) pipeLine.getSource();
        ListenableFuture<Object> future = pipeLine.getFuture();
        source.post("J");
        assertFalse(future.isDone());
        Throwable exc = new Throwable();
        source.postFailure(exc);
        assertTrue(future.isDone());
        try {
            future.get();
            fail("exception expected");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            assertEquals(exc, cause);
        }
    }


    public void test(PipelineGenerator pg) throws InterruptedException, ExecutionException {
        check1(pg.make());
        check(pg.make());
        checkExc(pg.make());
    }

    // @Test
    public void testAll() throws InterruptedException, ExecutionException {
        test1();
        test2();
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

    abstract class PipelineGenerator {
        abstract PipeLine make() throws InterruptedException, ExecutionException;
    }

    class PipelineGenerator1 extends PipelineGenerator {
        PipeLine make() throws InterruptedException, ExecutionException {
            CharBufSource source = new CharBufSource();
            CharBufCopyTransformer tf = new CharBufCopyTransformer(4);
            CharBufSink sink = new CharBufSink();
            PipeLine pipeline = new PipeLine();
            pipeline.setSource(source).addTransformer(tf).setSink(sink).start();
            return pipeline;
        }
    }

    class PipelineGenerator2 extends PipelineGenerator {
        PipeLine make() throws InterruptedException, ExecutionException {
            CharBufSource source = new CharBufSource();
            CharBufCopyTransformer tf = new CharBufCopyTransformer(4);
            CharBufCopyTransformer tf2 = new CharBufCopyTransformer(4);
            CharBufSink sink = new CharBufSink();
            PipeLine pipeline = new PipeLine();
            pipeline.setSource(source).addTransformer(tf).addTransformer(tf2).setSink(sink).start();
            return pipeline;
        }
    }
}

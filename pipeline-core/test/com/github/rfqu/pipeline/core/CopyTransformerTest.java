package com.github.rfqu.pipeline.core;

import static org.junit.Assert.*;

import java.nio.CharBuffer;
import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.ext.ImmediateExecutor;
import com.github.rfqu.pipeline.util.CharBufSink;
import com.github.rfqu.pipeline.util.CharBufSource;

public class CopyTransformerTest {
	final static String string1 = "1";
	final static String string2 = "2";
	final static String string3 = string1+string2;
	
	@BeforeClass
	public static void init() {
		DFContext.setCurrentExecutor(new ImmediateExecutor());
	}
	
	CharBufSource source;
	CharBufSink sink;
	
    void check1() throws InterruptedException, ExecutionException {
      source.post("J");
      assertFalse(sink.isClosed());
      source.close();
      assertTrue(sink.isClosed());
      assertEquals("J", sink.get());
  }
  
    void check() throws InterruptedException, ExecutionException {
      source.post(string1);
      source.post(string2);
      assertFalse(sink.isClosed());
      source.close();
      assertTrue(sink.isClosed());
      assertEquals(string3, sink.get());
  }
  
    void checkExc() throws InterruptedException {
        source.post("J");
        assertFalse(sink.isClosed());
        Throwable exc=new Throwable();
        source.postFailure(exc);
        assertTrue(sink.isClosed());
        try {
            sink.get();
            fail("exception expected");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            assertEquals(exc, cause);
        }
    }
    
    void checkExc2() throws InterruptedException, ExecutionException {
        source.post("J");
        assertFalse(sink.isClosed());
        source.close();
        assertTrue(sink.isClosed());
        assertEquals("J", sink.get());
        Throwable exc=new Throwable();
        source.postFailure(exc);
        try {
            sink.get();
            fail("exception expected");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof IllegalStateException);
            cause = cause.getCause();
            assertEquals(exc, cause);
        }
    }
    
    void checkExc3() throws InterruptedException, ExecutionException {
        source.post("J");
        assertFalse(sink.isClosed());
        source.close();
        assertTrue(sink.isClosed());
        assertEquals("J", sink.get());
        try {
            source.post("EE");
            fail("exception expected");
        } catch (IllegalStateException e) {
        }
    }
    
    @Test
    public void checkAll() throws InterruptedException, ExecutionException {
    	PipelineGenerator[] generators=new PipelineGenerator[]{
        		new PipelineGenerator(),
        		new PipelineGenerator1(),
        		new PipelineGenerator2(),
    	};
    	
    	for (PipelineGenerator pg:generators) {
    		pg.make(); check1();
    		pg.make(); check();
    		pg.make(); checkExc();
    		pg.make(); checkExc2();
    		pg.make(); checkExc3();
    	}
    }

    class PipelineGenerator {
    	void make() throws InterruptedException, ExecutionException {
            source=new CharBufSource();
            sink=new CharBufSink();
            sink.setSource(source);
    	}
    }
    
    class PipelineGenerator1 extends PipelineGenerator{
    	void make() throws InterruptedException, ExecutionException {
            source=new CharBufSource();
            MyCopyTransformer tf=new MyCopyTransformer();
            tf.setSource(source);
            sink=new CharBufSink();
            sink.setSource(tf);
    	}
    }
    
    class PipelineGenerator2 extends PipelineGenerator {
    	void make() throws InterruptedException, ExecutionException {
            source=new CharBufSource();
            MyCopyTransformer tf=new MyCopyTransformer();
            tf.setSource(source);
            MyCopyTransformer tf2=new MyCopyTransformer();
            tf2.setSource(tf);
            sink=new CharBufSink();
            sink.setSource(tf2);
    	}
    }
    
    class MyCopyTransformer extends CopyTransformer<CharBuffer> {
    }
}

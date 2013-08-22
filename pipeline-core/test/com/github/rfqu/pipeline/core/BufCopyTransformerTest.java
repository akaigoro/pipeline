package com.github.rfqu.pipeline.core;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.ext.ImmediateExecutor;
import com.github.rfqu.pipeline.util.CharBufSink;
import com.github.rfqu.pipeline.util.CharBufSource;

public class BufCopyTransformerTest {
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
	public void tNullTransform1() throws InterruptedException, ExecutionException {
        sink=new CharBufSink();
        CopyTransformer tf=new CopyTransformer(4);
        tf.setListener(sink);
        source=new CharBufSource(tf);
		check();
	}
	
	@Test
	public void tNullTransform1L() throws InterruptedException, ExecutionException {
        sink=new CharBufSink();
        CopyTransformer tf=new CopyTransformer(sink, 4000);
        source=new CharBufSource(tf);
		check();
	}
    
    @Test
    public void tNullTransform2() throws InterruptedException, ExecutionException {
        sink=new CharBufSink();
        CopyTransformer tf1=new CopyTransformer(sink, 4);
        CopyTransformer tf2=new CopyTransformer(tf1, 14);
        source=new CharBufSource(tf2);
        check();
    }
    
    @Test
    public void testPassExc() throws InterruptedException {
        sink=new CharBufSink();
        CopyTransformer tf1=new CopyTransformer(sink, 4);
        CopyTransformer tf2=new CopyTransformer(tf1, 14);
        source=new CharBufSource(tf2);
        checkExc();
    }
    
    @Test
    public void testPassExc2() throws InterruptedException, ExecutionException {
        sink=new CharBufSink();
        CopyTransformer tf1=new CopyTransformer(sink, 4);
        CopyTransformer tf2=new CopyTransformer(tf1, 14);
        source=new CharBufSource(tf2);
        checkExc2();
    }
    
    @Test
    public void testPassExc3() throws InterruptedException, ExecutionException {
        sink=new CharBufSink();
        CopyTransformer tf1=new CopyTransformer(sink, 4);
        CopyTransformer tf2=new CopyTransformer(tf1, 14);
        source=new CharBufSource(tf2);
        checkExc3();
    }
}

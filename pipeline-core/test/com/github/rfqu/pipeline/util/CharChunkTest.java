package com.github.rfqu.pipeline.util;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.ext.ImmediateExecutor;

public class CharChunkTest {
	final static String string1 = "1st str";
	final static String string2 = "2nd str";
	final static String string3 = string1+string2;
	
	@BeforeClass
	public static void init() {
		DFContext.setCurrentExecutor(new ImmediateExecutor());
	}
	
	StringChunkSource source;
	CharChunkSink sink;
	
	private void check() throws InterruptedException, ExecutionException {
		source.post(string1);
		source.post(string2);
		assertFalse(sink.isClosed());
		source.close();
		assertTrue(sink.isClosed());
		assertEquals(string3, sink.get());
	}
	
    @Test
    public void tChar2Char() throws InterruptedException, ExecutionException {
        sink=new CharChunkSink();
        source=new StringChunkSource(sink);
        check();
    }
}

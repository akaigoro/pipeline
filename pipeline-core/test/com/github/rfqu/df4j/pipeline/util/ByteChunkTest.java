package com.github.rfqu.df4j.pipeline.util;

import static org.junit.Assert.*;
import static com.github.rfqu.df4j.testutil.Utils.*;

import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.ext.ImmediateExecutor;

public class ByteChunkTest {
	final static byte[] array1 = {1,-1,0,7};
	final static byte[] array2 = {100,-100,127,-128};
	final static byte[] array3 = {1,-1,0,7,100,-100,127,-128};
	
	@BeforeClass
	public static void init() {
		DFContext.setCurrentExecutor(new ImmediateExecutor());
	}
	
	ByteChunkSource source;
	ByteChunkSink sink;
	
	private void check() throws InterruptedException, ExecutionException {
		source.post(array1);
		source.post(array2);
		assertFalse(sink.isClosed());
		source.close();
		assertTrue(sink.isClosed());
		assertTrue(byteArraysEqual(array3, sink.get()));
	}
	
    @Test
    public void tByte2Byte() throws InterruptedException, ExecutionException {
        sink=new ByteChunkSink();
        source=new ByteChunkSource(sink);
        check();
    }

}

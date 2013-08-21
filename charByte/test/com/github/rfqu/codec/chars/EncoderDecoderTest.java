/*
 * Copyright 2011 by Alexei Kaigorodov
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.github.rfqu.codec.chars;

import static com.github.rfqu.df4j.testutil.Utils.byteArraysEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.ext.ImmediateExecutor;
import com.github.rfqu.df4j.pipeline.util.ByteChunkSink;
import com.github.rfqu.df4j.pipeline.util.ByteChunkSource;
import com.github.rfqu.df4j.pipeline.util.CharChunkSink;
import com.github.rfqu.df4j.pipeline.util.StringChunkSource;

public class EncoderDecoderTest {
	@BeforeClass
	public static void init() {
		DFContext.setCurrentExecutor(new ImmediateExecutor());
	}
    
    @Test
    public void testACII1() throws IOException, InterruptedException, ExecutionException {
        test("a");
    }
    
    @Test
    public void testACII2() throws IOException, InterruptedException, ExecutionException {
        test("ascii");
    }
    
    @Test
    public void testCyrillic1() throws IOException, InterruptedException, ExecutionException {
        test("Я");
    }
    
    @Test
    public void testCyrillic() throws IOException, InterruptedException, ExecutionException {
        test("Овсянка, sir");
    }

    void test(String s) throws IOException, InterruptedException, ExecutionException {
        testE(s);
        testD(s);
        testDE(s);
    }
    
    void testE(String s) throws IOException, InterruptedException, ExecutionException {
        ByteChunkSink sink = new ByteChunkSink();
        Charset charset=Charset.forName("UTF8");
        Encoder encoder=new Encoder(charset, sink, 4);
        StringChunkSource source = new StringChunkSource(encoder);
        
        source.post(s);
        assertFalse(sink.isClosed());
        source.close();
        assertTrue(sink.isClosed());
        byte[] expectedBytes = s.getBytes("UTF8");
        byte[] res = sink.get();
        assertTrue(byteArraysEqual(expectedBytes, res));
        assertEquals(s, new String(res, "UTF8"));
    }

    void testD(String s) throws IOException, InterruptedException, ExecutionException {
        Charset charset=Charset.forName("UTF8");
        CharChunkSink sink = new CharChunkSink();
        Decoder decoder=new Decoder(charset, sink, 4);
        ByteChunkSource source = new ByteChunkSource(decoder);
        
        byte[] sBytes = s.getBytes("UTF8");
        source.post(sBytes);
        assertFalse(sink.isClosed());
        source.close();
        assertTrue(sink.isClosed());
        String res = sink.get();
        assertEquals(s, res);
    }
    
    void testDE(String s) throws IOException, InterruptedException, ExecutionException {
        Charset charset=Charset.forName("UTF8");
        CharChunkSink sink = new CharChunkSink();
        Decoder decoder=new Decoder(charset, sink, 4);
		Encoder encoder=new Encoder(charset, decoder, 5);
        StringChunkSource source = new StringChunkSource(encoder);
        
        source.post(s);
        assertFalse(sink.isClosed());
        source.close();
        assertTrue(sink.isClosed());
        String res = sink.get();
        assertEquals(s, res);
    }
}
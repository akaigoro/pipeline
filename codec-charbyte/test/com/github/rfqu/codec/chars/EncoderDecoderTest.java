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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.core.ListenableFuture;
import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.df4j.ext.ImmediateExecutor;
import com.github.rfqu.pipeline.core.Pipeline;
import com.github.rfqu.pipeline.core.Source;
import com.github.rfqu.pipeline.util.ByteBufSink;
import com.github.rfqu.pipeline.util.ByteBufSource;
import com.github.rfqu.pipeline.util.CharBufSink;
import com.github.rfqu.pipeline.util.CharBufSource;

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
        testEncoder(s);
        testDecoder(s);
        testDE(s);
    }
    
    void testEncoder(String s) throws IOException, InterruptedException, ExecutionException {
        Charset charset=Charset.forName("UTF8");
        CharBufSource source = new CharBufSource();
        Encoder encoder=new Encoder(charset);
        encoder.injectBuffers(2, 4);
        ByteBufSink sink = new ByteBufSink();
        
        Pipeline pipeline = new Pipeline();
        pipeline.setSource(source).addTransformer(encoder).setSink(sink).start();

        source.post(s);
        assertFalse(sink.isClosed());
        source.close();
        assertTrue(sink.isClosed());
        byte[] expectedBytes = s.getBytes("UTF8");
        byte[] res = sink.get();
        assertTrue(byteArraysEqual(expectedBytes, res));
        assertEquals(s, new String(res, "UTF8"));
    }

    void testDecoder(String s) throws IOException, InterruptedException, ExecutionException {
        Charset charset=Charset.forName("UTF8");
        ByteBufSource source = new ByteBufSource();
        Decoder decoder=new Decoder(charset);
        decoder.injectBuffers(2, 4);
        CharBufSink sink = new CharBufSink();
        
        Pipeline pipeline = new Pipeline();
        pipeline.setSource(source).addTransformer(decoder).setSink(sink).start();
        ListenableFuture<Object> future = pipeline.getFuture();
        
        byte[] sBytes = s.getBytes("UTF8");
        source.post(sBytes);
        assertFalse(sink.isClosed());
        source.close();
        assertTrue(sink.isClosed());
        String res = (String) future.get();
        assertEquals(s, res);
    }
    
    void testDE(String s) throws IOException, InterruptedException, ExecutionException {
        Charset charset=Charset.forName("UTF8");
        CharBufSource source = new CharBufSource();
        Encoder encoder=new Encoder(charset);
        encoder.injectBuffers(2, 4);
        Decoder decoder=new Decoder(charset);
        decoder.injectBuffers(2, 4);
        CharBufSink sink = new CharBufSink();

        Pipeline pipeline = new Pipeline();
        pipeline.setSource(source)
        .addTransformer(encoder)
        .addTransformer(decoder)
        .setSink(sink).start();
        ListenableFuture<Object> future = pipeline.getFuture();

        source.post(s);
        assertFalse(sink.isClosed());
        source.close();
        assertTrue(sink.isClosed());
        String res = (String) future.get();
        assertEquals(s, res);
    }
    
    void injectBuffers(Source<ByteBuffer> source, int bufCount, int buffLen) {
        StreamPort<ByteBuffer> port = source.getReturnPort();
    }


}
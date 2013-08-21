package com.github.rfqu.codec.javon.pushparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.github.rfqu.codec.javon.builder.impl.JavonPrinter;
import com.github.rfqu.codec.javon.pushparser.JavonParser;
import com.github.rfqu.df4j.core.CompletableFuture;
import com.github.rfqu.pipeline.util.StringChunkSource;

public class so1688099test extends com.github.rfqu.codec.javon.parser.so1688099test {
	@Test
    public void testWithPrinter() throws IOException, Exception {
        JavonPrinter pr = new JavonPrinter();
        JavonParser tp=new JavonParser(pr);
        StringChunkSource source = new StringChunkSource(tp);
        CompletableFuture<Object> res = tp.getResult();
        source.post(inp);
        source.close();
        assertTrue(res.isDone());
        String resS = res.get().toString();
		compareStrings(inp, resS);
        assertEquals(inp, resS);
	}

	@Test
    public void testWithBuilder() throws IOException, Exception {
        JavonPrinter pr = new JavonPrinter();
        JavonParser tp=new JavonParser(pr);
        StringChunkSource source = new StringChunkSource(tp);
        CompletableFuture<Object> res = tp.getResult();
        source.post(inp);
        source.close();
        assertTrue(res.isDone());
        String resS = res.get().toString();
		compareStrings(inp, resS);
        assertEquals(inp, resS);
	}
}

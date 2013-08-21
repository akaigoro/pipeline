package com.github.rfqu.codec.javon.pushparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.github.rfqu.codec.javon.builder.impl.JavonPrinter;
import com.github.rfqu.codec.javon.pushparser.JavonParser;
import com.github.rfqu.df4j.core.CompletableFuture;

public class so1688099test extends com.github.rfqu.codec.javon.parser.so1688099test {
	@Test
    public void testWithPrinter() throws IOException, Exception {
        JavonPrinter pr = new JavonPrinter();
        JavonParser mp=new JavonParser(pr);
        CompletableFuture<Object> res = mp.getResult();
        mp.postLine(inp);
        mp.postLine(null); // EOF needed to determine end of text
        assertTrue(res.isDone());
        String resS = res.get().toString();
		compareStrings(inp, resS);
        assertEquals(inp, resS);
	}

	@Test
    public void testWithBuilder() throws IOException, Exception {
        JavonPrinter pr = new JavonPrinter();
        JavonParser mp=new JavonParser(pr);
        CompletableFuture<Object> res = mp.getResult();
        mp.postLine(inp);
        mp.postLine(null); // EOF needed to determine end of text
        assertTrue(res.isDone());
        String resS = res.get().toString();
		compareStrings(inp, resS);
        assertEquals(inp, resS);
	}
}

package com.github.rfqu.codec.json.pushparser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.rfqu.codec.json.builder.impl.JsonPrinter;
import com.github.rfqu.codec.json.parser.ParseException;
import com.github.rfqu.codec.json.pushparser.JsonParser;
import com.github.rfqu.df4j.core.CompletableFuture;
import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.ext.ImmediateExecutor;
import com.github.rfqu.df4j.pipeline.util.StringChunkSource;

//import static com.github.rfqu.codec.json.asyncparser.Scanner.*;

public class JsonParserTest {
    @BeforeClass
    public static void init() {
        DFContext.setCurrentExecutor(new ImmediateExecutor());
    }

    @Test
    public void testN() throws Exception {
 //       checkN("[] {}"); Not sure it must be an error

        checkN("a");
        checkN("null");
        checkN("1");
        checkN("[1,2,3");
    }
    
    @Test
    public void testList() throws Exception {
        check("[]");
        check("[null]");
        check("[1]");
        check("[\"A\"]");
        check("[1 2 3.0, ]", "[1,2,3.0]");
    }

    @Test
    public void testMap() throws Exception {
        check("{}");
        check("{\"a\":1}");
        check("{\"a\":null}");
        check("{a:true, \"b\":1 \"c\":2.0 \"%%%\":2.0 \"...\":\"2.0\", }",
                "{\"a\":true,\"b\":1,\"c\":2.0,\"%%%\":2.0,\"...\":\"2.0\"}");
    }

    protected void check(String inp, String exp) throws IOException, Exception {
        JsonPrinter pr = new JsonPrinter();
        JsonParser tp = new JsonParser(pr);
        StringChunkSource source = new StringChunkSource(tp);
        CompletableFuture<Object> res = tp.getResult();
        source.post(inp);
        source.close();
        assertTrue(res.isDone());
        String resS = res.get().toString();
        assertEquals(exp, resS);
    }

    protected void check(String inp) throws IOException, Exception {
        check(inp, inp);
    }

    protected void checkN(String inp) throws IOException, Exception {
        JsonPrinter pr = new JsonPrinter();
        JsonParser tp = new JsonParser(pr);
        StringChunkSource source = new StringChunkSource(tp);
        CompletableFuture<Object> res = tp.getResult();
        source.post(inp);
        source.close();
        assertTrue(res.isDone());
        try {
			res.get();
			fail("ExecutionException expected");
		} catch (ExecutionException e) {
			ParseException pe=(ParseException) e.getCause();
			System.out.println(pe.getMessage());
		}
    }
}

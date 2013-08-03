package com.github.rfqu.codec.json.asyncparser;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.github.rfqu.df4j.core.CompletableFuture;
import com.github.rfqu.javon.builder.impl.JsonPrinter;

//import static com.github.rfqu.codec.json.asyncparser.Scanner.*;

public class AsyncParserTest {

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
        JsonParser tp = new JsonParser();
        JsonPrinter pr = new JsonPrinter();
        CompletableFuture<Object> res = tp.parseWith(pr);
        tp.postLine(inp);
        assertTrue(res.isDone());
        String resS = res.get().toString();
        assertEquals(exp, resS);
    }

    protected void check(String inp) throws IOException, Exception {
        check(inp, inp);
    }
}

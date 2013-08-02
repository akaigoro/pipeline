package com.github.rfqu.javon.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.github.rfqu.javon.builder.impl.JsonPrinter;

public class JsonParserTest {

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
        check("{\"a\":null}");
        check("{a:true, \"b\":1 \"c\":2.0 \"%%%\":2.0 \"...\":\"2.0\", }",
                "{\"a\":true,\"b\":1,\"c\":2.0,\"%%%\":2.0,\"...\":\"2.0\"}");
    }

    protected void check(String inp, String exp) throws IOException, Exception {
        JsonParser mp=new JsonParser(inp);
        JsonPrinter pr = new JsonPrinter();
        String res = mp.parseWith(pr).toString();
        assertEquals(exp, res);
    }

    protected void check(String inp) throws IOException, Exception {
        check(inp, inp);
    }
}
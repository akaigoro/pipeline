package com.github.rfqu.json.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.github.rfqu.json.builder.impl.JsonPrinter;

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
        JsonPrinter pr = new JsonPrinter();
        JsonParser mp=new JsonParser(pr);
        String res = mp.parseFrom(inp).toString();
        assertEquals(exp, res);
    }

    protected void check(String inp) throws IOException, Exception {
        check(inp, inp);
    }
}
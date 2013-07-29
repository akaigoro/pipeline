package com.github.rfqu.javon.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.github.rfqu.javon.parser.JavonParser;
import com.github.rfqu.javon.builder.JavonPrinter;

public class ParserTest {

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

    @Test
    public void testObj() throws Exception {
        check("A");
        check("A()","A");
        check("A(1)","A(1)");
        check("A(0,a:1)","A(0,a:1)");
        check("A(a:1)","A(a:1)");
        check("A(a:B(1))","A(a:B(1))");
    }

    @Test
    public void testObjList() throws Exception {
        check("A(a:B(1))[null,4.0,true]");
    }

    @Test
    public void testMapList() throws Exception {
        check("D{\"z\":null,\"w\":1.0,\"ace\":true}");
        check("D(){ z:null, w:1.0, ace:D()}"
             ,"D{\"z\":null,\"w\":1.0,\"ace\":D}");
    }

    protected void check(String inp, String exp) throws IOException, Exception {
        JavonParser mp=new JavonParser(inp);
        JavonPrinter pr = new JavonPrinter();
        String res = mp.parseWith(pr).toString();
        assertEquals(exp, res);
    }

    protected void check(String inp) throws IOException, Exception {
        check(inp, inp);
    }
}
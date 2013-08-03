package com.github.rfqu.codec.json.asyncparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.github.rfqu.df4j.core.CompletableFuture;
import com.github.rfqu.javon.builder.impl.JavonPrinter;

public class JavonParserTest {

    @Test
    public void testObj() throws Exception {
        /*
        check("A");
        check("A()","A");
        check("A(1)","A(1)");
        check("A(0,a:1)","A(0,a:1)");
        check("A(a:1)","A(a:1)");
        */
        check("A(a:B(1))");
    }

//    @Test
    public void testObjList() throws Exception {
        check("A(a:B(1))[null,4.0,true]");
    }

//    @Test
    public void testMapList() throws Exception {
        check("D{\"z\":null,\"w\":1.0,\"ace\":true}");
        check("D(){ z:null, w:1.0, ace:D()}"
             ,"D{\"z\":null,\"w\":1.0,\"ace\":D}");
    }

    protected void check(String inp, String exp) throws IOException, Exception {
        JavonParser mp=new JavonParser();
        JavonPrinter pr = new JavonPrinter();
        CompletableFuture<Object> res = mp.parseWith(pr);
        mp.postLine(inp);
        mp.postLine(null); // EOF needed to determine end of text
        assertTrue(res.isDone());
        String resS = res.get().toString();
        assertEquals(exp, resS);
    }

    protected void check(String inp) throws IOException, Exception {
        check(inp, inp);
    }
}
package com.github.rfqu.codec.javon.pushparser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.github.rfqu.codec.javon.builder.impl.JavonPrinter;
import com.github.rfqu.codec.javon.pushparser.JavonParser;
import com.github.rfqu.codec.json.parser.ParseException;
import com.github.rfqu.df4j.core.CompletableFuture;

public class JavonParserTest {

    @Test
    public void testObjN() throws Exception {
        checkN("A(a:1) A(a:1)");
        checkN("A(), []");
        checkN("A(1); [A(1)]");
        checkN("A(0,a:1) B{c:1}");
        checkN("A(a:B(1)) C[]");
    }

    @Test
    public void testObj() throws Exception {
        check("A");
        check("A()","A");
        check("A(1)");
        check("A(a,a,a:a,a:a)"); // ident both as positional arg and a key
        check("A(0,a:1)","A(0,a:1)");
        check("A(a:1)","A(a:1)");
        check("A(a:B(1))");
    }

    @Test
    public void testObjList() throws Exception {
        check("A[null,4.0,true]");
        check("A(a:B(1))[A{},4.0,true]");
    }

    @Test
    public void tesObjMap() throws Exception {
        check("A{}");
        check("A(){}","A{}");
        check("A(a:B(1)){\"b\":C(D(z:null))}");
        check("D{\"z\":null,\"w\":1.0,\"ace\":true}");
        check("D(){ z:null, w:1.0, ace:D()}"
                ,"D{\"z\":null,\"w\":1.0,\"ace\":D}");
    }

    @Test
    public void testObjMapList() throws Exception {
        check("D{\"z\":null,\"w\":1.0,\"ace\":true}[A{}]");
        check("D(){ z:null, w:1.0, ace:[D()]}[]"
             ,"D{\"z\":null,\"w\":1.0,\"ace\":[D]}[]");
    }

    protected void check(String inp, String exp) throws IOException, Exception {
        JavonPrinter pr = new JavonPrinter();
        JavonParser mp=new JavonParser(pr);
        CompletableFuture<Object> res = mp.getResult();
        mp.postLine(inp);
        mp.postLine(null); // EOF needed to determine end of text
        assertTrue(res.isDone());
        String resS = res.get().toString();
        assertEquals(exp, resS);
    }

    protected void checkN(String inp) throws IOException, Exception {
        JavonPrinter pr = new JavonPrinter();
        JavonParser mp=new JavonParser(pr);
        CompletableFuture<Object> res = mp.getResult();
        mp.postLine(inp);
        mp.postLine(null); // EOF needed to determine end of text
        assertTrue(res.isDone());
        try {
			res.get();
			fail("ExecutionException expected");
		} catch (ExecutionException e) {
			ParseException pe=(ParseException) e.getCause();
			System.out.println(pe.getMessage());
		}
    }

    protected void check(String inp) throws IOException, Exception {
        check(inp, inp);
    }
    
    public static void main(String[] args) throws Exception {
        new JavonParserTest().testObj();
    }
}
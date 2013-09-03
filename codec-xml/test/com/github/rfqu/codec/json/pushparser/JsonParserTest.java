package com.github.rfqu.codec.json.pushparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.rfqu.codec.json.builder.impl.JsonPrinter;
import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.ext.ImmediateExecutor;
import com.github.rfqu.pipeline.core.Pipeline;
import com.github.rfqu.pipeline.util.CharBufSource;

//import static com.github.rfqu.codec.json.asyncparser.Scanner.*;

public class JsonParserTest {
    @BeforeClass
    public static void initClass() {
        DFContext.setCurrentExecutor(new ImmediateExecutor());
    }
    
    public Pipeline init() throws Exception {
        JsonPrinter pr = new JsonPrinter();
        JsonParser tp=new JsonParser(pr);
        CharBufSource source = new CharBufSource();
        
        return new Pipeline()
        .setSource(source)
        .setSink(tp)
        .start();
    }

    @Test
    public void testN() throws Exception {
 //       checkN("[] {}"); Not sure it must be an error

        checkN("1");
        checkN("a");
        checkN("null");
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
    	Pipeline pp=init();
    	CharBufSource source=(CharBufSource)pp.getSource();
    	source.post(inp);
        source.close();
		assertTrue(pp.isDone());
        String resS = pp.get().toString();
        assertEquals(exp, resS);
    }

    protected void check(String inp) throws IOException, Exception {
        check(inp, inp);
    }

    protected void checkN(String inp) throws IOException, Exception {
    	Pipeline pp=init();
    	CharBufSource source=(CharBufSource)pp.getSource();
        
        source.post(inp);
        source.close();
        assertTrue(pp.isDone());
        try {
			pp.get();
			fail("ExecutionException expected");
		} catch (ExecutionException e) {
//			ParseException pe=(ParseException) e.getCause();
			Throwable pe=e.getCause();
			System.out.println(pe.getMessage());
		}
    }
}

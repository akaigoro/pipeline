package com.github.rfqu.codec.json.pushparser;

import static com.github.rfqu.codec.json.pushparser.Scanner.COLON;
import static com.github.rfqu.codec.json.pushparser.Scanner.COMMA;
import static com.github.rfqu.codec.json.pushparser.Scanner.IDENT;
import static com.github.rfqu.codec.json.pushparser.Scanner.LBRACE;
import static com.github.rfqu.codec.json.pushparser.Scanner.LBRACKET;
import static com.github.rfqu.codec.json.pushparser.Scanner.NUMBER;
import static com.github.rfqu.codec.json.pushparser.Scanner.RBRACE;
import static com.github.rfqu.codec.json.pushparser.Scanner.RBRACKET;
import static com.github.rfqu.codec.json.pushparser.Scanner.STRING;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.ext.ImmediateExecutor;
import com.github.rfqu.pipeline.core.Pipeline;
import com.github.rfqu.pipeline.util.CharBufSource;

public class ScannerTest {
    @BeforeClass
    public static void initClass() {
        DFContext.setCurrentExecutor(new ImmediateExecutor());
    }
    
    MyTokenPort tp;
    CharBufSource source;
    
    @Before
    public void init() throws Exception {
        tp = new MyTokenPort();
        source = new CharBufSource();
        new Pipeline()
        .setSource(source)
        .setSink(tp)
        .start();
    }

    @Test
    public void testList() throws Exception {
        check("[a]", LBRACKET, IDENT, RBRACKET);
        check("[1]", LBRACKET, NUMBER, RBRACKET);
        check("[null]", LBRACKET, IDENT, RBRACKET);
        check("[\"A\"]", "[A]", LBRACKET, STRING, RBRACKET);
        check("[1 2 3.0, ]", "[123.0,]", LBRACKET, NUMBER, NUMBER, NUMBER, COMMA, RBRACKET);
    }

 //   @Test
    public void testMap() throws Exception {
        check("{}", LBRACE, RBRACE);
        check("{\"a\":null}", "{a:null}", LBRACE, STRING,COLON,IDENT, RBRACE);
        check("{a:true, \"b\":1 \"c\":2.0 \"%%%\":2.0 \"...\":\"2.0\", }",
                "{a:true,b:1c:2.0%%%:2.0...:2.0,}",
                LBRACE, IDENT,COLON,IDENT, COMMA, STRING,COLON,NUMBER, 
                STRING,COLON,NUMBER, STRING,COLON,NUMBER, STRING,COLON,STRING, COMMA, RBRACE);
    }

    protected void check(String inp, String exp, Character... tokens) {
        source.post(inp);
        Character[] resT = tp.getTypes();
        assertEquals(tokens.length, resT.length);
        for (int k=0; k<tokens.length; k++) {
            assertEquals(tokens[k], resT[k]);
        }
        String resS = tp.getString();
        assertEquals(exp, resS);
    }

    protected void check(String inp, Character... tokens) throws IOException, Exception {
        check(inp, inp, tokens);
    }

    class MyTokenPort extends Scanner {
        StringBuilder sb=new StringBuilder();
        ArrayList<Character> types=new ArrayList<Character>(); 

        @Override
        public void postToken(char tokenType, String tokenString) {
            if (tokenType==NEWL) return;
            types.add(tokenType);
            if (tokenString==null) {
                sb.append((char)tokenType);
            } else {
                sb.append(tokenString);
            }
        }

        @Override
        public void setParseError(Throwable e) {
            throw new RuntimeException(e);
        }
        
        String getString() {
            String string2 = sb.toString();
            sb=new StringBuilder();
            return string2;
        }

        Character[] getTypes() {
            Character[] res = types.toArray(new Character[types.size()]);
            types=new ArrayList<Character>(); 
            return res;
        }
    }
}

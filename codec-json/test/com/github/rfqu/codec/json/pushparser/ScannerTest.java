package com.github.rfqu.codec.json.pushparser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.rfqu.codec.json.pushparser.Scanner;
import com.github.rfqu.codec.json.pushparser.TokenPort;
import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.ext.ImmediateExecutor;
import com.github.rfqu.pipeline.util.StringChunkSource;

import static com.github.rfqu.codec.json.pushparser.Scanner.*;

public class ScannerTest {
    @BeforeClass
    public static void init() {
        DFContext.setCurrentExecutor(new ImmediateExecutor());
    }

    @Test
    public void testList() throws Exception {
        check("[]", LBRACKET, RBRACKET);
        check("[null]", LBRACKET, IDENT, RBRACKET);
        check("[1]", LBRACKET, NUMBER, RBRACKET);
        check("[\"A\"]", "[A]", LBRACKET, STRING, RBRACKET);
        check("[1 2 3.0, ]", "[123.0,]", LBRACKET, NUMBER, NUMBER, NUMBER, COMMA, RBRACKET);
    }

    @Test
    public void testMap() throws Exception {
        check("{}", LBRACE, RBRACE);
        check("{\"a\":null}", "{a:null}", LBRACE, STRING,COLON,IDENT, RBRACE);
        check("{a:true, \"b\":1 \"c\":2.0 \"%%%\":2.0 \"...\":\"2.0\", }",
                "{a:true,b:1c:2.0%%%:2.0...:2.0,}",
                LBRACE, IDENT,COLON,IDENT, COMMA, STRING,COLON,NUMBER, 
                STRING,COLON,NUMBER, STRING,COLON,NUMBER, STRING,COLON,STRING, COMMA, RBRACE);
    }

    protected void check(String inp, String exp, Character... tokens) {
        MyTokenPort tp = new MyTokenPort();
        Scanner sc=new Scanner(tp);
        StringChunkSource source = new StringChunkSource(sc);
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

    class MyTokenPort extends TokenPort {
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
            return sb.toString();
        }

        Character[] getTypes() {
            return types.toArray(new Character[types.size()]);
        }
    }
}

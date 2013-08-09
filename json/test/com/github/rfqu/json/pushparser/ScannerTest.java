package com.github.rfqu.json.pushparser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import com.github.rfqu.json.pushparser.Scanner;
import com.github.rfqu.json.pushparser.TokenPort;

import static com.github.rfqu.json.pushparser.Scanner.*;

public class ScannerTest {

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

    protected void check(String inp, String exp, Integer... tokens) {
        Scanner sc=new Scanner();
        MyTokenPort tp = new MyTokenPort();
        sc.setTokenPort(tp);
        sc.postLine(inp);
        Integer[] resT = tp.getTypes();
        assertEquals(tokens.length, resT.length);
        for (int k=0; k<tokens.length; k++) {
            assertEquals(tokens[k], resT[k]);
        }
        String resS = tp.getString();
        assertEquals(exp, resS);
    }

    protected void check(String inp, Integer... tokens) throws IOException, Exception {
        check(inp, inp, tokens);
    }

    class MyTokenPort extends TokenPort {
        StringBuilder sb=new StringBuilder();
        ArrayList<Integer> types=new ArrayList<Integer>(); 

        @Override
        public void postToken(int tokenType, String tokenString) {
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

        Integer[] getTypes() {
            return types.toArray(new Integer[types.size()]);
        }
    }
}

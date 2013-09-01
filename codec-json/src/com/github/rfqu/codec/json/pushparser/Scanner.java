/*
 * Copyright 2013 by Alexei Kaigorodov
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.github.rfqu.codec.json.pushparser;

import java.nio.CharBuffer;

import com.github.rfqu.codec.json.parser.ParseException;
import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.pipeline.core.SinkNode;

public abstract class Scanner extends SinkNode<CharBuffer> {
    public static final char LPAREN='(', RPAREN=')', LBRACE='{', RBRACE='}'
            , LBRACKET='[', RBRACKET=']', COMMA=',', COLON=':'
            , SPACE=' ', TAB='\t', NEWL='\n', QUOTE='"', QUOTE2='\'', COMMENT='#'
            , EOF=0
            , NUMBER=EOF+1, IDENT=NUMBER+1, STRING=IDENT+1;
    
    private CharRingBuffer charBuffer=new CharRingBuffer();
    private boolean newLineSeen=false;
    
    private GeneralScanner generalScanner=new GeneralScanner();
    private StringScanner stringScanner=new StringScanner();
    private NumScanner numScanner=new NumScanner();
    private IdentScanner identScanner=new IdentScanner();
    private SubScanner subScanner=generalScanner;

    @Override
    protected void act(CharBuffer inbuf) {
        while (inbuf.hasRemaining()) {
            char c = inbuf.get();
            scan(c);
        }
    }

    @Override
    protected void complete() {
        scan(EOF);
    }

    protected void scan(char ch) {
        /*
        if (newLineSeen) {
            newLineSeen=false;
            charBuffer.startLine();
        }
        */
        switch (ch) {
            case NEWL:
                // if this chracter will cause error,
                // diagnostics should include current line
                // so postpone switching to another line for next character
                newLineSeen=true;
                subScanner.postChar(ch);
                break;
            default:
                charBuffer.putChar(ch);
                subScanner.postChar(ch);
        }
    }

    protected ParseException toParseException(Throwable e) {
        String header;
        if (e instanceof ParseException) {
            header="Syntax error";
        } else {
            header=e.getClass().getName();
        }
        String message = charBuffer.getTokenLine(header, e.getMessage());
        StackTraceElement[] stackTrace = e.getStackTrace();
        ParseException ee = new ParseException(message, e);
        ee.setStackTrace(stackTrace);
        return ee;
    }

    public abstract void postToken(char tokenType, String tokenString);
    
    public void postParseError(String message) {
        setParseError(new ParseException(message));
    }
    
    public abstract void setParseError(Throwable e);

    abstract class SubScanner {
        private StringBuilder sb=new StringBuilder();

        /**
         * posts token with string from string builder
         * @param tokenType
         */
        final void postToken(char tokenType) {
            String str=sb.toString();
            Scanner.this.postToken(tokenType, str);
            sb.delete(0, str.length());
            subScanner=generalScanner;
        }

        final void append(char ch) {
            sb.append(ch);
        }
       
        final void setMeAsScanner() {
            subScanner=this;
        }
        
        abstract void postChar(char ch);
   }
    
    private final class GeneralScanner extends SubScanner {

        /**
         * determines current lexical token
         */
        @Override
        void postChar(char cch) {
            switch (cch) {
            case LPAREN: case RPAREN:
            case LBRACE: case RBRACE: case LBRACKET:
            case RBRACKET: case COMMA: case COLON: case EOF:
                charBuffer.markTokenPosition();
                Scanner.this.postToken(cch, null);
                return;
            case SPACE: case TAB: case NEWL:
                return;
            case QUOTE:
            case QUOTE2:
                stringScanner.scanString(cch);
                return;
            default:
                if (Character.isDigit(cch)) {
                    numScanner.scanNumber(cch);
                    return;
                } else if (Character.isLetter(cch)) {
                    identScanner.scanIdent(cch);
                    return;
                } else {
                    charBuffer.markTokenPosition();
                    postParseError("unexpected character:'"+cch+"'");
                }
            } // end switch
        }
    }
    
    /** parses quoted literal
     * 
     * @param quoteSymbol
     */
    private final class StringScanner extends SubScanner {
        int quoteSymbol;
        
        void scanString(int quoteSymbol) {
            this.quoteSymbol=quoteSymbol;
            charBuffer.markTokenPosition();
            setMeAsScanner();
            
        }

        @Override
        void postChar(char cch) {
            if (cch==quoteSymbol) {
                postToken(STRING);
            } else if (cch==EOF) {
                postParseError("unexpected end of file");
            } else {
                append(cch);
            }
        }
        
    }
    
    /** parses unquoted identifier
     * 
     * @param quoteSymbol
     */
    private final class IdentScanner extends SubScanner {
        
        void scanIdent(char cch) {
            charBuffer.markTokenPosition();
            setMeAsScanner();
            append(cch);            
        }

        @Override
        void postChar(char cch) {
            if (Character.isDigit(cch) || Character.isLetter(cch)) {
                append(cch);
            } else {
                postToken(IDENT);
                subScanner.postChar(cch);
            }
        }
    }
    
    /** parses number
     * 
     * @param quoteSymbol
     */
    private final class NumScanner extends SubScanner {
        
        void scanNumber(char cch) {
            charBuffer.markTokenPosition();
            append(cch);
            setMeAsScanner();
        }

        @Override
        void postChar(char cch) {
            switch (cch) {
            case LPAREN: case RPAREN:
            case LBRACE: case RBRACE: case LBRACKET:
            case RBRACKET: case COMMA: case COLON:
            case SPACE: case TAB:   case NEWL:
            case COMMENT:
            case EOF:
                postToken(NUMBER);
                subScanner.postChar(cch);
                break;
            default:
                append(cch);
            }
        }   
    }

    protected static String token2Str(int t) {
        switch(t) {
        case SPACE: return "<SPACE>";
        case TAB: return "<TAB>";
        case NEWL: return "<\n>";
        case QUOTE: return "<\\\">";
        case QUOTE2: return "<'>";
        case COMMENT: return "#";
        case EOF: return "<EOF>";
        case IDENT: return "<IDENTIFIER>";
        case STRING: return "<STRING>";
        default:
           return String.valueOf((char)t);
        }
    }
}

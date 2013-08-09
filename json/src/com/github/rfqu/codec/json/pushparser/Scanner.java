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

import com.github.rfqu.codec.json.parser.ParseException;

public class Scanner {
    public static final int LPAREN='(', RPAREN=')', LBRACE='{', RBRACE='}'
            , LBRACKET='[', RBRACKET=']', COMMA=',', COLON=':'
            , SPACE=' ', TAB='\t', NEWL='\n', QUOTE='"', QUOTE2='\'', COMMENT='#'
            , EOF=Character.MAX_VALUE
            , NUMBER=EOF+1, IDENT=NUMBER+1, STRING=IDENT+1;
    
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

    private CharRingBuffer charBuffer=new CharRingBuffer();
    private boolean newLineSeen=false;
    
    private GeneralScanner generalScanner=new GeneralScanner();
    private StringScanner stringScanner=new StringScanner();
    private NumScanner numScanner=new NumScanner();
    private IdentScanner identScanner=new IdentScanner();
    private CharPort chp=generalScanner;
    private TokenPort tokenPort;
    
    private StringBuilder tokenStrinBuilder=new StringBuilder();
    
    void setScanner(CharPort p) {
        chp=p;
    }
    
    public void setTokenPort(TokenPort tp) {
        tokenPort=tp;
    }

    public void postCharSource(CharSource source) {
        int ch;
        for (;;) {
            if (newLineSeen) {
                newLineSeen=false;
                charBuffer.startLine();
            }
            switch (ch=source.nextChar()) {
                case -1:
                    return;
                case NEWL:
                    // if this chracter will cause error,
                    // diagnostics should include current line
                    // so postpone sitching to another line for next character
                    newLineSeen=true;
                    chp.postChar((char) ch);
                    break;
                default:
                    charBuffer.putChar((char) ch);
                    chp.postChar((char) ch);
            }
        }
    } 
    
    public void postLine(String str) {
        if (str==null) {
            tokenPort.postToken(EOF, null);
            return;
        }
        postCharSource(new StringSource(str));
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

    private void postToken(int tokenType) {
        String str=tokenStrinBuilder.toString();
        tokenPort.postToken(tokenType, str);
        tokenStrinBuilder.delete(0, str.length());
        setScanner(generalScanner);
    }
    
    /** parses quoted literal
     * 
     * @param quoteSymbol
     */
    private final class StringScanner implements CharPort {
        int quoteSymbol;
        
        void scanString(int quoteSymbol) {
            this.quoteSymbol=quoteSymbol;
            charBuffer.markTokenPosition();
            setScanner(this);
            
        }

        @Override
        public void postChar(char cch) {
            if (cch==quoteSymbol) {
                postToken(STRING);
            } else if (cch==EOF) {
                tokenPort.postParseError("unexpected end of file");
            } else {
                tokenStrinBuilder.append(cch);
            }
        }
        
    }
    
    /** parses unquoted identifier
     * 
     * @param quoteSymbol
     */
    private final class IdentScanner implements CharPort {
        
        void scanIdent(char cch) {
            charBuffer.markTokenPosition();
            setScanner(this);
            tokenStrinBuilder.append(cch);            
        }

        @Override
        public void postChar(char cch) {
            if (Character.isDigit(cch) || Character.isLetter(cch)) {
                tokenStrinBuilder.append(cch);
            } else {
                postToken(IDENT);
                chp.postChar(cch);
            }
        }
    }
    
    /** parses number
     * 
     * @param quoteSymbol
     */
    private final class NumScanner implements CharPort {
        
        void scanNumber(char cch) {
            charBuffer.markTokenPosition();
            tokenStrinBuilder.append(cch);
            setScanner(this);
        }

        @Override
        public void postChar(char cch) {
            switch (cch) {
            case EOF:
            case LPAREN: case RPAREN:
            case LBRACE: case RBRACE: case LBRACKET:
            case RBRACKET: case COMMA: case COLON:
            case SPACE: case TAB:   case NEWL:
            case COMMENT:
                postToken(NUMBER);
                chp.postChar(cch);
                break;
            default:
                tokenStrinBuilder.append(cch);
            }
        }   
    }
    
    private final class GeneralScanner implements CharPort {

        /**
         * determines current lexical token
         */
        @Override
        public void postChar(char cch) {
            switch (cch) {
            case LPAREN: case RPAREN:
            case LBRACE: case RBRACE: case LBRACKET:
            case RBRACKET: case COMMA: case COLON:
                charBuffer.markTokenPosition();
                tokenPort.postToken(cch, null);
                return;
            case SPACE: case TAB:   case NEWL:
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
                    tokenPort.postParseError("unexpected character:'"+cch+"'");
                }
            } // end switch
        }
    }
}

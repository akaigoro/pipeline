/*
 * Copyright 2013 by Alexei Kaigorodov
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.github.rfqu.javon.pushparser;

import com.github.rfqu.javon.parser.ParseException;

public class Scanner implements LinePort {
    protected static final int LPAREN='(', RPAREN=')', LBRACE='{', RBRACE='}'
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

    private String line;
    private int lineNumber=0;
    private int pos=0;
    
    private String tokenLine;
    private int tokenLineNumber;
    private int tokenPos;
    
    GeneralScanner generalScanner=new GeneralScanner();
    StringScanner stringScanner=new StringScanner();
    NumScanner numScanner=new NumScanner();
    IdentScanner identScanner=new IdentScanner();
    CharPort chp=generalScanner;
    TokenPort tokenPort;
    
    StringBuilder tokenStrinBuilder=new StringBuilder();
    
    void setScanner(CharPort p) {
        chp=p;
    }
    
    protected void setTokenPort(TokenPort tp) {
        tokenPort=tp;
    }

    @Override
    public void postLine(String str) {
        if (str==null) {
            tokenPort.postToken(EOF, null);
            return;
        }
        line = str;
        lineNumber++;
        int length = line.length();
        for (pos=0; pos<length; pos++) {
            char ch = line.charAt(pos);
            chp.postChar(ch);
        }
        chp.postChar((char) NEWL);
    } 
    
    protected ParseException toParseException(Throwable e) {
        StringBuilder sb=new StringBuilder("\n");
        if (e instanceof ParseException) {
            sb.append("Syntax error");
        } else {
            sb.append(e.getClass().getName());
        }
        sb.append(" at line ").append(tokenLineNumber).append(":\n")
          .append(tokenLine).append("\n");
        for (int k=0; k<tokenPos; k++) {
            sb.append(' ');
        }
        sb.append("^ ").append(e.getMessage());
        String message = sb.toString();
        StackTraceElement[] stackTrace = e.getStackTrace();
        ParseException ee = new ParseException(message, e);
        ee.setStackTrace(stackTrace);
        return ee;
    }

    void postToken(int tokenType) {
        String str=tokenStrinBuilder.toString();
        tokenPort.postToken(tokenType, str);
        tokenStrinBuilder.delete(0, str.length());
        setScanner(generalScanner);
    }
    
    private void markTokenPosition() {
        tokenLineNumber=lineNumber;
        tokenLine=line;
        tokenPos=pos;
    }

    /** parses quoted literal
     * 
     * @param quoteSymbol
     */
    private final class StringScanner implements CharPort {
        int quoteSymbol;
        
        void scanString(int quoteSymbol) {
            this.quoteSymbol=quoteSymbol;
            markTokenPosition();
            setScanner(this);
            
        }

        @Override
        public void postChar(char cch) {
            if (cch==quoteSymbol) {
                postToken(STRING);
            } else if (cch==EOF) {
                tokenPort.postParserError("unexpected end of file");
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
            markTokenPosition();
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
            markTokenPosition();
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
                markTokenPosition();
                tokenPort.postToken(cch, null);
                return;
            case SPACE: case TAB:   case NEWL:
                return;
            case COMMENT:
                pos=line.length()-1;
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
                    markTokenPosition();
                    tokenPort.postParserError("unexpected character:'"+cch+"'");
                }
            } // end switch
        }
    }
}

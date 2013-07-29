/*
 * Copyright 2013 by Alexei Kaigorodov
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.github.rfqu.javon.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import com.github.rfqu.javon.JavonBulderFactory;
import com.github.rfqu.javon.ListBuilder;
import com.github.rfqu.javon.MapBuilder;
import com.github.rfqu.javon.ObjectBuilder;

/** at least one of following methods should be overriden:
 * newRootObject(),
 * newRootList()
 * 
 * @author ak
 *
 */
public class JavonParser extends JavonScanner {//extends JsonStack {
    private JavonBulderFactory factory; 

    public JavonParser(Reader ir) throws IOException {
        super(new BufferedReader(ir));
    }
    
    public JavonParser(InputStream is) throws IOException {
        this(new InputStreamReader(is));
    }

    public JavonParser(File inputFile) throws IOException {
        this(new BufferedReader(new FileReader(inputFile)));
    }

    public JavonParser(String str) throws IOException {
        this(new StringReader(str));
    }

    public Object parseWith(JavonBulderFactory factory) throws Exception {
        this.factory=factory;
        scan();
        // skip empty lines
        skipSpaces();
        Object res;
        try {
            switch (tokenType) {
            case IDENT:
                res=parseObject();
                break;
            case LBRACKET:
                res=parseList();
                break;
            case LBRACE:
                res=parseMap();
                break;
            default:
                throw new ParseException("Identifier, { or [ expected");
            }
        } catch (Exception e) {
            throw toParseException(e);
        }
        // skip empty lines
        skipSpaces();
        if (tokenType!=EOF ) {
            throw new ParseException("extra text:"+tokenType);
        }
        return res;
    }

    protected Object parseObject() throws Exception {
        ObjectBuilder builder = factory.newObjectBuilder(tokenString);
        checkAndScan(IDENT);
        if (tokenType == LPAREN) {
            scan();
            ArrayList<Object> args = null;
            positionalArgs: // parse positional parameters
            for (;;) {
                switch (tokenType) {
                case RPAREN:
                    // do not eat
                    break positionalArgs;
                case IDENT: {
                    // lookahead needed to differentiate A and A:A
                    if (lookahead() == COLON) {
                        break positionalArgs;
                    }
                    break;
                }
                case COMMA:
                    scan();
                    continue;
                }
                Object value = parseValue();
                if (args==null) {
                    args = new ArrayList<Object>();
                }
                args.add(value);
            }
            if (args==null) {
                builder.instatntiate();
            } else {
                builder.instatntiate(args.toArray());
            }
            // parse named parameters
            namedArgs:
            for (;;) {
                switch (tokenType) {
                case RPAREN:
                    scan();
                    break namedArgs;
                case COMMA:
                    scan();
                    break;
                case IDENT: {
                    String key = tokenString;
                    scan();
                    checkAndScan(COLON);
                    Object value = parseValue();
                    builder.set(key, value);
                    break;
                }
                default:
                    throw new ParseException("comma  or ) expected");
                }
            }
        } else {
            builder.instatntiate();
        }
        if (tokenType == LBRACKET) {
            parseList(builder.asListBuilder());
        }
        if (tokenType == LBRACE) {
            parseMap(builder.asMapBuilder());
        }
        return builder.getValue();
    }

    protected Object parseMap() throws IOException, ParseException, Exception {
        MapBuilder builder = factory.newMapBuilder();
        parseMap(builder);
        return builder.getValue();
    }

    protected void parseMap(MapBuilder builder) throws IOException, ParseException, Exception {
        checkAndScan(LBRACE);
        for (;;) {
            switch (tokenType) {
            case COMMA:
                scan();
                break;
            case RBRACE:
                scan();
                return;
            case STRING: 
            case IDENT: {
                String key = tokenString;
                scan();
                checkAndScan(COLON);
                Object value=parseValue();
                builder.set(key, value);
                break;
            }
            default:
                throw new ParseException("comma  or } expected");
            }
        }
    }

    /**
     * allows spare commas
     */
    private void parseList(ListBuilder builder) throws Exception {
        checkAndScan(LBRACKET);
        for (;;) {
            switch (tokenType) {
            case RBRACKET:
                scan();
                return;
            case COMMA:
                scan();
                break;
            default:
                Object value=parseValue();
                builder.add(value);
            }
        }
    }

    protected Object parseList() throws Exception {
        ListBuilder builder = factory.newListBuilder();
        parseList(builder);
        return builder.getValue();
    }
    
    /** 
     * string, array, map, or object
     */
    private Object parseValue() throws Exception {
        switch (tokenType) {
        case LBRACKET:
            scan();
            return parseList();
        case LBRACE:
            scan();
            return parseMap();
        case STRING: {
            String str=tokenString;
            scan();
            return str;
        }
        case NUMBER: 
            return parseNumber();
        case IDENT:
            return parseIdent();
        default: 
            throw new ParseException("value expected, but "+token2Str(tokenType)+" seen");
        }
    }
    
    private Object parseNumber() throws ParseException, IOException {
        Object res;
        try {
            res=Integer.valueOf(tokenString);
        } catch (NumberFormatException e) {
            try {
                res=Double.valueOf(tokenString);
            } catch (NumberFormatException e1) {
                throw new ParseException("bad number:"+tokenString);
            }
        }
        scan();
        return res;
    }
    
    /** 
     * string, array, or object
     * @return
     * @throws IOException
     * @throws ParseException 
     */
    private Object parseIdent() throws Exception {
        Object res;
        if ("null".equals(tokenString)) {
            res=null;
            scan();
        } else if ("false".equals(tokenString)) {
            res=Boolean.FALSE;
            scan();
        } else if ("true".equals(tokenString)) {
            res=Boolean.TRUE;
            scan();
        } else {
            res=parseObject();
        }
        return res;
    }
}


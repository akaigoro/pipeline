/*
 * Copyright 2013 by Alexei Kaigorodov
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.github.rfqu.codec.json.asyncparser;

import java.util.ArrayList;

import com.github.rfqu.df4j.core.CompletableFuture;
import com.github.rfqu.javon.builder.ListBuilder;
import com.github.rfqu.javon.builder.MapBuilder;
import com.github.rfqu.javon.builder.ObjectBuilder;
import com.github.rfqu.javon.builder.JavonBulderFactory;
import com.github.rfqu.javon.parser.ParseException;

import static com.github.rfqu.codec.json.asyncparser.Scanner.*;

public class JavonParser extends JsonParser {
    JavonBulderFactory factory;

    public JavonParser() {
        new RootTokenPort();
    }

    public CompletableFuture<Object> parseWith(JavonBulderFactory factory) throws Exception {
        super.factory = factory;
        this.factory = factory;
        res = new CompletableFuture<Object>();
        setCurrentParser(new RootTokenPort());
        return res;
    }

    protected void setCurrentParser(Parser tp) {
        currentParser=tp;
        scanner.setTokenPort(tp);
    }

    public void postLine(String inp) {
        scanner.postLine(inp);
    }

    protected void parseObject(String tokenString) {
        try {
            ObjectBuilder builder = factory.newObjectBuilder(tokenString);
            new ObjectParser(currentParser, builder);
        } catch (Exception e) {
            currentParser.postParserError(e.getMessage());
        }
    }

    /**
     * null, boolean, or object
     */
    protected void parseIdent(String tokenString) {
        if ("null".equals(tokenString)) {
            currentParser.setValue(null);
        } else if ("false".equals(tokenString)) {
            currentParser.setValue(Boolean.FALSE);
        } else if ("true".equals(tokenString)) {
            currentParser.setValue(Boolean.TRUE);
        } else {
            parseObject(tokenString);
        }
    }

    class RootTokenPort extends JsonParser.RootTokenPort {

        @Override
        public void postToken(int tokenType, String tokenString) {
            switch (tokenType) {
            case IDENT:
                parseObject(tokenString);
                break;
            case LBRACKET:
                parseList();
                break;
            case LBRACE:
                parseMap();
                break;
            default:
                postParserError("Identifier, { or [ expected");
            }
        }
    }
    
    class ObjectParser extends Parser {
        ObjectBuilder builder;
        ArrayList<Object> args = null;
        String key;
        int state = 0;

        public ObjectParser(Parser parent, ObjectBuilder builder) {
            super(parent);
            this.builder = builder;
        }

        @Override
        public void postToken(int tokenType, String tokenString) {
            switch (state) {
            case 0: // just after class name
                switch (tokenType) {
                case LPAREN:
                    state = 1;
                    break;
                default:
                    state = 5;
                    instantiate();
                    postToken(tokenType, tokenString);
                }
                break;
            case 1: // after '(' parse positional arguments
                switch (tokenType) {
                case RPAREN: // end args
                    state = 5;
                    instantiate();
                    break;
                case IDENT:
                    // lookahead needed to differentiate A and A:A
                    if (scanner.getCharAterIdent() == COLON) {
                        instantiate();
                        state = 2; // 
                        postToken(tokenType, tokenString);
                    } else {
                        parseIdent(tokenString);
                    }
                    break;
                case COMMA:
                    break;
                default:
                    parseValue(tokenType, tokenString);
                }
                break;
             // parse named arguments
            case 2: // parse key in key-value pair
                switch (tokenType) {
                case RPAREN: // end args
                    state = 5;
                    break;
                case COMMA:
                    break;
                case IDENT:
                    key = tokenString;
                    state = 3;
                    break;
                default:
                    setParseError("comma  or ) expected");
                }
                break;
            case 3: // check ':'
                if (tokenType == COLON) {
                    state = 4;
                } else {
                    setParseError("':' expected");
                }
                break;
            case 4: // parse value in key-value pair
                parseValue(tokenType, tokenString);
                break;
            case 5: // parse list and map tails
                switch (tokenType) {
                case LBRACKET:
                    state = 1;
                    try {
                        ListBuilder listBuilder = builder.asListBuilder();
                        new ListParser(this, listBuilder);
                    } catch (Exception e) {
                        setParseError(e);
                    }
                    break;
                case LBRACE:
                    try {
                        MapBuilder mapBuilder = builder.asMapBuilder();
                        new MapParser(this, mapBuilder);
                    } catch (ParseException e) {
                        setParseError(e);
                    }
                    break;
                default:
                    parent.setValue(builder.getValue());
                }
                break;
            }
        }

        @Override
        public void setValue(Object value) {
            try {
                switch (state) {
                case 1: // after '(' parse positional arguments
                    if (args==null) {
                        args = new ArrayList<Object>();
                    }
                    args.add(value);
                    break;
                case 4: // parse value in key-value pair
                    builder.set(key, value);
                    state=2;
                    break;
                default:
                    setParseError("internal error: call to setValue when state="+state);
                }
            } catch (Exception e) {
                setParseError(e);
            }
        }

        boolean instantiated=false;
        
        protected void instantiate() {
            if (instantiated) {
                return;
            }
            instantiated=true;
            try {
                if (args==null) {
                    builder.instatntiate();
                } else {
                    builder.instatntiate(args.toArray());
                    args=null;
                }
            } catch (Exception e) {
                setParseError(e);
            }
        }

        @Override
        public void postParserError(String message) {
            throw new RuntimeException(message);
        }
    }
}

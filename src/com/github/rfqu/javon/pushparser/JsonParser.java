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

import com.github.rfqu.df4j.core.CompletableFuture;
import com.github.rfqu.javon.builder.JsonBulderFactory;
import com.github.rfqu.javon.builder.ListBuilder;
import com.github.rfqu.javon.builder.MapBuilder;
import com.github.rfqu.javon.parser.ParseException;

import static com.github.rfqu.javon.pushparser.Scanner.*;

public class JsonParser {
    Scanner scanner=new Scanner();
    protected JsonBulderFactory factory;
    protected CompletableFuture<Object> res;
    Parser currentParser;

    public JsonParser() {
        new RootTokenPort();
    }

    public CompletableFuture<Object> parseWith(JsonBulderFactory factory) throws Exception {
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


    /**
      * null, boolean, number, string, list, or map
      */
     protected void parseValue(int tokenType, String tokenString) {
         switch (tokenType) {
         case LBRACKET:
             currentParser.parseList();
             break;
         case LBRACE:
             currentParser.parseMap();
             break;
         case STRING:
             String str = tokenString;
             currentParser.setValue(str);
             break;
         case NUMBER:
             parseNumber(tokenString);
             break;
         case IDENT:
             parseIdent(tokenString);
             break;
         default:
             currentParser.setParseError("value expected, but " + token2Str(tokenType) + " seen");
         }
     }

     /**
      * null or boolean
      */
     protected void parseIdent(String tokenString) {
         if ("null".equals(tokenString)) {
             currentParser.setValue(null);
         } else if ("false".equals(tokenString)) {
             currentParser.setValue(Boolean.FALSE);
         } else if ("true".equals(tokenString)) {
             currentParser.setValue(Boolean.TRUE);
         } else {
             currentParser.setParseError("invalid identifier");
         }
     }

     protected void parseNumber(String tokenString) {
         Object res;
         try {
             res = Integer.valueOf(tokenString);
         } catch (NumberFormatException e) {
             try {
                 res = Double.valueOf(tokenString);
             } catch (NumberFormatException e1) {
                 currentParser.setParseError("bad number:" + tokenString);
                 return;
             }
         }
         currentParser.setValue(res);
     }

     abstract class Parser implements TokenPort {
        final Parser parent;

        Parser(Parser parent) {
            this.parent = parent;
            setCurrentParser(this);
        }

        protected void parseList() {
            ListBuilder builder = factory.newListBuilder();
            new ListParser(this, builder);
        }

        protected void parseMap() {
            MapBuilder builder = factory.newMapBuilder();
            new MapParser(this, builder);
        }

        protected void returnValue(Object value) {
            parent.setValue(value);
            setCurrentParser(parent);
        }

        public void postParserError(String message) {
            setParseError(message);
        }

        void setParseError(String message) {
            parent.setParseError(message);
        }

        void setParseError(Throwable e) {
            parent.setParseError(e);
        }

        public abstract void setValue(Object value);
    }

    class RootTokenPort extends Parser {
    	Object resValue=null;
    	Throwable resError=null;
    	boolean first=true;

        public RootTokenPort() {
            super(null);
        }

        @Override
        public void postToken(int tokenType, String tokenString) {
        	if (first) {
        		first=false;
                firstToken(tokenType, tokenString);
        	} else {
        		if (tokenType!=EOF) {
                    postParserError("EOF expected");
            	} else {
            		if ((resValue==null) && (resError==null)) {
                        postParserError("unexpected EOF");
                	}
            		if (res.isDone()) {
            			throw new RuntimeException();
            		}
                	if (resError!=null) {
                        res.postFailure(resError);
                	} else {
                		res.post(resValue);
                	}
            	}
        	}
        }

		protected void firstToken(int tokenType, String tokenString) {
			switch (tokenType) {
			case LBRACKET:
			    parseList();
			    break;
			case LBRACE:
			    parseMap();
			    break;
			default:
			    postParserError("{ or [ expected");
			}
		}

        @Override
        public void setValue(Object value) {
        	if (resValue==null) {
        		resValue=value;
        	} else {
        		setParseError("EOF expected");
        	}
        }

        void setParseError(Throwable e) {
        	if (resError==null) {
        		resError=scanner.toParseException(e); // only first error matters
        	}
        }

        void setParseError(String message) {
        	setParseError(new ParseException(message));
        }
    }

    class ListParser extends Parser {
        ListBuilder builder;

        public ListParser(Parser parent, ListBuilder builder) {
            super(parent);
            this.builder = builder;
        }

        @Override
        public void postToken(int tokenType, String tokenString) {
            switch (tokenType) {
            case COMMA:
                return;
            case RBRACKET:
                Object value = builder.getValue();
                returnValue(value);
                return;
            default:
                parseValue(tokenType, tokenString);
                // parent.postParserError("Identifier, { or [ expected");
            }
        }

        @Override
        public void postParserError(String message) {
            throw new RuntimeException(message);
        }

        @Override
        public void setValue(Object value) {
            builder.add(value);
        }
    }

    class MapParser extends Parser {
        MapBuilder builder;
        String key;
        int state = 0;

        public MapParser(Parser parent, MapBuilder builder) {
            super(parent);
            this.builder = builder;
        }

        @Override
        public void postToken(int tokenType, String tokenString) {
            switch (state) {
            case 0:
                switch (tokenType) {
                case COMMA:
                    return;
                case RBRACE:
                    Object value = builder.getValue();
                    returnValue(value);
                    return;
                case STRING:
                case IDENT:
                    key = tokenString;
                    state = 1;
                    break;
                default:
                    setParseError("Identifier, { or [ expected");
                }
                break;
            case 1:
                if (tokenType == COLON) {
                    state = 2;
                } else {
                    setParseError("':' expected");
                }
                break;
            case 2:
                parseValue(tokenType, tokenString);
                state=0;
            }
        }

        @Override
        public void postParserError(String message) {
            throw new RuntimeException(message);
        }

        @Override
        public void setValue(Object value) {
            builder.set(key, value);
        }
    }
}

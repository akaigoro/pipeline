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

import com.github.rfqu.codec.json.builder.JsonBulderFactory;
import com.github.rfqu.codec.json.builder.ListBuilder;
import com.github.rfqu.codec.json.builder.MapBuilder;
import com.github.rfqu.df4j.core.CompletableFuture;

import static com.github.rfqu.codec.json.pushparser.Scanner.*;

public class JsonParser extends TokenPort {
    protected Scanner scanner=new Scanner();
    protected final JsonBulderFactory factory;
    protected final CompletableFuture<Object> res;
    protected Parser currentParser;

    public JsonParser(JsonBulderFactory factory) {
        this.factory = factory;
        res = new CompletableFuture<Object>();
        new RootTokenPort();
        scanner.setTokenPort(this);
    }

    public CompletableFuture<Object> getResult() throws Exception {
        return res;
    }

    protected void setCurrentParser(Parser tp) {
        currentParser=tp;
    }

    public void postLine(String inp) {
        scanner.postLine(inp);
    }

    @Override
    public void postToken(int tokenType, String tokenString) {
        try {
            currentParser.postToken(tokenType, tokenString);
        } catch (Exception e) {
            currentParser.setParseError(e);
        }
    }

    @Override
    public void setParseError(Throwable e) {
        currentParser.setParseError(e);
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
             currentParser.postParseError("value expected, but " + token2Str(tokenType) + " seen");
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
             currentParser.postParseError("invalid identifier");
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
                 currentParser.postParseError("bad number:" + tokenString);
                 return;
             }
         }
         currentParser.setValue(res);
     }

     public abstract class Parser extends TokenPort {
        final Parser parent;

        protected Parser(Parser parent) {
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

        @Override
        public void setParseError(Throwable e) {
            parent.setParseError(e);
        }
/*
        public void postParserError(String message) {
            setParseError(message);
        }

        protected void setParseError(String message) {
            parent.setParseError(message);
        }
*/
        public abstract void setValue(Object value);
    }

    protected class RootTokenPort extends Parser {
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
        	} else if (tokenType!=EOF) {
                postParseError("EOF expected");
            } else if (!res.isDone()) {
                postParseError("unexpected EOF");
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
			    postParseError("{ or [ expected");
			}
		}

        @Override
        public void setValue(Object value) {
        	if (!res.isDone()) {
        	    res.post(value);
        	} else {
        	    postParseError("EOF expected");
        	}
        }

        @Override
        public void setParseError(Throwable e) {
            if (!res.isDone()) { // only first error matters TODO if it was value, not error
                res.postFailure(scanner.toParseException(e));
            }
        }
    }

    public class ListParser extends Parser {
        ListBuilder builder;

        public ListParser(Parser parent, ListBuilder builder) {
            super(parent);
            this.builder = builder;
        }

        @Override
        public void setParseError(Throwable e) {
            if (!res.isDone()) { // only first error matters
                res.postFailure(scanner.toParseException(e));
            }
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
        public void postParseError(String message) {
            throw new RuntimeException(message);
        }

        @Override
        public void setValue(Object value) {
            builder.add(value);
        }
    }

    public class MapParser extends Parser {
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
                    postParseError("Identifier, { or [ expected");
                }
                break;
            case 1:
                if (tokenType == COLON) {
                    state = 2;
                } else {
                    postParseError("':' expected");
                }
                break;
            case 2:
                parseValue(tokenType, tokenString);
                state=0;
            }
        }

        @Override
        public void postParseError(String message) {
            throw new RuntimeException(message);
        }

        @Override
        public void setValue(Object value) {
            builder.set(key, value);
        }
    }
}

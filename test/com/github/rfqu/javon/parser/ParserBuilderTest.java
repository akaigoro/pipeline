package com.github.rfqu.javon.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

//import org.junit.Test;

import com.github.rfqu.javon.parser.JavonParser;
import com.github.rfqu.javon.builder.impl.JavonBuilder;
import com.github.rfqu.javon.builder.impl.JsonList;
import com.github.rfqu.javon.builder.impl.JsonMap;

public class ParserBuilderTest extends ParserTest {

    protected void check(String inp, String exp) throws IOException, Exception {
        JavonParser mp=new JavonParser(inp);
        JavonBuilder bd = new JavonBuilder();
        bd.put("A", A.class);
        bd.put("B", B.class);
        bd.put("D", D.class);
        Object obj = mp.parseWith(bd);
        String res = obj.toString();
        assertEquals(exp, res);
    }

    public static class D extends JsonMap{

        public String toString() {
            StringBuilder sb=new StringBuilder();
            sb.append("D");
            if (!super.isEmpty()) {
                sb.append(super.toString());
            }
            return sb.toString();
        }
    }

    public static class B {
        int i;
        public B(int i) {this.i=i;}
        
        public String toString() {
            return "B("+i+")";
        }
    }
    
    public static class A extends JsonList {
        Integer i;
        Object a;

        public A() {
        }

        public A(Integer i) {
            this.i = i;
        }

        public Object getA() {
            return a;
        }

        public void setA(Object a) {
            this.a = a;
        }
        public String toString() {
            if (i==null && a==null) {
                return "A";
            }
            StringBuilder sb=new StringBuilder();
            sb.append("A(");
            if (i!=null) {
                sb.append(i);
            }
            if (a!=null) {
                if (i!=null) {
                    sb.append(',');
                }
                sb.append("a:");
                sb.append(a);
            }
            sb.append(')');
            if (!super.isEmpty()) {
                sb.append(super.toString());
            }
            return sb.toString();
        }
    }
}
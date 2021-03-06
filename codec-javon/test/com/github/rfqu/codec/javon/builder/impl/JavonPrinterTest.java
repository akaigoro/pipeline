package com.github.rfqu.codec.javon.builder.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.rfqu.codec.javon.builder.impl.JavonPrinter;
import com.github.rfqu.codec.json.builder.ListBuilder;
import com.github.rfqu.codec.json.builder.MapBuilder;

public class JavonPrinterTest {

    @Test
    public void testList0() throws Exception {
        JavonPrinter pr=new JavonPrinter();
        ListBuilder lb = pr.newListBuilder();
        String str=lb.getValue().toString();
        assertEquals("[]", str);
    }

    @Test
    public void testList1() throws Exception {
        JavonPrinter pr=new JavonPrinter();
        ListBuilder lb = pr.newListBuilder();
        lb.add(1);
        String str=lb.getValue().toString();
        assertEquals("[1]", str);
    }

    @Test
    public void testList2() throws Exception {
        JavonPrinter pr=new JavonPrinter();
        ListBuilder lb = pr.newListBuilder();
        lb.add("");
        lb.add(null);
        String str=lb.getValue().toString();
        assertEquals("[\"\",null]", str);
    }

    @Test
    public void testMap0() throws Exception {
        JavonPrinter pr=new JavonPrinter();
        MapBuilder lb = pr.newMapBuilder();
        String str=lb.getValue().toString();
        assertEquals("{}", str);
    }

    @Test
    public void testMap1() throws Exception {
        JavonPrinter pr=new JavonPrinter();
        MapBuilder lb = pr.newMapBuilder();
        lb.set("=",1.0);
        String str=lb.getValue().toString();
        assertEquals("{\"=\":1.0}", str);
    }

    @Test
    public void testMap2() throws Exception {
        JavonPrinter pr=new JavonPrinter();
        MapBuilder lb = pr.newMapBuilder();
        lb.set("1",1);
        lb.set("A",null);
        String str=lb.getValue().toString();
        assertEquals("{\"1\":1,\"A\":null}", str);
    }
}

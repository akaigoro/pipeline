Javon is a simple serialization language to store and load program configurations, small databases, and other complex objects.
In contrast to Json and XML, Javon is better suited for using from object-oriented languages like Java.

Compared to Json, Javon has following extentions:

- true objects, with class name, constructor parameters, and access to properties as named parameters:
<pre>
MulticastSocket(InetSocketAddress("localhost", 7777),
    loopbackMode:true,
    timeToLive:100)
</pre>

This is equivalent to following Java code:
<pre>
MulticastSocket ms=new MulticastSocket(InetSocketAddress("localhost", 7777));
ms.setLoopbackMode(true);
ms.setTimeToLive:100);
</pre>

 - True objects can implement java.util.Map and java.util.List
 - Keys in maps can be without quote marks.
 - Commas can be omitted.

<pre>MyMap(100) {
  "key1": 1
  key2: 2.0
  key3:
   [MyMap(10), {k1:"v1"}]
}

</pre>which is equivalent to:
<pre>
Map<String,Object> map=new MyMap(100);
map.put("key1", 1);
map.put("key2", 2.0);

List<Object> list=new JsonList();
list.add(new MyMap(10);
Map<String,Object> map2=new JsonMap();
map2.put("k1":"v1");
list.add(new MyMap(10));

map.put("key3", list);
</pre>

Note that if comma is omitted in the above example:
<pre>
   [MyMap(10) {k1:"v1"}]
</pre>

text remains syntactically correct but the list contains only one Map value:

<pre>
List<Object> list=new JsonList();
Map<String,Object> map2=new MyMap(10);
map2.put("k1":"v1");
list.add(map2);
</pre>

Architecture
------------
The library consits of three packages:
<br>com.github.rfqu.javon.parser, with main class JavonParser
<br>com.github.rfqu.javon.builder, with main class JavonBuilder
<br>com.github.rfqu.javon, which defines interfaces to connect parser and builder.
<br>Parser and builder are connected at run time. User can define own builder, provided
it implements required interfaces. For testing purposes, JavonPrinter class can be used.

Before connecting parser and builder, the latter should be informed which actual classes 
are denoted by the names in the text. This is demonstrated in the ParserBuilderTest.java.
Unnamed lists and maps are implemented by classes JsonList and JsonMap, respectively.

Grammar
-------
<br>VALUE: PRIMITIVE | LIST | MAP | OBJECT
<br>PRIMITIVE: null | false | true | NUMBER | STRING
<br>STRING: '"' SYMBOL* '" 
<br>LIST: '[' (VALUE)* ']'
<br>MAP: '{' (KEY ':' VALUE)* '}'
<br>KEY: STRING | IDENT
<br>OBJECT: IDENT ('(' POS_ARG* NAMED_ARG* ')' )? LIST? MAP?
<br>POS_ARG: VALUE
<br>NAMED_ARG: IDENT ':' VALUE
<br>IDENT: LETTER ( LETTER | DIGIT )*
<br>ROOT_OBJECT: LIST | MAP | OBJECT

Disclaimer
----------
the project is under heavy development and is not usable yet. If you are unterested in this project, drop me a line, of file a feature request at https://github.com/rfqu/pipeline/issues.


Pipeline is a linear chain of dataflow nodes with additional rule: after processing, messages are returned to the sender. Senders consider messages as a resource and stop working when receivers do not return messages. This prevents memory overflow when receivers are slow but senders continue to produce messages.

All subprojects rely on df4j-core project.

Subprojects:
------------

pipeline-core: extends df4j

pipeline-nio*: asyncronous network I/O, provides input and output streams of byte buffers.

codec* projects provide popular coding and encoding procedures. All codecs can be used in asynchronous (push) way, and sone also in synchronous (pull) way.

codec-charbyte: coding and decoding streams of character buffers in/from streams of byte buffers.

codec-json: reads and writes JSON files

codec-javon: reads and writes Javon files. Javon ia an extension to JSON, addint true java-like objects, while JSON objects still are converted to java.util.Maps.

codec-xml: planned.

Motivation
----------

Create a library which is able to replace Netty in many projects but is an order of magnitude smaller. A couple of facts about Netty: 
- volume of source code to build minimal echo server is about 2 megabytes
- 82 classes and interfaces implement/extend io.netty.util.concurrent.Future interface.


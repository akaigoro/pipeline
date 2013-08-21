package com.github.rfqu.pipeline.core;

import java.nio.Buffer;
import java.nio.CharBuffer;

import com.github.rfqu.df4j.core.Port;

public class BufChunk<B extends Buffer> extends Chunk {
    protected B buffer;

	public BufChunk(B buffer, Port<Chunk> returnPort) {
	    super(returnPort);
        this.buffer = buffer;
	}

    public B getClearBuffer() {
    	buffer.clear();
        return buffer;
    }
	
    public B getCompletedBuffer() {
    	buffer.flip();
        return buffer;
    }
	
    public B getBuffer() {
        return buffer;
    }
	
	public static BufChunk<CharBuffer> wrap(CharSequence s, Port<Chunk> returnPort) {
		return new BufChunk<CharBuffer>(CharBuffer.wrap(s), returnPort);
	}
}

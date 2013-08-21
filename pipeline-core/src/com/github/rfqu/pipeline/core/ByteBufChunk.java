package com.github.rfqu.pipeline.core;

import java.nio.Buffer;

import com.github.rfqu.df4j.core.Port;

public class ByteBufChunk extends BufChunk<Buffer> {

	public ByteBufChunk(Buffer buffer, Port<Chunk> returnPort) {
		super(buffer, returnPort);
	}
}

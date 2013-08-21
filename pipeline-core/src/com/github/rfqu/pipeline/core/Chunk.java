package com.github.rfqu.pipeline.core;

import com.github.rfqu.df4j.core.Port;

public class Chunk {
	protected Port<Chunk> returnPort;

	public Chunk(Port<Chunk> returnPort) {
        this.returnPort = returnPort;
	}

	public void free() {
		if (returnPort==null) {
			return;
		}
		returnPort.post(this);
	}
}

/* Copyright 2011-2012 by Alexei Kaigorodov
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.github.rfqu.pipeline.nio;

import java.nio.ByteBuffer;

import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.pipeline.core.ByteBufChunk;

/**
 * Request for a socket I/O operation.
 */
public abstract class SocketIORequest {
	private Port<SocketIORequest> chunkPort;
    protected ByteBufChunk chunk;

    private long timeout; // milliseconds
	private boolean timed;
    int result;
    Throwable exc;

    public void start(ByteBufChunk chunk, Port<SocketIORequest> handlerPort) throws Exception {
        this.chunk=chunk;
        this.chunkPort=handlerPort;
    	result = -2;
        exc = null;
        startIO();
   }

    protected abstract void startIO() throws Exception;
    
    public void prepareRead(ByteBuffer buf) {
//        buffer.flip(); TODO in IO
    }

    public void prepareWrite(ByteBuffer buf) {
//        buffer.clear(); TODO in IO
    }

	public ByteBuffer getBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

    public boolean isTimed() {
        return timed;
    }

    public void setTimed(boolean timed) {
        this.timed = timed;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void post(int res) {
		this.result=res;
		chunkPort.post(this);
	}

	public void postFailure(Throwable exc) {
		this.exc=exc;
		chunkPort.post(this);
	}

	public ByteBufChunk takeChunk() {
		ByteBufChunk chunk=this.chunk;
		this.chunk=null;
		this.chunkPort=null;
		return chunk;
	}
}
 
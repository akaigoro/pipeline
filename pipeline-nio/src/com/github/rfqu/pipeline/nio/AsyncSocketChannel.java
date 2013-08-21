/*
 * Copyright 2011-2012 by Alexei Kaigorodov
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

import java.io.IOException;
import java.net.SocketAddress;
import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.df4j.core.ListenableFuture;
import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.df4j.core.StreamPort;
import com.github.rfqu.pipeline.core.ByteBufChunk;

/**
 * Wrapper over {@link AsynchronousSocketChannel}.
 * Simplifies input-output, handling queues of I/O requests.
 * 
 * For client-side connections, instatntiate and call connect(addr).
 * For server-side connections, obtain new instances via AsyncServerSocketChannel.accept().
 *  
 * Internally, manages 2 input queues: one for reading requests and one for writing requests.
 * After request is served, it is sent to the port denoted by <code>replyTo</code>
 * property in the request.
 * 
 * IO requests can be posted immediately, but will be executed
 * only after connection completes.
 * If interested in the moment when connection is established,
 * add a listener to connEvent.
 */
public abstract class AsyncSocketChannel {

	/** handles write errors */
	private ActorPort<Void> handler;

	/**
     * for client-side socket
     * Starts connection to a server. IO requests can be queued immediately,
     * but will be executed only after connection completes.
     * If interested in the moment when connection is established, add a
     * listener to the returned ListenableFuture.
     * @return  same object as {@link getConnEvent }
     * 
     * @throws IOException
     */
    public abstract ListenableFuture<AsyncSocketChannel> connect(SocketAddress addr) throws IOException;
    
    /** signals connection completion
     *  @return same object as {@link connect }
     */
    public abstract ListenableFuture<AsyncSocketChannel> getConnEvent();

    /** signals connection closing */
    public abstract ListenableFuture<AsyncSocketChannel> getCloseEvent();

    public boolean isConnected() {
        return getConnEvent().isDone();
    }

    public boolean isClosed() {
        return getCloseEvent().isDone();
    }

    public abstract void close();
    
    //------------  writer's part
    
    /** sets handler for close and failure events reached the end of pipeline */
    public void setHandler(ActorPort<Void> handler) {
    	this.handler=handler;
    }

    //------------  reader's part
    
	/** sets the port where read data should be routed */
    public void setReader(Port<SocketIORequest> reader) {
    	this.readerOutput=reader;
    }
    
    //================================== implementaton
	protected Port<SocketIORequest> readerOutput;

    private StreamPort<ByteBufChunk> writeInput;
    
    

}

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
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

import com.github.rfqu.df4j.core.CompletableFuture;
import com.github.rfqu.df4j.core.ListenableFuture;
import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.pipeline.nio.AsyncSocketChannel;
import com.github.rfqu.pipeline.nio.SocketIORequest;

public class AsyncSocketChannel2 extends AsyncSocketChannel {
	protected volatile AsynchronousSocketChannel channel;
	protected final ConnectionCompleter connEvent = new ConnectionCompleter();
	protected final CompletableFuture<AsyncSocketChannel> closeEvent = new CompletableFuture<AsyncSocketChannel>();
	public final Reader reader = new Reader();
	public final Writer writer = new Writer();
	
	{
		reader.setConsumer(readerOutput);
	}

	/**
	 * starts connection process from client side
	 * 
	 * @throws IOException
	 */
	public ListenableFuture<AsyncSocketChannel> connect(SocketAddress addr)
			throws IOException {
		AsynchronousChannelGroup acg = AsyncChannelCroup.getCurrentACGroup();
		AsynchronousSocketChannel channel = AsynchronousSocketChannel.open(acg);
		channel.connect(addr, channel, connEvent);
		return connEvent;
	}

	public void setTcpNoDelay(boolean on) throws IOException {
		channel.setOption(StandardSocketOptions.TCP_NODELAY, on);
	}

	public ListenableFuture<AsyncSocketChannel> getConnEvent() {
		return connEvent;
	}

	@Override
	public ListenableFuture<AsyncSocketChannel> getCloseEvent() {
		return closeEvent;
	}

	/**
	 * disallows subsequent posts of requests; already posted requests would be
	 * processed.
	 * 
	 * @throws IOException
	 */
	@Override
	public void close() {
		try {
			closeEvent.post(this);
		} catch (IllegalStateException ok) {
		}
		try {
			channel.close();
		} catch (IOException e) {
		}
	}

	// ===================== inner classes

	/**
	 * callback for connection completion works both in client-side and
	 * server-side modes
	 */
	class ConnectionCompleter extends CompletableFuture<AsyncSocketChannel>
		implements CompletionHandler<Void, AsynchronousSocketChannel>
	{
		// ------------- CompletionHandler's backend

		@Override
		public void completed(Void result, AsynchronousSocketChannel asc) {
			channel = asc;
			reader.resume();
			writer.resume();
			super.post(AsyncSocketChannel2.this);
		}

		/**
		 * in server-side mode, channel==null
		 */
		@Override
		public void failed(Throwable exc, AsynchronousSocketChannel channel) {
			super.postFailure(exc);
		}
	}

	private abstract class ReaderWriter extends SocketIORequest
		implements CompletionHandler<Integer, Void>
	{
		protected Port<SocketIORequest> consumer;

		void setConsumer(Port<SocketIORequest> consumer) {
			this.consumer = consumer;
			if (channel != null) {
				consumer.post(this);
			}
		}

		/** indicates that the channel is ready */
		public void resume() {
			if (consumer != null) {
				consumer.post(this);
			}
		}

		@Override
		protected void startIO() throws Exception {
			if (isClosed()) {
				failed(new AsynchronousCloseException(), null);
				return;
			}
			ByteBuffer buffer = super.getBuffer();
			if (buffer.remaining() == 0) {
				failed(new IllegalArgumentException("empty buffer"), null);
				return;
			}
			doIO(buffer);
		}

		protected abstract void doIO(ByteBuffer buffer) throws Exception;

		// ------------- CompletionHandler's backend

		public void completed(Integer result, Void attach) {
			super.post(result);
			consumer.post(this);
		}

		public void failed(Throwable exc, Void attach) {
			if (exc instanceof AsynchronousCloseException) {
				AsyncSocketChannel2.this.close();
			} else {
				super.postFailure(exc);
				consumer.post(this);
			}
		}
	}

	private class Reader extends ReaderWriter {

		@Override
		protected void doIO(ByteBuffer buffer) throws Exception {
			if (super.isTimed()) {
				long timeout = super.getTimeout();
				channel.read(buffer, timeout, TimeUnit.MILLISECONDS, null, this);
			} else {
				channel.read(buffer, null, this);
			}
		}
	}

	private class Writer extends ReaderWriter {

		@Override
		protected void doIO(ByteBuffer buffer) throws Exception {
			if (super.isTimed()) {
				long timeout = super.getTimeout();
				channel.write(buffer, timeout, TimeUnit.MILLISECONDS, null, this);
			} else {
				channel.write(buffer, null, this);
			}
		}
	}
}
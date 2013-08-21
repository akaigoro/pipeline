package com.github.rfqu.pipeline.nio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.util.concurrent.TimeoutException;

import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.df4j.core.ActorVariable;
import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.pipeline.core.ByteBufChunk;
import com.github.rfqu.pipeline.core.Chunk;

public class AsyncBufChunkSource {
    protected final AsyncSocketChannel asch;
    protected final ActorPort<ByteBufChunk> consumer;
    protected ChunkQueue chunkQueue=new ChunkQueue();
    protected RequestHandler requestHandler=new RequestHandler();

    public AsyncBufChunkSource(AsyncSocketChannel asch, ActorPort<ByteBufChunk> consumer) {
        this.asch = asch;
        this.consumer = consumer;
    }

    /** initiates the buffer pool. At least one buffer must be submitted. */
    public void injectBuffer(ByteBuffer buf) {
    	Port<?> port=chunkQueue;
		@SuppressWarnings("unchecked")
		ByteBufChunk bbc = new ByteBufChunk(buf, (Port<Chunk>) port);
        chunkQueue.post(bbc);
    }

    class ChunkQueue extends ActorVariable<ByteBufChunk> {
    	Input<SocketIORequest> siorInp =new Input<SocketIORequest>();
    	
		@Override
		protected void act(ByteBufChunk chunk) throws Exception {
			SocketIORequest sior=siorInp.get();
			sior.start(chunk, requestHandler);
		}

		@Override
		protected void complete() throws Exception {
			// TODO Auto-generated method stub
			super.complete();
		}
    }
    
    class RequestHandler extends IOHandler  {

		@Override
		public void completed(SocketIORequest request, int res) {
			ByteBufChunk chunk=request.takeChunk();
			consumer.post(chunk);
			chunkQueue.siorInp.post(request);
		}

		@Override
		public void failed(SocketIORequest request, Throwable exc) {
			ByteBufChunk chunk=request.takeChunk();
			consumer.postFailure(exc);
			chunkQueue.post(chunk);
			chunkQueue.siorInp.post(request);
		}

		@Override
		public void timedOut(SocketIORequest request) {
			ByteBufChunk chunk=request.takeChunk();
			consumer.postFailure(new TimeoutException());
			chunkQueue.post(chunk);
			chunkQueue.siorInp.post(request);
		}

		@Override
		public void closed(SocketIORequest request) {
			ByteBufChunk chunk=request.takeChunk();
			consumer.postFailure(new AsynchronousCloseException());
			chunkQueue.post(chunk);
			chunkQueue.siorInp.post(request);
		}
    }
}
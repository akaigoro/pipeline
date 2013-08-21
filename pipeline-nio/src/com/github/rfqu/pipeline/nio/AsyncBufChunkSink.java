package com.github.rfqu.pipeline.nio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.util.concurrent.TimeoutException;

import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.df4j.core.ActorVariable;
import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.df4j.core.DataflowVariable.Input;
import com.github.rfqu.pipeline.core.ByteBufChunk;
import com.github.rfqu.pipeline.nio.AsyncBufChunkSource.ChunkQueue;
import com.github.rfqu.pipeline.nio.AsyncBufChunkSource.RequestHandler;

public class AsyncBufChunkSink extends ActorVariable<ByteBufChunk>
{
    protected final AsyncSocketChannel asch;
    protected RequestHandler requestHandler;
    protected Input<SocketIORequest> siorInp = new Input<SocketIORequest>();

    public AsyncBufChunkSink(AsyncSocketChannel asch) {
        this.asch = asch;
        requestHandler = new RequestHandler();
        asch.setReader(siorInp);
    }

    @Override
    public void close() {
        asch.close();
    }

    @Override
    public boolean isClosed() {
        return asch.isClosed();
    }

    @Override
    public void postFailure(Throwable exc) {
        // TODO Auto-generated method stub
        
    }

	
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
    
    class RequestHandler extends IOHandler  {

		@Override
		public void completed(SocketIORequest request, int res) {
			ByteBufChunk chunk=request.takeChunk();
			chunk.free();
			siorInp.post(request);
		}

		@Override
		public void failed(SocketIORequest request, Throwable exc) {
			ByteBufChunk chunk=request.takeChunk();
//			consumer.postFailure(exc);
			chunk.free();
			siorInp.post(request);
		}

		@Override
		public void timedOut(SocketIORequest request) {
			ByteBufChunk chunk=request.takeChunk();
//			consumer.postFailure(new TimeoutException());
			chunk.free();
			siorInp.post(request);
		}

		@Override
		public void closed(SocketIORequest request) {
			ByteBufChunk chunk=request.takeChunk();
//			consumer.postFailure(new AsynchronousCloseException());
			chunk.free();
			siorInp.post(request);
		}
    }
}
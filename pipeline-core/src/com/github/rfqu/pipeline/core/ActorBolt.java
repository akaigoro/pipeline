package com.github.rfqu.pipeline.core;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.Executor;

import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.df4j.core.DataflowNode;
import com.github.rfqu.df4j.core.Port;
import com.github.rfqu.df4j.core.StreamPort;

public abstract class ActorBolt<BT extends Buffer, C>
    extends DataflowNode
    implements ActorPort<BufChunk<BT>>
{
    private Pausable<?> chunkStreams=null;
    protected InpIterator<BT> inputChunkStream;

    public ActorBolt() {
    }

    public ActorBolt(Executor executor) {
        super(executor);
    }

    public abstract void setListener(ActorPort<C> listener);
    
    @Override
    public void close() {
        inputChunkStream.close();
    }

    @Override
    public boolean isClosed() {
        return inputChunkStream.isClosed();
    }

    @Override
    public void post(BufChunk<BT> m) {
        inputChunkStream.post(m);
    }
    
    @Override
    protected void act() {
        for (Pausable<?> p=chunkStreams; p!=null; p=p.next) {
            p.activate();
        }
        transform();
        for (Pausable<?> p=chunkStreams; p!=null; p=p.next) {
            p.deActivate();
        }
    }

    protected abstract void transform();
    
    abstract class Pausable<B extends Buffer>
        extends StreamInput<BufChunk<B>>
        implements StreamPort<BufChunk<B>>
    {
        private Pausable<?> next;
        protected B currentBuf;
        
        Pausable() {
            next=chunkStreams;
            chunkStreams=this;
        }
        
        public B getCurrentBuffer() {
            return currentBuf;
        }

        void activate(){};
        void deActivate(){};    
    }

    public class InpIterator<B extends Buffer> extends Pausable<B> {
        
        public void activate() {
            BufChunk<B> inpChunk = get();
            if (inpChunk!=null) { // may be null if chunk stream is completed
                currentBuf=inpChunk.getBuffer();
                if (!currentBuf.hasRemaining()) {
                    throw new IllegalArgumentException("empty chunks not allowed");
                }
            }
        }

        public boolean moveNext() {
            BufChunk<B> prevChunk = get();
            // get the next chunk from the queue, if any
            if (!super.moveNext()) {
                return false;
            }
            // only after the next chunk is taken, free previous chunk
            prevChunk.free();
            // activate current chunk
            currentBuf = get().getBuffer();
            if (!currentBuf.hasRemaining()) {
                throw new IllegalArgumentException("empty chunks not allowed");
            }
            return true;
        }

        @Override
        void deActivate() {
            if (currentBuf == null) {
                return;
            }
            if (currentBuf.hasRemaining()) {
                pushback();
            } else {
                get().free();
                currentBuf = null;
            }
        }
    }

    public class InpByteIterator extends InpIterator<ByteBuffer> implements ByteIterator {
        protected byte currentByte;

        @Override
		public void activate() {
			super.activate();
			if (currentBuf!=null) {
				currentByte=currentBuf.get();
			}
		}

        /** moves stream to the next byte
         */
        public boolean moveNextByte() {
            if (currentBuf==null) {
                return false;
            }
            if (currentBuf.hasRemaining()) {
                currentByte=currentBuf.get();
                return true;
            }
            if (!moveNext()) {
                return false;
            }
            currentByte=currentBuf.get();
            return true;
        }

		@Override
        public byte currentByte() {
            return currentByte;
        }
    }

    public class InpCharIterator
        extends InpIterator<CharBuffer>
        implements CharIterator
    {
        protected char currentChar;

        @Override
		public void activate() {
			super.activate();
			if (currentBuf!=null) {
				currentChar=currentBuf.get();
			}
		}

        /** moves stream to the next character
         */
        @Override
        public boolean moveNextChar() {
            if (currentBuf==null) {
                return false;
            }
            if (currentBuf.hasRemaining()) {
                currentChar=currentBuf.get();
                return true;
            }
            if (!moveNext()) { // also checks hasRemaining()
                return false;
            }
            currentChar=currentBuf.get();
            return true;
        }

        @Override
        public char currentChar() {
            return currentChar;
        }
    }

    public class OutpIterator<B extends Buffer>
        extends Pausable<B>
        implements ActorPort<BufChunk<B>>
    {
		private ActorPort<BufChunk<B>> outPort;

        public OutpIterator(ActorPort<BufChunk<B>> outPort) {
            if (outPort==null) {
                throw new IllegalArgumentException();
            }
            this.outPort = outPort;
        }

		public void activate() {
            if (currentBuf==null) {
                currentBuf=get().getClearBuffer(); // when act() starts, buffs not empty
            }
        }

        public boolean hasSpace() {
            return currentBuf.hasRemaining() || moveNext();
        }

        public boolean moveNext() {
            if ((currentBuf==null) || (currentBuf.position()==0)) {
                // do not send empty chuncks
                throw new IllegalStateException("attempt to post empty chunk");
            }
            post();
            if (!super.moveNext()) {
                return false;
            }
            // activate current chunk
            currentBuf=get().getClearBuffer();
            // empty chuncks not allowed
            return true;
        }
        
        private void post() {
            currentBuf.flip();
            currentBuf=null;
            outPort.post(get());
        }
        
        public void close() {
            deActivate();
            outPort.close();
            // TODO free buffers
        }

        public void deActivate() {
            // do not send empty chuncks
            if ((currentBuf!=null) && (currentBuf.position()>0)) {
                post();
            }
        }

        @Override
        public void postFailure(Throwable exc) {
            outPort.postFailure(exc);
        }
    }
    
    public class OutpByteIterator extends OutpIterator<ByteBuffer> {

        public OutpByteIterator(ActorPort<BufChunk<ByteBuffer>> outPort) {
            super(outPort);
        }

        public void injectChunks(int count, int buffLen) {
            for (int k=0; k<count; k++) {
            	ByteBuffer buf=ByteBuffer.allocate(buffLen);
                Port<?> pch = this;
                @SuppressWarnings("unchecked")
                Port<Chunk> pch1=(Port<Chunk>)pch;
                BufChunk<ByteBuffer> t=new BufChunk<ByteBuffer>(buf, pch1);
                post(t);
            }
        }

        /** hasSpace() must be already checked
         * 
         * @param ch character to write
         */
//        @Override
        public void add(byte b) {
            currentBuf.put(b);
        }
    }
    
    public class OutpCharIterator extends OutpIterator<CharBuffer> {

        public OutpCharIterator(ActorPort<BufChunk<CharBuffer>> outPort) {
            super(outPort);
        }

        public void injectChunks(int count, int buffLen) {
            for (int k=0; k<count; k++) {
            	CharBuffer buf=CharBuffer.allocate(buffLen);
                Port<?> pch = this;
                @SuppressWarnings("unchecked")
                Port<Chunk> pch1=(Port<Chunk>)pch;
                BufChunk<CharBuffer> t=new BufChunk<CharBuffer>(buf, pch1);
                post(t);
            }
        }

        /** hasSpace() must be already checked
         * 
         * @param ch character to write
         */
//        @Override
        public void add(char ch) {
            currentBuf.put(ch);
        }
    }
}
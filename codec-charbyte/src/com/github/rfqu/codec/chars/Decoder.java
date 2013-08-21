package com.github.rfqu.codec.chars;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.pipeline.core.ActorBolt;
import com.github.rfqu.pipeline.core.BufChunk;

/** 
 * Converts chars in ByteBuffers to butes in CharBuffers
 * @author kaigorodov
 *
 */
class Decoder extends ActorBolt <ByteBuffer, BufChunk<CharBuffer>> {
	static final ByteBuffer emptyByteBuf=(ByteBuffer) ByteBuffer.wrap(new byte[]{}).flip();

    private int buffLen;
    protected final Charset charset;
	protected final CharsetDecoder charSetDecoder;
    protected  OutpCharIterator out;
    
    public Decoder(Charset charset, int buffLen) {
        this.charset=charset;
        charSetDecoder = charset.newDecoder();
        charSetDecoder.reset();
        this.buffLen=buffLen;
        inputChunkStream=new InpIterator<ByteBuffer>();    }

    public Decoder(Charset charset, ActorPort<BufChunk<CharBuffer>> outPort, int buffLen) {
        this(charset, buffLen);
        setListener(outPort);
    }

    @Override
    public void setListener(ActorPort<BufChunk<CharBuffer>> outPort) {
        if (outPort==null) {
            throw new IllegalArgumentException();
        }
        out=new OutpCharIterator(outPort);
        out.injectChunks(2, buffLen);
    }

	@Override
	protected void transform() {
		for (;;) {
            CharBuffer outBuf = out.getCurrentBuffer();
            if (inputChunkStream.isClosed()) {
                CoderResult cr=charSetDecoder.decode(emptyByteBuf, outBuf, true);
                if (cr.isOverflow()) {
                	if (!out.moveNext()) {
                		return;
                	}
                }
                cr=charSetDecoder.flush(outBuf);
                if (cr.isOverflow()) {
                	if (!out.moveNext()) {
                		return;
                	}
                }
                out.close();
                return;
            }
            ByteBuffer inBuf=inputChunkStream.getCurrentBuffer();
            CoderResult cr=charSetDecoder.decode(inBuf, outBuf, false);
            if (cr.isUnderflow()) {
            	if (!inputChunkStream.moveNext()) {
            		return;
            	}
            }
            if (cr.isOverflow()) {
            	if (!out.moveNext()) {
            		return;
            	}
            }
		}
	}
}
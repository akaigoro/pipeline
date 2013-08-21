package com.github.rfqu.codec.chars;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.pipeline.core.ActorBolt;
import com.github.rfqu.pipeline.core.BufChunk;

/** 
 * Converts chars in CharBuffers to bytes in ByteBuffers
 * @author kaigorodov
 *
 */
class Encoder extends ActorBolt <CharBuffer, BufChunk<ByteBuffer>> {
	static final CharBuffer emptyCharBuf=(CharBuffer) CharBuffer.wrap(new char[]{}).flip();
	protected final Charset charset;
	protected final CharsetEncoder charSetEncoder;
    protected OutpByteIterator out;
    private int buffLen;
    
	public Encoder(Charset charset, ActorPort<BufChunk<ByteBuffer>> outPort, int buffLen) {
        this(charset, buffLen);
        setListener(outPort);
	}
	
    public Encoder(Charset charset, int buffLen) {
        this.buffLen=buffLen;
        this.charset=charset;
        charSetEncoder = charset.newEncoder();
        charSetEncoder.reset();
        inputChunkStream=new InpIterator<CharBuffer>();
    }

    @Override
    public void setListener(ActorPort<BufChunk<ByteBuffer>> outPort) {
        if (outPort==null) {
            throw new IllegalArgumentException();
        }
        out=new OutpByteIterator(outPort);
        out.injectChunks(2, buffLen);
    }

	@Override
	protected void transform() {
		for (;;) {
            ByteBuffer outBuf = out.getCurrentBuffer();
            if (inputChunkStream.isClosed()) {
                CoderResult cr=charSetEncoder.encode(emptyCharBuf, outBuf, true);
                if (cr.isOverflow()) {
                	if (!out.moveNext()) {
                		return;
                	}
                }
                cr=charSetEncoder.flush(outBuf);
                if (cr.isOverflow()) {
                	if (!out.moveNext()) {
                		return;
                	}
                }
                out.close();
                return;
            }
            CharBuffer inBuf=inputChunkStream.getCurrentBuffer();
            CoderResult cr=charSetEncoder.encode(inBuf, outBuf, false);
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
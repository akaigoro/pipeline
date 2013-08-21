package com.github.rfqu.pipeline.core;

import java.nio.CharBuffer;

import com.github.rfqu.df4j.core.ActorPort;
import com.github.rfqu.pipeline.core.ActorBolt;
import com.github.rfqu.pipeline.core.BufChunk;

class TransformNullChar extends ActorBolt<CharBuffer, BufChunk<CharBuffer>> {
    int buffLen;
    OutpCharIterator out;
    InpCharIterator inp=new InpCharIterator();
    
    public TransformNullChar(int buffLen){
        this.buffLen=buffLen;
        super.inputChunkStream=inp;
    }

    public TransformNullChar(ActorPort<BufChunk<CharBuffer>> outPort, int buffLen){
        this(buffLen);
        setListener(outPort);
    }

    @Override
    public void setListener(ActorPort<BufChunk<CharBuffer>> listener) {
        out=new OutpCharIterator(listener);
        out.injectChunks(2, buffLen);
    }

	@Override
	protected void transform() {
		for (;;) {
            if (inp.isClosed()) {
                out.close();
                return;
            }
            char ch=inp.currentChar();
            out.add(ch);
            if (!inp.moveNextChar()) {
                if (inp.isClosed()) {
                    out.close();
                }
                return;
            }
            if (!out.hasSpace()) {
                return;
            }
		}
	}

    @Override
    protected void handleException(Throwable exc) {
        out.postFailure(exc);
    }
}
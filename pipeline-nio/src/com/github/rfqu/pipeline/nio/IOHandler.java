package com.github.rfqu.pipeline.nio;

import java.nio.channels.AsynchronousCloseException;

import com.github.rfqu.df4j.core.Port;

public abstract class IOHandler
  implements Port<SocketIORequest>
{
    @Override
    public void post(SocketIORequest r) {
        if (r.exc == null) {
            if (r.result == -1) {
                closed(r);
            } else {
                completed(r, r.result);
            }
        } else {
            if (r.exc instanceof AsynchronousCloseException) {
                closed(r);
// TODO                
//            } else if (exc instanceof InterruptedByTimeoutException) {
//                handler.timedOut(r);
            } else {
                failed(r, r.exc);
            }
        }
    }

	public void completed(SocketIORequest request, int result) {
	}

	public void timedOut(SocketIORequest request) {
	}

	public void closed(SocketIORequest request) {
	}

	public void failed(SocketIORequest request, Throwable exc) {
	}

}
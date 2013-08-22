package com.github.rfqu.pipeline.core;

import java.util.concurrent.Executor;

import com.github.rfqu.df4j.core.DataflowNode;

/**
 * {@link mySinkPort} and {@link myreturnPort} must be initiakized in subclasses
 * 
 * @author kaigorodov
 *
 * @param <I> type of input messages
 * @param <O> type of output messages
 */
public abstract class Transformer<I, O>
    extends DataflowNode
    implements Sink<I>, Source<O>
{
	/**  here input messages arrive */
	protected PausableInp mySinkPort;
	/** there input messages return */
	protected Source<I> source;

	/** there output messages go */
	protected Sink<O> sink;
	/** here output messages return */
	protected PausableOut myReturnPort;

	private Pausable<?> chunkStreams=null;

    public Transformer() {
    }

    public Transformer(Executor executor) {
        super(executor);
    }

    @Override
	public void setSource(Source<I> source) {
		if (this.source != null) {
			return; // avoid infinite recursion
		}
		this.source = source;
		source.setSink(this);
	}

	@Override
	public void post(I m) {
		mySinkPort.post(m);
	}

	@Override
	public void postFailure(Throwable exc) {
		handleException(exc);
	}

	public void close() {
		mySinkPort.close();
	}

	public void setSink(Sink<O> sink) {
		if (this.sink != null) {
			return; // avoid infinite recursion
		}
		this.sink = sink;
		sink.setSource(this);
	}

	@Override
	public void recycle(O message) {
		myReturnPort.post(message);
	}

	@Override
	public void shut() {
		myReturnPort.close();
	}


    @Override
    protected void act() {
        for (Pausable<?> p=chunkStreams; p!=null; p=p.next) {
            p.activate();
        }
        
		if (mySinkPort.isClosed()) {
			complete();

	        for (Pausable<?> p=chunkStreams; p!=null; p=p.next) {
	            p.shutDown();
	        }
		} else {
			I inmessage=mySinkPort.get();
			O outmessage=myReturnPort.get();
	        transform(inmessage, outmessage);

	        for (Pausable<?> p=chunkStreams; p!=null; p=p.next) {
	            p.deActivate();
	        }
		}
    }

    /**
     * input is closed; pass closing signal further
     */
    protected void complete() {
    	sink.close();
	}

    @Override
    protected void handleException(Throwable exc) {
		sink.postFailure(exc);
    }
    
	/**
	 * {$link StreamPort} with activate/deactivate methods
	 *
	 * @param <T>
	 */
    protected class Pausable<T> extends StreamInput<T> {
        private Pausable<?> next;
        
        Pausable() {
            next=chunkStreams;
            chunkStreams=this;
        }
        
        protected void shutDown() {}
        protected void activate(){};
        protected void deActivate(){};    
    }

    protected class PausableInp extends Pausable<I> {

		@Override
		protected void deActivate() {
			source.recycle(super.get());
		}
    	
    }

    protected class PausableOut extends Pausable<O>  {

		@Override
		protected void deActivate() {
			sink.post(super.get());
		}

		@Override
		protected void shutDown() {
			while (super.moveNext()) {
				// free output containers
			}
		}

	}


    protected abstract void transform(I inmessage, O outmessage);
	}
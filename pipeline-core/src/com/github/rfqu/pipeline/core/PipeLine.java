package com.github.rfqu.pipeline.core;

import java.util.ArrayList;

import com.github.rfqu.df4j.core.CompletableFuture;
import com.github.rfqu.df4j.core.DataflowVariable;
import com.github.rfqu.df4j.core.ListenableFuture;

/**
 * PipeLine p=new PipeLine();
 * p.setSource(new MySource())
 *  .addTransformer(new MyTransformer1())
 *  .addTransformer(new MyTransformer2())
 *  .setSink(new MySink());
 *  
 *  p.start();
 *  p.get(); // wait pipeline to finish; can rethrow errors. 
 * 
 * @author Alexei Kaigorodov
 *
 */
public class PipeLine {
    ArrayList<Bolt> nodes=new ArrayList<Bolt>();
    CompletableFuture<Object> completer=new CompletableFuture<Object>(){
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (super.cancel(mayInterruptIfRunning)) {
                stop();
                return true;
            }
            return false;
        }
    };

    public Bolt getSource() {
        return nodes.get(0);
    }
    
    public <O> Connector<O> setSource(Source<O> source) {
        nodes.add(source);
        source.setContext(completer);
        return new Connector<O>(source);
    }

    public <T> void connect(Source<T> source, Sink<T> sink) {
        nodes.add(sink);
        sink.setContext(completer);
        source.setSinkPort(sink.getInputPort());
        sink.setReturnPort(source.getReturnPort());
    }

    public void start() {
        for (Bolt node: nodes) {
            node.start();
        }
    }

    public void stop() {
        for (Bolt node: nodes) {
            node.stop();
        }
    }

    public ListenableFuture<Object> getFuture() {
        return completer;
    }

    public class Connector<I> {
        Source<I> source;
        public Connector(Source<I> source) {
            this.source = source;
        }
        
        public <O> Connector<O> addTransformer(Transformer<I,O> tr) {
            PipeLine.this.connect(source, tr);
            return new Connector<O>(tr);
        }
        
        public PipeLine setSink(Sink<I> sink) {
            PipeLine.this.connect(source, sink);
            return PipeLine.this;
        }
    }

    public String getStatus() {
        StringBuilder sb=new StringBuilder();
        for (Bolt node: nodes) {
            if (node instanceof DataflowVariable) {
                DataflowVariable dfv=(DataflowVariable)node;
                String status=dfv.getStatus();
                sb.append(dfv.getClass().getSimpleName());
                sb.append(": ");
                sb.append(status);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
// TODO reaction to multiple postFailures and postFailure after post(value)
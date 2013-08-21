/*
 * Copyright 2011-2013 by Alexei Kaigorodov
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
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Executor;

import com.github.rfqu.df4j.core.DFContext;
import com.github.rfqu.df4j.core.DFContext.ItemKey;
import com.github.rfqu.df4j.core.DataflowVariable.Semafor;

public class SelectorThread implements Runnable, Executor {
    DFContext context;
    private Thread thrd;
	// The selector we'll be monitoring
	Selector selector;
    private LinkedList<Runnable> tasks=new LinkedList<Runnable>();
    private boolean running=false;

    public SelectorThread(DFContext context) throws IOException {
        this.context=context;
        // Create a new selector
        this.selector = Selector.open();
		thrd=new Thread(this);
		thrd.setDaemon(true);
		thrd.setName("SelectorThread");
		thrd.start(); // TODO kill suicide when not used
    }

    @Override
    public void execute(final Runnable task) {
        boolean doFire;
        synchronized (this) {
            tasks.add(task);
            doFire = !running;
            running = true;
        }
//        System.err.println("task enqueued:"+task);
        if (doFire) {
            selector.wakeup();
        }
    }

    public void run() {
	    DFContext.setCurrentContext(context);
		while (selector.isOpen() && !Thread.interrupted()) {
            try {
                if (selector.select()!=0) {
                    // Iterate over the set of keys for which events are available
                    Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                    while (selectedKeys.hasNext()) {
                        SelectionKey key = selectedKeys.next();
                        selectedKeys.remove();

                        if (!key.isValid()) {
                            break;
                        }

                        SelectorListener listener=(SelectorListener)key.attachment();
                        if (listener.key!=key) {
                            throw new RuntimeException("keys different");
                        }
                        int interestOps = key.interestOps();
                        key.interestOps(0);
                        listener.run(interestOps);
                    }
                }
                for (;;) {
                    Runnable task;
                    synchronized (this) {
                        task=tasks.poll();
                        if (task==null) {
                            running=false;
                            break;
                        }
                    }
//                    System.err.println("task started:"+task);
                    task.run();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
		}
	}
    
    //--------------------- inner class
    public class SelectorListener implements Runnable {
        private SelectorListenerUser asyncChannel;
        SelectionKey key;
        /** set of SelectionKeys: OP_ACCEPT, OP_CONNECT, OP_READ, OP_WRITE */
        private int interestBits=0;
        private Semafor[] semafores=new Semafor[5];
        boolean fired=false;
        
        SelectorListener(SelectorListenerUser asyncChannel) throws ClosedChannelException {
            this.asyncChannel=asyncChannel;
        }

        /** 
         * Raises bit in the key's interest operations.
         * When selector return this key, the semaphore will be notified.
         * Note that if this channel has two bits raised simultaneously,
         * and one of them fired, then both semafores will be notified, 
         * one of them receiving false notification.
         * @param bit one of SelectorKey constants
         * @throws ClosedChannelException 
         */
        synchronized void interestOn(int bit, Semafor sema) throws ClosedChannelException {
            if (sema==null) {
                throw new IllegalArgumentException("sema=null");
            }
            int bitPos=bitPosByBit[bit];
            semafores[bitPos]=sema;
            if ((interestBits&bit)!=0) { // bit set already
//                System.err.println("interest On: "+bitPosToString[bitPos]+ " already");
                return;
            }
//            System.err.println("interest On: "+bitPosToString[bitPos]);
            interestBits|=bit;
            if (fired) {
                return;
            }
            fired=true;
            SelectorThread.this.execute(this);
        }
        
        // to set inerest on
        public synchronized void run(){
            if (key==null) {
                try {
                    key=asyncChannel.getChannel().register(selector, interestBits, this);
                } catch (ClosedChannelException e) {
                    run(interestBits);// let listeners retry and receive the exception
                }
            } else {
                key.interestOps(interestBits);
            }
//            System.err.println("set :"+interestBits);
        }
        
        public synchronized void run(int interestOps) {
//            System.err.println(" get:"+interestOps);
            int cout=0;

            interestBits=0;
//            System.err.println("listener started: "+asyncChannel+"; bits="+interestOps);
            try {
                for (int bitPos=0; bitPos<5; bitPos++) {
                    int bit=1<<bitPos;
                    if ((interestOps&bit)==0) continue;
                    cout++;
                    if (semafores[bitPos]==null) {
                    	// unwanted signal received
//                        System.err.println("semafores["+bitPos+"]==null for "+bitPosToString[bitPos]);
                    	continue;
                    }
                    semafores[bitPos].up();
                    semafores[bitPos]=null;
                    /*
                    if ((interestBits&bit)==0) {  // bit unset already
                        System.err.println("interest Off: "+bitPosToString[bitPos]+ " already");
                    } else {
                        System.err.println("interest Off: "+bitPosToString[bitPos]);
                    }
                    */
                }
            } catch (CancelledKeyException e) {
                 asyncChannel.close();
            }
            if(cout>1) {
                System.err.println(" get:"+interestOps+" -- "+cout);
            }
        }

    }
    
    private static final int[] bitPosByBit = {0,0,1,0,2,0,0,0,3,0,0,0,0,0,0,0,4};
    @SuppressWarnings("unused")
	private static final String[] bitPosToString={"READ","UNISED","WRITE","ACCEPT","CONNECT"};

    //--------------------- context
    
    private static ItemKey<SelectorThread> selectorThreadKey
        = DFContext.getCurrentContext().new ItemKey<SelectorThread>()
    {
        @Override
        protected SelectorThread initialValue(DFContext context) {
            try {
                return new SelectorThread(context);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        
    };
    
    public static SelectorThread getCurrentSelectorThread() {
        return selectorThreadKey.get();
    }
}

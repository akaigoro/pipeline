/*
 * Copyright 2011 by Alexei Kaigorodov
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.github.rfqu.codec.chars;

import java.util.concurrent.Executor;

import com.github.rfqu.codec.BytePort;
import com.github.rfqu.codec.ByteSource;
import com.github.rfqu.codec.CharPort;
import com.github.rfqu.codec.CharSource;
import com.github.rfqu.df4j.codec.util.Task;

public class UTF82Byte extends Task implements CharPort, ByteSource {
    CharSource charSource;
    
    public UTF82Byte(CharSource charSource, Executor executor) {
        super(executor);
        this.charSource=charSource;
    }

    BytePort out;
    int num; // of cont bytes
    int data;

    @Override
    public void postChar(char ch) {
        if (ch<128) {
            out.postByte((byte) ch);
        } else if (ch<2048) { // 110x xxxx 10xx xxxx
            byte b=(byte) (0xC0|(ch>>6));
            out.postByte(b);
            b=(byte) (0x80|(ch&0x37));
            out.postByte(b);
        } else { // 1110 xxxx 10xx xxxx 10xx xxxx
            byte b=(byte) (0xE0|(ch>>12));
            out.postByte(b);
            b=(byte) (0x80|((ch>>6)&0x37));
            out.postByte(b);
            b=(byte) (0x80|(ch&0x37));
            out.postByte(b);
        }
    }

    @Override
    public void postEOF() {
        out.postEOF();
    }

    @Override
    public void demand(BytePort sink) {
        this.out = sink;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }
}

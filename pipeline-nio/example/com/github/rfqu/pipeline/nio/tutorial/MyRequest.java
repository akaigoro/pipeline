package com.github.rfqu.pipeline.nio.tutorial;

import java.nio.ByteBuffer;

import com.github.rfqu.pipeline.nio.SocketIORequest;

public class MyRequest extends SocketIORequest<MyRequest> {
    public MyRequest() {
        this(1024);
    }
    public MyRequest(int size) {
        super(ByteBuffer.allocate(size));
    }
}
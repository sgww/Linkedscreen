package com.videoexpress.nettyserverlibriry.client;

import android.support.v4.util.ArrayMap;

import com.videoexpress.nettyserverlibriry.Test;
import com.videoexpress.nettyserverlibriry.listener.OnReceiveListener;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by user on 2016/10/27.
 */
@ChannelHandler.Sharable
public class Dispatcher extends SimpleChannelInboundHandler<Test.ProtoTest> {
    private ArrayMap<Integer, OnReceiveListener> receiveListenerHolder;

    public Dispatcher() {
        receiveListenerHolder = new ArrayMap<>();
    }

    public void holdListener(Test.ProtoTest test, OnReceiveListener onReceiveListener) {
        receiveListenerHolder.put(test.getId(), onReceiveListener);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Test.ProtoTest protoTest) throws Exception {
        if (receiveListenerHolder.containsKey(protoTest.getId())) {
            OnReceiveListener listener = receiveListenerHolder.get(protoTest.getId());
            if (listener != null) {
                listener.handleReceive(protoTest.getContent());
            }
        }
    }
}

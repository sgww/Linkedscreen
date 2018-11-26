package com.videoexpress.nettyserverlibriry.handler;

import android.util.Log;

import com.videoexpress.nettyserverlibriry.Test;
import com.videoexpress.nettyserverlibriry.client.ClientSet;
import com.videoexpress.nettyserverlibriry.listener.ServerListener;
import com.videoexpress.nettyserverlibriry.listener.SocketMsgListener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Describe
 */
public class ServerHandler extends SimpleChannelInboundHandler<Test.ProtoTest> {
    private String TAG = "ServerHandler";
    private SocketMsgListener socketMsgListener;//消息回调
    private ServerListener onServerConnectListener;//客户端连接回调
    private boolean channelIsActivi;//client socket 是否连接

    public ServerHandler(SocketMsgListener socketMsgListener, ServerListener onServerConnectListener) {
        this.socketMsgListener = socketMsgListener;
        this.onServerConnectListener = onServerConnectListener;
    }



    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Test.ProtoTest protoTest) throws Exception {
        Log.d(TAG, "channelRead0: " + channelHandlerContext.name());
        //当前客户端是否已经连接上了
        if (!ClientSet.onlineUsers.keySet().contains(protoTest.getId())) {
            ClientSet.onlineUsers.put(protoTest.getId(), channelHandlerContext);
            onServerConnectListener.clientConnect(protoTest.getId());
        }
        Test.ProtoTest res = Test.ProtoTest.newBuilder()
                .setId(protoTest.getId())
                .setTitle("res" + protoTest.getTitle())
                .setContent("res" + protoTest.getContent())
                .build();
        channelHandlerContext.writeAndFlush(res);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //客户端主动断开连接
        Log.i(TAG, "client close");
        channelIsActivi = false;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.i(TAG, "client connect");
        channelIsActivi = true;
    }
}
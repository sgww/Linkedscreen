package com.videoexpress.nettyserverlibriry.client;

import android.util.Log;

import com.videoexpress.nettyserverlibriry.Test;
import com.videoexpress.nettyserverlibriry.listener.ClientListener;
import com.videoexpress.nettyserverlibriry.listener.OnReceiveListener;
import com.videoexpress.nettyserverlibriry.listener.ServerListener;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 * Created by vinda on 2018/10/18.
 * Describe:socket客户端
 * qq:1304314404
 */

public class NettyClient {
    private static final String TAG = "NettyClient";
    private static NettyClient INSTANCE;
    private InetSocketAddress mServerAddress;//地址
    private Bootstrap mBootstrap;//
    private Channel mChannel;//通道
    private EventLoopGroup mWorkerGroup;//事件循环
    private ServerListener onServerConnectListener;
    private Dispatcher mDispatcher;//分发调度器

    public static NettyClient getInstance() {
        if (INSTANCE == null) {
            synchronized (NettyClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NettyClient();
                }
            }
        }
        return INSTANCE;
    }

    private NettyClient() {
        mDispatcher = new Dispatcher();
    }


    /**
     * 连接
     * @param socketAddress
     * @param clientListener
     */
    public void connect(final InetSocketAddress socketAddress, ClientListener clientListener) {
        if (mChannel != null && mChannel.isActive()) {
            return;
        }
        mServerAddress = socketAddress;
        this.onServerConnectListener = onServerConnectListener;

        if (mBootstrap == null) {
            mWorkerGroup = new NioEventLoopGroup();
            mBootstrap = new Bootstrap();
            mBootstrap.group(mWorkerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("decoder", new ProtobufDecoder(Test.ProtoTest.getDefaultInstance()));
                            pipeline.addLast("encoder", new ProtobufEncoder());
                            pipeline.addLast("handler", mDispatcher);

                        }
                    })
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        }

        ChannelFuture future = mBootstrap.connect(mServerAddress);
        future.addListener(mConnectFutureListener);
    }
    private ChannelFutureListener mConnectFutureListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture pChannelFuture) throws Exception {
            if (pChannelFuture.isSuccess()) {
                mChannel = pChannelFuture.channel();
                if (onServerConnectListener != null) {
                    onServerConnectListener.onConnectSuccess();
                }
                Log.i(TAG, "operationComplete: connected!");
            } else {
                if (onServerConnectListener != null) {
                    onServerConnectListener.onConnectFailed();
                }
                Log.i(TAG, "operationComplete: connect failed!");
            }
        }
    };

    /**
     * 客户端发送socket消息
     * @param msg
     * @param listener
     */
    public synchronized void send(Test.ProtoTest msg, OnReceiveListener listener) {
        if (mChannel == null) {
            Log.e(TAG, "send: channel is null");
            return;
        }

        if (!mChannel.isWritable()) {
            Log.e(TAG, "send: channel is not Writable");
            return;
        }

        if (!mChannel.isActive()) {
            Log.e(TAG, "send: channel is not active!");
            return;
        }
        mDispatcher.holdListener(msg, listener);
        if (mChannel != null) {
            mChannel.writeAndFlush(msg);
        }

    }

}

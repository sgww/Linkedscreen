package com.videoexpress.nettyserverlibriry.server;

import com.videoexpress.nettyserverlibriry.Test;
import com.videoexpress.nettyserverlibriry.client.ClientSet;
import com.videoexpress.nettyserverlibriry.handler.InBoundHandler;
import com.videoexpress.nettyserverlibriry.handler.OutBoundHandler;
import com.videoexpress.nettyserverlibriry.handler.ServerHandler;
import com.videoexpress.nettyserverlibriry.listener.ServerListener;
import com.videoexpress.nettyserverlibriry.listener.SocketMsgListener;

import java.util.Iterator;
import java.util.Map;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 * Created by Vinda on 2016/10/27.
 * Describe netty服务端实现类
 */

public class NettyServer {
    private ServerBootstrap mServerBootstrap;
    private EventLoopGroup mWorkerGroup;
    private ChannelFuture channelFuture;
    private boolean isInit;
    private SocketMsgListener socketMsgListener;
    private ServerListener serverListener;

    private static NettyServer INSTANCE;

    public final static int PORT_NUMBER = 8888;

    private NettyServer(SocketMsgListener socketMsgListener, ServerListener onServerConnectListener) {
        this.socketMsgListener = socketMsgListener;
        this.serverListener = onServerConnectListener;
    }

    private NettyServer() {

    }

    public static NettyServer getInstance(SocketMsgListener socketMsgListener, ServerListener onServerConnectListener) {
        if (INSTANCE == null) {
            synchronized (NettyServer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NettyServer(socketMsgListener, onServerConnectListener);
                }
            }
        }
        return INSTANCE;
    }

    public static NettyServer getInstance() {
        if (INSTANCE == null) {
            synchronized (NettyServer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NettyServer();
                }
            }
        }
        return INSTANCE;
    }

    public void init() {
        if (isInit) {
            return;
        }
        //创建worker线程池，这里只创建了一个线程池，使用的是netty的多线程模型
        mWorkerGroup = new NioEventLoopGroup();
        //服务端启动引导类，负责配置服务端信息
        mServerBootstrap = new ServerBootstrap();
        mServerBootstrap.group(mWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new ChannelInitializer<NioServerSocketChannel>() {
                    @Override
                    protected void initChannel(NioServerSocketChannel nioServerSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioServerSocketChannel.pipeline();
                        pipeline.addLast("ServerSocketChannel out", new OutBoundHandler());
                        pipeline.addLast("ServerSocketChannel in", new InBoundHandler());
//                        pipeline.addLast(new SimpleServerHandler());
                    }
                })
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //为连接上来的客户端设置pipeline
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("decoder", new ProtobufDecoder(Test.ProtoTest.getDefaultInstance()));
                        pipeline.addLast("encoder", new ProtobufEncoder());
                        pipeline.addLast("out1", new OutBoundHandler());
                        pipeline.addLast("out2", new OutBoundHandler());
                        pipeline.addLast("in1", new InBoundHandler());
                        pipeline.addLast("in2", new InBoundHandler());
                        pipeline.addLast(new ServerHandler(socketMsgListener, serverListener));
                    }
                });

        channelFuture = mServerBootstrap.bind(PORT_NUMBER);
        if (channelFuture.isSuccess()) {
            serverListener.onConnectSuccess();
        } else {
            serverListener.onConnectFailed();
        }
        isInit = true;
    }

    /**
     * 消息推送
     */
    public void push(String content) {
        Iterator iter = ClientSet.onlineUsers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Test.ProtoTest res = Test.ProtoTest.newBuilder()
                    .setId((Integer) key)
                    .setTitle("服务端下发数据")
                    .setContent(content)
                    .build();
            ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) entry.getValue();
            channelHandlerContext.writeAndFlush(res);
        }
    }

    public void shutDown() {
        if (channelFuture != null && channelFuture.isSuccess()) {
            isInit = false;
            channelFuture.channel().closeFuture();
            mWorkerGroup.shutdownGracefully();
        }
    }

}

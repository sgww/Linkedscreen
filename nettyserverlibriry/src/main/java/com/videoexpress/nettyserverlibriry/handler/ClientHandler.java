package com.videoexpress.nettyserverlibriry.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by Vinda on 2018/4/20.
 * Describe 客户端消息读取Handler
 */

public class ClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * 连接服务器成功后执行发送信息
     * 向服务器发送报文
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        // 客户端请求服务器的请求信息  一般包含报文头+报文体
        // 报文头为固定长度
        // 报文体协定用&拼接
        String msgClient = "客户端信息1";

        byte[] bytes = msgClient.getBytes();

        ByteBuf firstMessage = Unpooled.buffer(bytes.length);

        firstMessage.writeBytes(bytes);

        ctx.writeAndFlush(firstMessage);
    }


    /**
     * 服务端返回消息后执行
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //服务端返回消息后
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");

        System.out.println("client1 接收到的信息=" + body);
    }

    /**
     * 异常的场合调用
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();

        ctx.close();
    }

}

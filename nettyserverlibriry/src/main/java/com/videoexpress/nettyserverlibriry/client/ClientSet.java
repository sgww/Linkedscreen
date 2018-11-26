package com.videoexpress.nettyserverlibriry.client;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by vinda on 2018/10/18.
 * Describe:客户端集合
 * qq:1304314404
 */

public class ClientSet {
    //客户端通道集合
    public static Map<Integer, ChannelHandlerContext> onlineUsers = new HashMap<Integer, ChannelHandlerContext>();
}

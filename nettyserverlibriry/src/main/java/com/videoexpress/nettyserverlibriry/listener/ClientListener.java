package com.videoexpress.nettyserverlibriry.listener;

/**
 * Created by user on 2016/10/26.
 * Describe socket 连接状况毁掉
 */

public interface ClientListener {
    void onConnectSuccess();
    void onConnectFailed();
}

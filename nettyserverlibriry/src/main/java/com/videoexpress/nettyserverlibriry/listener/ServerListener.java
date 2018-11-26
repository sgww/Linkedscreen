package com.videoexpress.nettyserverlibriry.listener;

/**
 * Created by user on 2016/10/26.
 * Describe socket 连接状况毁掉
 */

public interface ServerListener {
    void onConnectSuccess();
    void onConnectFailed();
    void clientConnect(int id);
    void clientDisConnect(int id);
}

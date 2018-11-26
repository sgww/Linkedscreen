package orisim3d.vinda.com.lianpingvideodemo.server

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.videoexpress.nettyserverlibriry.server.NettyServer
import com.videoexpress.nettyserverlibriry.listener.ServerListener
import com.videoexpress.nettyserverlibriry.listener.SocketMsgListener
import org.greenrobot.eventbus.EventBus
import orisim3d.vinda.com.lianpingvideodemo.eventmsg.EventMsg

/**
 * Created by vinda on 2018/10/18.
 * Describe:Socket服务端
 * qq:1304314404
 */
class SocketServer : Service(), SocketMsgListener, ServerListener {

    var binder: SocketBinder = SocketBinder()

    inner class SocketBinder : Binder()

    override fun onBind(intent: Intent?): IBinder {
        NettyServer.getInstance(this,this).init()
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }



    override fun onConnectSuccess() {

    }

    override fun onConnectFailed() {

    }


    override fun recieveMsg(msg: String?) {

    }

    /**
     * 当前有客户端连接上来
     */
    override fun clientConnect(id: Int) {
        EventBus.getDefault().post(EventMsg(EventMsg.CLIENT_CONNECT))
    }

    /**
     * 当前有客户端断线
     */
    override fun clientDisConnect(id: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
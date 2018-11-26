package orisim3d.vinda.com.lianpingvideodemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.pili.pldroid.player.AVOptions
import com.pili.pldroid.player.PLOnCompletionListener
import com.pili.pldroid.player.PLOnPreparedListener
import com.pili.pldroid.player.PLOnVideoFrameListener
import com.pili.pldroid.player.widget.PLVideoTextureView
import com.videoexpress.nettyserverlibriry.client.NettyClient
import com.videoexpress.nettyserverlibriry.server.NettyServer
import com.videoexpress.nettyserverlibriry.Test
import com.videoexpress.nettyserverlibriry.client.ClientSet
import com.videoexpress.nettyserverlibriry.listener.ClientListener
import com.videoexpress.nettyserverlibriry.listener.OnReceiveListener
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import orisim3d.vinda.com.lianpingvideodemo.controll.MediaController
import orisim3d.vinda.com.lianpingvideodemo.eventmsg.EventMsg
import orisim3d.vinda.com.lianpingvideodemo.module.PushMsg
import orisim3d.vinda.com.lianpingvideodemo.server.SocketServer
import orisim3d.vinda.com.lianpingvideodemo.util.FileUtil
import orisim3d.vinda.com.lianpingvideodemo.util.NetUtils
import orisim3d.vinda.com.lianpingvideodemo.util.ToastUtil
import java.io.File
import java.net.InetSocketAddress

class MainActivity : AppCompatActivity(), View.OnClickListener {
    var TAG: String = "MainActivity"
    var videoPath: String = "video_left.mp4"
    var mediaPlayController: MediaController? = null
    var playIsOver: Boolean = false
    var serverRegister: Boolean = false
    var eventRegister: Boolean = false
    var gson: Gson = Gson()
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            serverRegister = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serverRegister = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindSocketServer()
        registerEvent()
        if (FileUtil.copyAssetAndWrite(videoPath, this)) {
            init()
        }
        setUI()
    }

    private fun registerEvent() {
        if (!eventRegister) {
            EventBus.getDefault().register(this)
        }
    }

    private fun unRegisterEvent() {
        if (eventRegister) {
            EventBus.getDefault().unregister(this)
        }
    }


    private fun setUI() {
        var ip: String = NetUtils.getIpAdress(this)
        tv_ip.text = "服务器地址: " + ip + ":9001"
        btn_send.setOnClickListener(this)
        btn_connect.setOnClickListener(this)
        btn_send_to_c.setOnClickListener(this)
    }

    /**
     * 启动socket服务
     */
    private fun bindSocketServer() {
        if (!serverRegister) {
            val intent = Intent(this@MainActivity, SocketServer::class.java)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

    }

    private fun init() {
        mediaPlayController = MediaController(this)
//        原始尺寸、适应屏幕、全屏铺满、16:9、4:3
//        pl_videoView.setDisplayAspectRatio(PLVideoTextureView.ASPECT_RATIO_ORIGIN)
//        pl_videoView.setDisplayAspectRatio(PLVideoTextureView.ASPECT_RATIO_FIT_PARENT)
        pl_videoView.setDisplayAspectRatio(PLVideoTextureView.ASPECT_RATIO_PAVED_PARENT)
//        pl_videoView.setDisplayAspectRatio(PLVideoTextureView.ASPECT_RATIO_16_9)
//        pl_videoView.setDisplayAspectRatio(PLVideoTextureView.ASPECT_RATIO_4_3)

        pl_videoView.setMediaController(mediaPlayController)

        pl_videoView.setOnPreparedListener(object : PLOnPreparedListener {
            override fun onPrepared(p0: Int) {
                Log.d(TAG, "播放准备完毕")
            }

        })

        pl_videoView.setOnCompletionListener(object : PLOnCompletionListener {
            override fun onCompletion() {
                Log.d(TAG, "播放完成")
                playIsOver = true
            }

        })

        pl_videoView.setOnVideoFrameListener(object : PLOnVideoFrameListener {
            /**
             * 回调一帧视频帧数据
             *
             * @param data   视频帧数据
             * @param size   数据大小
             * @param width  视频帧的宽
             * @param height 视频帧的高
             * @param format 视频帧的格式，0代表 YUV420P，1 代表 JPEG， 2 代表 SEI
             * @param ts     时间戳，单位是毫秒
             */
            override fun onVideoFrameAvailable(data: ByteArray?, size: Int, width: Int, height: Int, format: Int, ts: Long) {
                Log.d(TAG, "数据大小：" + size + " 帧宽: " + width + " 帧高： " + height + " 格式： " + format + " 时间戳 ： " + ts)
                MainApplication.videofram = ts
                MainApplication.runUITask{

                    tv_recieve_info.text = "本地帧：" + ts
                    //服务端将时间戳发送给客户端
//                    if (!ts.equals(0)) {
//                        val eventMsg = EventMsg(EventMsg.SEEK_VIDEO)
//                        eventMsg.bundle = ts
//                        EventBus.getDefault().post(eventMsg)
//                    }
                }

            }


        })
        var option: AVOptions = AVOptions()
        // 设置回调帧数
        option.setInteger(AVOptions.KEY_VIDEO_DATA_CALLBACK, 1)
        // 设置拖动模式，1 位精准模式，即会拖动到时间戳的那一秒；0 为普通模式，会拖动到时间戳最近的关键帧。默认为 0
        option.setInteger(AVOptions.KEY_SEEK_MODE, 0)
        // 设置开始播放位置 ，默认不开启，单位为 ms
//        option.setInteger(AVOptions.KEY_START_POSITION, 10 * 1000)

        // 解码方式:
        // codec＝AVOptions.MEDIA_CODEC_HW_DECODE，硬解
        // codec=AVOptions.MEDIA_CODEC_SW_DECODE, 软解
//        var codec = AVOptions.MEDIA_CODEC_AUTO// 硬解优先，失败后自动切换到软解
        // 默认值是：MEDIA_CODEC_SW_DECODE
//        option.setInteger(AVOptions.KEY_MEDIACODEC, codec)
        pl_videoView.setAVOptions(option)

        var file: File = File(cacheDir, videoPath)
        pl_videoView.setVideoPath(file.absolutePath)
        pl_videoView.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindSocketServer()
        unRegisterEvent()
    }

    private fun unbindSocketServer() {
        if (serverRegister)
            unbindService(connection)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_send -> {
                clientSendMsg()
            }
            R.id.btn_connect -> {
                connectServer()
            }
            R.id.btn_send_to_c -> {
                serverSengMsg(1000)
            }
        }
    }

    private fun serverSengMsg(newposition: Long) {
        Log.d(TAG, "服务端推送消息：" + newposition)
        var pushMsg: PushMsg = PushMsg()
        pushMsg.framets = newposition
        val bodyRecieve = gson.toJson(pushMsg)
        NettyServer.getInstance().push(bodyRecieve)
    }


    private fun clientSendMsg() {
        var proto: Test.ProtoTest = Test.ProtoTest.newBuilder()
                .setId(1)
                .setTitle("title")
                .setContent("消息内容")
                .build()
        NettyClient.getInstance().send(proto, object : OnReceiveListener {
            override fun handleReceive(msg: Any) {
                if (msg is String) {
                    var recieveMsg = gson.fromJson(msg, PushMsg::class.java)
                    MainApplication.runUITask {
                        Log.d(TAG, "收到服务端消息：" + recieveMsg.framets)
                        tv_serverfram.text ="服务器端播放第" + recieveMsg.framets + "帧"
                        if (recieveMsg.framets > MainApplication.videofram) {
                            if ((recieveMsg.framets - MainApplication.videofram) > 1500) {//误差超过60ms
                                Log.d(TAG, "误差大于100ms，纠正")
                                if (!pl_videoView.isPlaying) {
                                    pl_videoView.start()
                                }
                                ToastUtil.showShortToast("同步纠正")
                                //调整
                                mediaPlayController!!.getMplayer().seekTo(recieveMsg.framets)
                            }
                        }

                        if (recieveMsg.framets < MainApplication.videofram) {
                            if ((MainApplication.videofram - recieveMsg.framets) > 1500) {//误差超过60ms
                                Log.d(TAG, "误差大于100ms，纠正")
                                if (!pl_videoView.isPlaying) {
                                    pl_videoView.start()
                                }
                                ToastUtil.showShortToast("同步纠正")
                                //调整
                                mediaPlayController!!.getMplayer().seekTo(recieveMsg.framets)
                            }
                        }
                    }
                }
            }
        })
    }

    /**
     * 连接socket服务器
     */
    private fun connectServer() {
        Thread(Runnable {
            NettyClient.getInstance()
                    .connect(InetSocketAddress(server_IP.text.toString(), NettyServer.PORT_NUMBER), object : ClientListener {

                        override fun onConnectSuccess() {
                            Handler(Looper.getMainLooper()).post { Toast.makeText(this@MainActivity, "connect success!", Toast.LENGTH_SHORT).show() }

                        }

                        override fun onConnectFailed() {
                            Handler(Looper.getMainLooper()).post { Toast.makeText(this@MainActivity, "connect failed!", Toast.LENGTH_SHORT).show() }

                        }
                    })
        }).start()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventMsg(eventMsg: EventMsg) {
        when (eventMsg.type) {
            EventMsg.CLIENT_CONNECT -> tv_client_info.text = "当前有 " + ClientSet.onlineUsers.size + " 个客户端连接"
            EventMsg.SEEK_VIDEO -> {
                var newPOsitiong: Long = eventMsg.bundle as Long
                serverSengMsg(newPOsitiong)
            }
        }

    }


}

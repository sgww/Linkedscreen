package orisim3d.vinda.com.lianpingvideodemo.eventmsg;

/**
 * Created by vinda on 2018/7/12.
 * Describe:
 * qq:1304314404
 */

public class EventMsg {
    public static final int CLIENT_CONNECT = 1;//客户端连接
    public static final int CLIENT_DISCONNECT = 2;//客户端数量发生变化
    public static final int SEEK_VIDEO = 3;//拖拽视频
    private int type;
    private Object bundle;

    public EventMsg(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getBundle() {
        return bundle;
    }

    public void setBundle(Object bundle) {
        this.bundle = bundle;
    }
}

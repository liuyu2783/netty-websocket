package fun.liuyu2783.websocket.common;

/**
 * 系统常量配置
 *
 * @author liuyu
 * @date 2019-11-25
 */
public class CmcConstants {
    /**
     * 广播事件
     */
    public static final String BROADCAST_EVENT = "cmcBroadcast";
    /**
     * 广播事件room
     */
    public static final String BROADCAST_ROOM = "all";
    /**
     * hello事件
     */
    public static final String HELLO_EVENT = "hello";
    /**
     * room前缀
     */
    public static final String KEY_ROOM_PREFIX = "cmc:rooms:";
    /**
     * 发送失败的事件信息前缀
     */
    public static final String KEY_FAIL_EVENT_PREFIX = "cmc:fail:event:";
}

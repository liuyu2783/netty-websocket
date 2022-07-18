package fun.liuyu2783.websocket.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import fun.liuyu2783.websocket.common.CmcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 消息事件处理器
 */
@Component
public class MessageEventHandler {

    @Autowired
    private SocketIOServer server;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(MessageEventHandler.class);

    //添加connect事件，当客户端发起连接时调用
    @OnConnect
    public void onConnect(SocketIOClient client) {
        if (client != null) {
            String room = client.getHandshakeData().getSingleUrlParam("room");
            String token = client.getHandshakeData().getSingleUrlParam("token");
            String sessionId = client.getSessionId().toString();
            if(!StringUtils.isEmpty(room)) {
                if(!client.getAllRooms().contains(room)) {
                    client.joinRoom(room);
                }
            }
            logger.info("socket连接成功, room={}, token={}, sessionId={}",room,token,sessionId);
            stringRedisTemplate.opsForHash().put(CmcConstants.KEY_ROOM_PREFIX +room,"sessionId",sessionId);
            stringRedisTemplate.opsForHash().put(CmcConstants.KEY_ROOM_PREFIX +room,"connectTime", new Date());
            stringRedisTemplate.expire(CmcConstants.KEY_ROOM_PREFIX +room,365,TimeUnit.DAYS);
        } else {
            logger.error("客户端建立连接失败: SocketIOClient为空");
        }
    }

    //添加@OnDisconnect事件，客户端断开连接时调用，刷新客户端信息
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        //断开连接
        client.disconnect();
        //redis删除
        String sessionId = client.getSessionId().toString();
        Set<String> keys = stringRedisTemplate.keys(CmcConstants.KEY_ROOM_PREFIX +"*");
        String room = "";
        Map value = new HashMap();
        for(String key: keys){
            value = stringRedisTemplate.opsForHash().entries(key);
            if(value.get("sessionId").equals(sessionId)){
                room = key;
                break;
            }
        }
        stringRedisTemplate.delete(room);
        logger.warn("客户端断开连接, room={},value={}", room, value);
    }

    //连接验证
    @OnEvent(value = CmcConstants.HELLO_EVENT)
    public void onHelloEvent(SocketIOClient client, AckRequest ackRequest, String message) {
        logger.info("hello事件, sessionId={}, message={}",client.getSessionId().toString(),message);
        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData("您好, netty连接已建立.");
        }
    }
}

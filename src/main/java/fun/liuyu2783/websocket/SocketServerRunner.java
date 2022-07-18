package fun.liuyu2783.websocket;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.BroadcastAckCallback;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubListener;
import com.corundumstudio.socketio.store.pubsub.PubSubStore;
import com.corundumstudio.socketio.store.pubsub.PubSubType;
import fun.liuyu2783.websocket.common.CmcConstants;
import fun.liuyu2783.websocket.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * SocketIOServer启动器
 * @author liuyu
 */
@Component
@Order(1)
public class SocketServerRunner implements CommandLineRunner {

    private final SocketIOServer socketIOServer;

    @Autowired
    private PubSubStore pubSubStore;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${cmc.broadcast.times}")
    private int broadcastTimes;

    private static final Logger logger = LoggerFactory.getLogger(SocketServerRunner.class);

    @Autowired
    public SocketServerRunner(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;
    }

    @Override
    public void run(String... args) {
        socketIOServer.start();
        //订阅redis队列
        logger.warn("CMC订阅redis队列");
        pubSubStore.subscribe(PubSubType.DISPATCH, new PubSubListener<DispatchMessage>() {
            @Override
            public void onMessage(DispatchMessage msg) {
                String room = msg.getRoom();
                Packet packet = msg.getPacket();
                logger.info("收到订阅消息：DispatchMessage={}", JSON.toJSONString(msg));
                String str = packet.getData();
                ChatMessage chatMessage = JSON.parseObject(str, ChatMessage.class);
                int times = chatMessage.getBroadcastTimes();
                if(times > broadcastTimes){
                    String key = CmcConstants.KEY_FAIL_EVENT_PREFIX + chatMessage.getRoom();
                    stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(chatMessage));
                    logger.warn("消息广播次数超过{}次，放入redis待处理。key={},message={}",broadcastTimes,key,chatMessage);
                }else {
                    chatMessage.setBroadcastTimes(times+1);
                    packet.setData(JSON.toJSONString(chatMessage));
                    if(room.equals(CmcConstants.BROADCAST_ROOM)){
                        Collection<SocketIOClient> clients = socketIOServer.getBroadcastOperations().getClients();
                        for(final SocketIOClient client : clients){
                            client.sendEvent(CmcConstants.BROADCAST_EVENT, JSON.toJSON(packet.getData()));
                        }
                        logger.info("推送广播消息：data={}", packet.getData().toString());
                    }else{
                        Collection<SocketIOClient> cs = socketIOServer.getRoomOperations(room).getClients();
                        if(!cs.isEmpty()) {
                            socketIOServer.getRoomOperations(room).sendEvent(packet.getName(), JSON.toJSON(packet.getData()),new BroadcastAckCallback<String>(String.class){
                                @Override
                                public void onClientSuccess(SocketIOClient client, String result) {
                                    logger.info("客户端，返回值={}", result);
                                }
                            });
                            logger.info("推送订阅消息：data={}", packet.getData().toString());
                        }else{
                            logger.info("推送订阅消息：client不在当前节点。room={}",room);
                        }
                    }
                }

            }
        }, DispatchMessage.class);
        logger.warn("CMC(Cloud Message Center)系统启动...");
    }

}
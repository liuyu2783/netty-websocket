package fun.liuyu2783.websocket.controller;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.protocol.PacketType;
import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubStore;
import com.corundumstudio.socketio.store.pubsub.PubSubType;
import fun.liuyu2783.websocket.common.CmcConstants;
import fun.liuyu2783.websocket.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;

/**
 * 消息管理器
 *
 * @author liuyu
 */
@RestController
@RequestMapping("cmc/message")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private SocketIOServer socketIOServer;

    @Autowired
    private PubSubStore pubSubStore;

    /**
     * 发送消息
     * @param chatMessage
     * @return
     */
    @RequestMapping("send")
    public HashMap sendMessage(@RequestBody ChatMessage chatMessage){
        logger.info("发送消息参数={}", chatMessage);
        String room = chatMessage.getRoom();
        String eventName = chatMessage.getEventName();
        Collection<SocketIOClient> cs = socketIOServer.getRoomOperations(room).getClients();
        if(!cs.isEmpty()) {
            socketIOServer.getRoomOperations(room).sendEvent(eventName, JSON.toJSONString(chatMessage));
            logger.info("向客户端推送消息: message={}", chatMessage);
        } else {
            Packet packet = new Packet(PacketType.MESSAGE);
            packet.setSubType(PacketType.EVENT);
            packet.setName(eventName);
            packet.setData(JSON.toJSONString(chatMessage));
            packet.setNsp("");
            pubSubStore.publish(PubSubType.DISPATCH, new DispatchMessage(room, packet, ""));
            logger.info("发送消息到队列: message={}", chatMessage);
        }
        HashMap hashMap = new HashMap();
        hashMap.put("result","成功");
        return hashMap;
    }

    /**
     * 广播消息
     * @param chatMessage
     * @return
     */
    @RequestMapping("broadcast")
    public HashMap broadcast(@RequestBody ChatMessage chatMessage){
        logger.info("广播消息参数={}", chatMessage);
        String room = chatMessage.getRoom();
        String eventName = chatMessage.getEventName();
        String message = chatMessage.getMessage();
        Collection<SocketIOClient> clients = socketIOServer.getBroadcastOperations().getClients();
        for(final SocketIOClient client : clients){
            client.sendEvent(CmcConstants.BROADCAST_EVENT, message);
            logger.info("向客户端推送消息, message={}",message);
        }
        Packet packet = new Packet(PacketType.MESSAGE);
        packet.setSubType(PacketType.EVENT);
        packet.setName(CmcConstants.BROADCAST_EVENT);
        packet.setData(message);
        packet.setNsp("");
        pubSubStore.publish(PubSubType.DISPATCH, new DispatchMessage(CmcConstants.BROADCAST_ROOM, packet, ""));
        logger.info("发送消息到队列: message={}", message);
        HashMap hashMap = new HashMap();
        hashMap.put("result","成功");
        return hashMap;
    }
}

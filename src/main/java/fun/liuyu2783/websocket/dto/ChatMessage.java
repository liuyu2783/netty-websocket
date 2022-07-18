package fun.liuyu2783.websocket.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 通信消息实体
 * @author liuyu
 */
@Data
@ToString
public class ChatMessage implements Serializable {

    private String room;

    private String message;

    private String eventName;

    private int broadcastTimes;
}

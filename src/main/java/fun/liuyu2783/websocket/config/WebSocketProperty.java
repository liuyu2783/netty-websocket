package fun.liuyu2783.websocket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * websocket配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "cmc.socket")
public class WebSocketProperty {
    /**
     * socket端口
     */
    private Integer socketPort;
    /**
     * Ping消息间隔（毫秒）
     */
    private Integer pingInterval;
    /**
     * Ping消息超时时间（毫秒）
     */
    private Integer pingTimeout;
    /**
     * HTTP握手升级为ws协议超时时间(毫秒)
     */
    private Integer upgradeTimeout;


}
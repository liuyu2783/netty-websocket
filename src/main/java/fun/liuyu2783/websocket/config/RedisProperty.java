package fun.liuyu2783.websocket.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Redission配置
 * @author liuyu07
 */
@Data
@Component
public class RedisProperty {

    @Value("${spring.redis.sentinel.master}")
    private String masterName;
    @Value("${spring.redis.database}")
    private Integer database;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.sentinel.nodes}")
    private String sentinelAddressList;

}

package fun.liuyu2783.websocket.config;

import com.corundumstudio.socketio.AckMode;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import com.corundumstudio.socketio.store.pubsub.PubSubStore;
import fun.liuyu2783.websocket.common.TokenUtil;
import fun.liuyu2783.websocket.handler.NettyExceptionListener;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * NettySocketConfig
 */
@Configuration
public class NettySocketConfig {
    @Resource
    private WebSocketProperty webSocketProperty;

    @Resource
    private RedisProperty redisProperty;

    @Resource
    private NettyExceptionListener nettyExceptionListener;

    private static final Logger logger = LoggerFactory.getLogger(NettySocketConfig.class);

    @Bean
    public SocketIOServer socketIOServer() {
        /*
         * 创建Socket，并设置监听端口
         */
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        // 设置主机名，默认是0.0.0.0
        // config.setHostname("localhost");
        // 设置监听端口
        config.setPort(webSocketProperty.getSocketPort());
        // 协议升级超时时间（毫秒），默认10000。HTTP握手升级为ws协议超时时间
        config.setUpgradeTimeout(webSocketProperty.getUpgradeTimeout());
        // Ping消息间隔（毫秒），默认25000。客户端向服务器发送一条心跳消息间隔
        config.setPingInterval(webSocketProperty.getPingInterval());
        // Ping消息超时时间（毫秒），默认60000，这个时间间隔内没有接收到心跳消息就会发送超时事件
        config.setPingTimeout(webSocketProperty.getPingTimeout());
        // 基于redisson
        config.setStoreFactory(createRedissonStoreFactory());
        //异常处理
        config.setExceptionListener(nettyExceptionListener);

        //手动确认
        config.setAckMode(AckMode.MANUAL);
        // 握手协议参数使用JWT的Token认证方案
        config.setAuthorizationListener(data -> {
            // 可以使用如下代码获取用户密码信息
            String token = data.getSingleUrlParam("token");
            String room = data.getSingleUrlParam("room");
            logger.info("socket认证参数: token={}, room={}",token, room);
            if(StringUtils.isEmpty(token) || StringUtils.isEmpty(room)){
                logger.error("socket认证失败, 参数不符合要求: token={}, room={}",token, room);
                return false;
            }
            if(!TokenUtil.getToken(room).equals(token)){
                logger.error("socket认证失败, 权限校验失败: token={}",token);
                return false;
            }
            return true;
        });
        return new SocketIOServer(config);
    }

    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {
        return new SpringAnnotationScanner(socketServer);
    }

    @Bean
    public PubSubStore pubSubStore() {
        return socketIOServer().getConfiguration().getStoreFactory().pubSubStore();
    }

    @Bean
    public RedissonClient redissonClient(){
        Config redissonConfig = new Config();
        redissonConfig.useSentinelServers().
                setDatabase(redisProperty.getDatabase()).
                setMasterName(redisProperty.getMasterName()).
                setPassword(redisProperty.getPassword());
        String[] address = redisProperty.getSentinelAddressList().split(",");
        Arrays.stream(address).forEach((add)-> redissonConfig.useSentinelServers().addSentinelAddress("redis://"+add));
        RedissonClient redisson = Redisson.create(redissonConfig);
        return  redisson;
    }

    private RedissonStoreFactory createRedissonStoreFactory(){
        RedissonStoreFactory redissonStoreFactory = new RedissonStoreFactory(redissonClient());
        return redissonStoreFactory;
    }
}
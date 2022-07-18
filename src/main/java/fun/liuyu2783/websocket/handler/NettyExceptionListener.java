package fun.liuyu2783.websocket.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ExceptionListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * netty异常处理器
 * @author liuyu
 */
@Component
public class NettyExceptionListener implements ExceptionListener {

    private static final Logger logger = LoggerFactory.getLogger(NettyExceptionListener.class);

    @Override
    public void onEventException(Exception e, List<Object> args, SocketIOClient client) {
        logger.error("websocket事件异常, {}",e.getMessage(), e);
    }

    @Override
    public void onDisconnectException(Exception e, SocketIOClient client) {
        logger.error("websocket断开事件异常, {}",e.getMessage(), e);
    }

    @Override
    public void onConnectException(Exception e, SocketIOClient client) {
        logger.error("websocket连接异常, {}", e.getMessage(), e);
    }

    @Override
    public boolean exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        logger.error("websocket连接异常, {}", e.getMessage(), e);
        return false;
    }

    @Override
    public void onPingException(Exception e, SocketIOClient client) {
        logger.error("websocket心跳异常, {}", e.getMessage(), e);
    }
}

package net.anumbrella.rabbitmq.config;

import net.anumbrella.rabbitmq.entity.MyPrincipal;
import net.anumbrella.rabbitmq.util.SocketSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Map;

/**
 * @author Anumbrella
 */
@Component
public class MyChannelInterceptor extends ChannelInterceptorAdapter {

    @Autowired
    private SocketSessionRegistry webAgentSessionRegistry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            System.out.println("连接success");
            Object raw = message.getHeaders().get(SimpMessageHeaderAccessor.NATIVE_HEADERS);
            if (raw instanceof Map) {
                Object name = ((Map) raw).get("name");
                if (name instanceof LinkedList) {
                    String id = ((LinkedList) name).get(0).toString();

                    //设置当前访问器的认证用户
                    accessor.setUser(new MyPrincipal(id));

                    String sessionId = accessor.getSessionId();

                    // 统计用户在线数，可通过redis来实现更好
                    webAgentSessionRegistry.registerSessionId(id, sessionId);

                }
            }
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            //点击断开连接，这里会执行两次，第二次执行的时候，message.getHeaders.size()=5,第一次是6。直接关闭浏览器，只会执行一次，size是5。
            System.out.println("断开连接");
            MyPrincipal principal = (MyPrincipal) message.getHeaders().get(SimpMessageHeaderAccessor.USER_HEADER);
            //  如果同时发生两个连接，只有都断开才能叫做不在线
            if (message.getHeaders().size() == 5 && principal.getName() != null) {
                String sessionId = accessor.getSessionId();
                webAgentSessionRegistry.unregisterSessionId(principal.getName(), sessionId);
            }
        }
        return message;
    }

}

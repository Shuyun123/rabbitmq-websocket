package net.anumbrella.rabbitmq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * @author Anumbrella
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketConfig.class);


    @Autowired
    private MyChannelInterceptor inboundChannelInterceptor;

    @Autowired
    private AuthHandshakeInterceptor authHandshakeInterceptor;

    @Autowired
    private MyHandshakeHandler myHandshakeHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("172.16.66.18")      // rabbitmq-host服务器地址
                .setRelayPort(61613)     // rabbitmq-stomp 服务器服务端口
                .setClientLogin("guest")   // 登陆账户
                .setClientPasscode("guest"); // 登陆密码
        //定义一对一推送的时候前缀
        registry.setUserDestinationPrefix("/user/");
        //客户端需要把消息发送到/message/xxx地址
        registry.setApplicationDestinationPrefixes("/message");
        LOGGER.info("init rabbitmq websocket MessageBroker complated.");
    }

    /**
     * 连接站点配置
     *
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*")
                .setHandshakeHandler(myHandshakeHandler)
                .addInterceptors(authHandshakeInterceptor)
                .withSockJS();
        LOGGER.info("init rabbitmq websocket endpoint ");
    }


    /**
     * 输入通道配置
     *
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(inboundChannelInterceptor);
        registration.taskExecutor()    // 线程信息
                .corePoolSize(400)     // 核心线程池
                .maxPoolSize(800)      // 最多线程池数
                .keepAliveSeconds(60); // 超过核心线程数后，空闲线程超时60秒则杀死
    }

    /**
     * 消息传输参数配置
     *
     * @param registration
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setSendTimeLimit(15 * 1000)    // 超时时间
                .setSendBufferSizeLimit(512 * 1024) // 缓存空间
                .setMessageSizeLimit(128 * 1024);   // 消息大小
    }


}

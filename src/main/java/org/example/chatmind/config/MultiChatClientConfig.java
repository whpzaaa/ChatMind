package org.example.chatmind.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultiChatClientConfig {
    @Bean("deepseek-chat")
    public ChatClient deepseekChatClient(DeepSeekChatModel deepSeekChatModel) {
        return ChatClient.create(deepSeekChatModel);
    }

    @Bean("glm-4.6")
    public ChatClient zhiPuAiChat(ZhiPuAiChatModel zhiPuAiChatModel){
        return ChatClient.create(zhiPuAiChatModel);
    }
}

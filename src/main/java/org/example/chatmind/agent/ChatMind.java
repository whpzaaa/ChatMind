package org.example.chatmind.agent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chatmind.model.common.SseMessage;
import org.example.chatmind.model.dto.ChatMessageDTO;
import org.example.chatmind.model.dto.KnowledgeBaseDTO;
import org.example.chatmind.model.entity.ChatMessage;
import org.example.chatmind.model.vo.ChatMessageVO;
import org.example.chatmind.service.ChatMessageService;
import org.example.chatmind.service.SseService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.DefaultChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
public class ChatMind {
    private String agentId;
    private String name;
    private String description;
    private String systemPrompt;
    private ChatClient chatClient;
    private AgentState agentState;
    private List<ToolCallback> availableTools;
    private List<KnowledgeBaseDTO> availableKnowledgeBases;
    private ToolCallingManager toolCallingManager;
    private ChatMemory chatMemory;
    private String chatSessionId;
    private static final Integer MAX_STEPS = 20;
    private static final Integer DEFAULT_MAX_MESSAGES = 20;
    private ChatOptions chatOptions;
    private SseService sseService;
    private ChatMessageService chatMessageService;
    private final List<ChatMessageDTO> pendingChatMessage = new ArrayList<>();
    private ChatResponse lastResponse;
    public ChatMind(String agentId,
                    String name,
                    String description,
                    String systemPrompt,
                    ChatClient chatClient,
                    Integer maxMessages,
                    List<Message> memory,
                    List<ToolCallback> availableTools,
                    List<KnowledgeBaseDTO> availableKnowledgeBases,
                    String chatSessionId,
                    SseService sseService,
                    ChatMessageService chatMessageService) {
        this.agentId = agentId;
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.chatClient = chatClient;
        this.availableTools = availableTools;
        this.availableKnowledgeBases = availableKnowledgeBases;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatMemory = MessageWindowChatMemory.builder().maxMessages(maxMessages == null ? DEFAULT_MAX_MESSAGES : maxMessages).build();
        this.chatMemory.add(chatSessionId,memory);
        if(StringUtils.hasLength(systemPrompt)){
            this.chatMemory.add(chatSessionId, new SystemMessage(systemPrompt));
        }
        this.chatOptions = DefaultToolCallingChatOptions.builder().internalToolExecutionEnabled(false).build();
        this.chatSessionId = chatSessionId;
        this.sseService = sseService;
        this.chatMessageService = chatMessageService;
        this.agentState = AgentState.IDLE;
        log.info("ChatMind init success");
    }

    //保存消息(存入pending集合中)并持久化
    public void saveMessage(Message message){
        if(message instanceof AssistantMessage assistantMessage){
            ChatMessageDTO chatMessageDTO = ChatMessageDTO.builder()
                    .sessionId(chatSessionId)
                    .role(ChatMessageDTO.RoleType.ASSISTANT)
                    .content(assistantMessage.getText())
                    .metadata(ChatMessageDTO.MetaData.builder().toolCalls(assistantMessage.getToolCalls()).build())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            String messageId = chatMessageService.create(chatMessageDTO);
            chatMessageDTO.setId(messageId);
            pendingChatMessage.add(chatMessageDTO);
        } else if (message instanceof ToolResponseMessage toolResponseMessage) {
            for (ToolResponseMessage.ToolResponse toolResponse : toolResponseMessage.getResponses()) {
                ChatMessageDTO chatMessageDTO = ChatMessageDTO.builder()
                        .sessionId(chatSessionId)
                        .role(ChatMessageDTO.RoleType.TOOL)
                        .content(toolResponse.responseData())
                        .metadata(ChatMessageDTO.MetaData.builder().toolResponse(toolResponse).build())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                String messageId = chatMessageService.create(chatMessageDTO);
                chatMessageDTO.setId(messageId);
                pendingChatMessage.add(chatMessageDTO);
            }
        }else {
            throw new IllegalArgumentException("不支持的message类型");
        }
    }

    //刷新pendingmessage，并通过sse发送给前端
    public void refreshPendingMessage(){
        for (ChatMessageDTO chatMessageDTO : pendingChatMessage) {
            ChatMessageVO chatMessageVO = ChatMessageVO.builder()
                    .id(chatMessageDTO.getId())
                    .sessionId(chatMessageDTO.getSessionId())
                    .role(chatMessageDTO.getRole())
                    .content(chatMessageDTO.getContent())
                    .metadata(chatMessageDTO.getMetadata())
                    .build();
            sseService.sendMessage(chatSessionId, SseMessage.builder()
                    .type(SseMessage.Type.AI_GENERATED_CONTENT)
                    .metadata(SseMessage.Metadata.builder()
                            .chatMessageId(chatMessageDTO.getId())
                            .build())
                    .payload(SseMessage.Payload.builder()
                            .message(chatMessageVO)
                            .build())
                    .build());
        }
        pendingChatMessage.clear();
    }

    private boolean think(){
        String thinkPrompt =  """
                现在你是一个智能的的具体「决策模块」
                请根据当前对话上下文，决定下一步的动作。
                                \s
                【额外信息】
                - 你目前拥有的知识库列表以及描述：%s
                - 如果有缺失的上下文时，优先从知识库中进行搜索
                """.formatted(this.availableKnowledgeBases);

        Prompt prompt  = Prompt.builder()
                .chatOptions(this.chatOptions)
                .messages(this.chatMemory.get(this.chatSessionId))
                .build();

        this.lastResponse= this.chatClient.prompt(prompt)
                .system(thinkPrompt)
                .toolCallbacks(this.availableTools.toArray(new ToolCallback[0]))
                .call()
                .chatClientResponse()
                .chatResponse();
        Assert.notNull(this.lastResponse, "chatResponse is null");
        AssistantMessage output = this.lastResponse.getResult().getOutput();

        saveMessage(output);
        refreshPendingMessage();

        List<AssistantMessage.ToolCall> toolCalls = output.getToolCalls();
        logToolCalls(toolCalls);

        return !toolCalls.isEmpty();
    }

    public void execute(){
        Prompt prompt = Prompt.builder()
                .chatOptions(this.chatOptions)
                .messages(this.chatMemory.get(this.chatSessionId))
                .build();

        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, this.lastResponse);

        this.chatMemory.clear(this.chatSessionId);
        this.chatMemory.add(this.chatSessionId, toolExecutionResult.conversationHistory());

        ToolResponseMessage toolResponse = (ToolResponseMessage)toolExecutionResult.conversationHistory().get(toolExecutionResult.conversationHistory().size() - 1);

        String collect = toolResponse.getResponses()
                .stream()
                .map(toolResponseMessage -> "工具" + toolResponseMessage.name() + "的返回结果为：" + toolResponseMessage.responseData())
                .collect(Collectors.joining("\n"));

        log.info("工具执行结果：{}", collect);

        saveMessage(toolResponse);
        refreshPendingMessage();

        if(toolResponse.getResponses()
                .stream()
                .anyMatch(toolResponseMessage -> toolResponseMessage.name().equals("terminate"))){
            this.agentState = AgentState.FINISHED;
            log.info("任务结束");
        }
    }

    private void step(){
        if(think()){
            execute();
        }else{
            this.agentState = AgentState.FINISHED;
            log.info("任务结束");
        }
    }

    public void run(){
        if(this.agentState != AgentState.IDLE){
            throw new IllegalArgumentException("agent is not idle");
        }
        try {
            for(int i = 0;i < MAX_STEPS && this.agentState != AgentState.FINISHED;i++){
                step();
                if(i == MAX_STEPS - 1){
                    agentState = AgentState.FINISHED;
                    log.warn("Max steps reached, stopping agent");
                }
            }
            agentState = AgentState.FINISHED;
            log.info("任务结束");
        } catch (Exception e) {
            agentState = AgentState.ERROR;
            log.error("任务出错", e);
            throw new RuntimeException("Error running agent",e);
        }
    }

    //打印工具调用信息
    private void logToolCalls(List<AssistantMessage.ToolCall> toolCalls){
        if(toolCalls == null || toolCalls.isEmpty()){
            log.info("无工具调用");
            return;
        }
        log.info("==================工具调用===============================");
        for(int i = 0;i < toolCalls.size();i++){
            log.info("第{}个工具调用：", i + 1);
            log.info("name : {}", toolCalls.get(i).name());
            log.info("arguments : {}", toolCalls.get(i).arguments());
        }
        log.info("=========================================================");
    }
}

package org.example.chatmind.model.response;

import lombok.Builder;
import lombok.Data;
import org.example.chatmind.model.vo.AgentVO;

@Data
@Builder
public class GetAgentsResponse {
    private AgentVO[] agents;
}

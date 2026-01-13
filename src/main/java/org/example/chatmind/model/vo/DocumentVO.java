package org.example.chatmind.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentVO {
    private String id;
    private String kbId;
    private String filename;
    private String filetype;
    private Long size;
}


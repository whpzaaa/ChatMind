package org.example.chatmind.service;

import java.util.List;

public interface RagService {
    float[] embed(String text);

    List<String> search(String kbId,String title);
}

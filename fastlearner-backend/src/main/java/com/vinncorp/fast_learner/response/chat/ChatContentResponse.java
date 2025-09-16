package com.vinncorp.fast_learner.response.chat;

import com.vinncorp.fast_learner.dtos.chat.ChatTopic;
import com.vinncorp.fast_learner.dtos.chat.ChatTopicHistory;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatContentResponse {
    private Long sectionId;
    private Long sequenceNumber;
    private String sectionName;
    private List<ChatTopic> topics;

    public static List<ChatContentResponse> convertToChatContentResponse(List<Tuple> tuples) {
        List<ChatContentResponse> chatContentResponses = new ArrayList<>();

        for (Tuple tuple : tuples) {
            Long sectionId = ((Number) tuple.get("section_id")).longValue();
            String sectionName = (String) tuple.get("section_name");
            Long sequenceNumber = ((Number) tuple.get("sequence_number")).longValue();

            Long topicId = ((Number) tuple.get("topic_id")).longValue();
            String topicName = (String) tuple.get("name");
            Long topicSeq = ((Number) tuple.get("topic_seq")).longValue();
            Long videoId = ((Number) tuple.get("video_id")).longValue();
            Long chatId = ((Number) tuple.get("chat_id")).longValue();
            String time = (String) tuple.get("times");
            String title = (String) tuple.get("title");


            // Check if the section exists, if not create a new ChatContentResponse
            ChatContentResponse chatContentResponse = chatContentResponses.stream()
                    .filter(response -> response.getSectionId().equals(sectionId))
                    .findFirst()
                    .orElseGet(() -> {
                        ChatContentResponse newResponse = ChatContentResponse.builder()
                                .sectionId(sectionId)
                                .sectionName(sectionName)
                                .sequenceNumber(sequenceNumber)
                                .topics(new ArrayList<>())
                                .build();
                        chatContentResponses.add(newResponse);
                        return newResponse;
                    });

            // Check if the topic exists, if not create a new ChatTopic
            ChatTopic chatTopic = chatContentResponse.getTopics().stream()
                    .filter(topic -> topic.getTopicId().equals(topicId))
                    .findFirst()
                    .orElseGet(() -> {
                        ChatTopic newTopic = new ChatTopic();
                        newTopic.setTopicId(topicId);
                        newTopic.setSequence(topicSeq);
                        newTopic.setTopicName(topicName);
                        newTopic.setVideoId(videoId);
                        newTopic.setChatTopicHistory(new ArrayList<>());
                        chatContentResponse.getTopics().add(newTopic);
                        return newTopic;
                    });

            // Create ChatTopicHistory and add it to the corresponding ChatTopic
            ChatTopicHistory chatTopicHistory = new ChatTopicHistory();
            chatTopicHistory.setChatId(chatId);
            chatTopicHistory.setTime(time);
            chatTopicHistory.setTitle(title);
            chatTopic.getChatTopicHistory().add(chatTopicHistory);
        }

        return chatContentResponses;
    }
}

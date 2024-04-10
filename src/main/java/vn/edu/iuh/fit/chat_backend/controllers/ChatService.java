package vn.edu.iuh.fit.chat_backend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vn.edu.iuh.fit.chat_backend.models.*;
import vn.edu.iuh.fit.chat_backend.services.MessageService;
import vn.edu.iuh.fit.chat_backend.services.UserService;
import vn.edu.iuh.fit.chat_backend.types.MessageType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller
public class ChatService {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

    @MessageMapping("/react-message")
    public String reactMessage(@Payload MessageText messageText, @Payload MessageFile messageFile) {
        if (messageText.getContent() == null) {
            System.out.println(messageFile.getReact());
            simpMessagingTemplate.convertAndSendToUser(messageFile.getId(), "react-message", messageFile.getReact());
        } else {
            System.out.println(messageText.getReact());
        }
        return "";
    }

    @MessageMapping("/retrieve-message")
    public String retrieveMessage(@Payload MessageText messageText, @Payload MessageFile messageFile) {
        if (messageText.getContent() == null) {
            System.out.println(messageFile);
            Message messageNew = messageService.retrieveMessageSingle(messageFile);
            simpMessagingTemplate.convertAndSendToUser(messageFile.getReceiver().getId(), "/retrieveMessage", messageNew);
            simpMessagingTemplate.convertAndSendToUser(messageFile.getSender().getId(), "/retrieveMessage", messageNew);
        } else {
            System.out.println(messageText);
            Message messageNew = messageService.retrieveMessageSingle(messageText);
            simpMessagingTemplate.convertAndSendToUser(messageText.getReceiver().getId(), "/retrieveMessage", messageNew);
            simpMessagingTemplate.convertAndSendToUser(messageText.getSender().getId(), "/retrieveMessage", messageNew);
        }
        return "";
    }

    @MessageMapping("/delete-message")
    public String deleteMessage(@Payload MessageText messageText, @Payload MessageFile messageFile, @Payload String idGroup) throws JsonProcessingException {
        System.out.println(messageFile.getSize());
        System.out.println(messageText.getContent());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(idGroup);
        String Group = rootNode.get("idGroup").asText();
        String ownerId = rootNode.get("ownerId").asText();
        if (Group.equals("")) {
            if (messageText.getContent() == null) {
                System.out.println(messageFile);
                messageService.deleteMessageSingle(messageFile, ownerId);
                simpMessagingTemplate.convertAndSendToUser(ownerId, "/deleteMessage", messageFile);
            } else {
                System.out.println(messageText.getReceiver());
                messageService.deleteMessageSingle(messageText, ownerId);
                simpMessagingTemplate.convertAndSendToUser(ownerId, "/deleteMessage", messageText);
            }
        } else {
        }
        return "";
    }

    @MessageMapping("/forward-message")
    public Message forwardMessage(@Payload List<MessageText> messageTexts, @Payload List<MessageFile> messageFiles) {
        if (messageTexts.get(0).getContent() == null) {
            for (MessageFile messageFile : messageFiles) {
                messageFile.setSenderDate(LocalDateTime.now());
                messageService.insertMessageSingleSender(messageFile);
                messageService.insertMessageSingleReceiver(messageFile);
                messageFile.setId(UUID.randomUUID().toString());
                simpMessagingTemplate.convertAndSendToUser(messageFile.getReceiver().getId() + "", "/singleChat", messageFile);
                simpMessagingTemplate.convertAndSendToUser(messageFile.getSender().getId() + "", "/singleChat", messageFile);
            }
            return null;
        } else {
            for (MessageText messageText : messageTexts) {
                messageText.setSenderDate(LocalDateTime.now());
                messageService.insertMessageSingleSender(messageText);
                messageService.insertMessageSingleReceiver(messageText);
                messageText.setId(UUID.randomUUID().toString());
                simpMessagingTemplate.convertAndSendToUser(messageText.getReceiver().getId() + "", "/singleChat", messageText);
                simpMessagingTemplate.convertAndSendToUser(messageText.getSender().getId() + "", "/singleChat", messageText);
            }
            return null;
        }

    }

    @MessageMapping("/deleteConversation")
    public Conversation deleteConversation(@Payload String conversation) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(conversation);
        String idGroup = rootNode.get("idGroup").asText();
        String idUser = rootNode.get("idUser").asText();
        String ownerId = rootNode.get("ownerId").asText();
        System.out.println(idGroup);
        System.out.println(idUser);
        System.out.println(ownerId);

        return null;
    }

    @MessageMapping("/private-single-message")
    public Message recMessageTextSingle(@Payload MessageText messageText, @Payload MessageFile messageFile) {
        if (messageText.getContent() == null) {
            messageFile.setSenderDate(LocalDateTime.now());
            messageService.insertMessageSingleSender(messageFile);
            messageService.insertMessageSingleReceiver(messageFile);
            simpMessagingTemplate.convertAndSendToUser(messageFile.getReceiver().getId() + "", "/singleChat", messageFile);
            simpMessagingTemplate.convertAndSendToUser(messageFile.getSender().getId() + "", "/singleChat", messageFile);

            return messageFile;
        } else {
            messageText.setSenderDate(LocalDateTime.now());
            messageService.insertMessageSingleSender(messageText);
            messageService.insertMessageSingleReceiver(messageText);
            simpMessagingTemplate.convertAndSendToUser(messageText.getReceiver().getId() + "", "/singleChat", messageText);
            simpMessagingTemplate.convertAndSendToUser(messageText.getSender().getId() + "", "/singleChat", messageText);
            return messageText;
        }

    }

    @MessageMapping("/request-add-friend")
    public User requestAddFriend(@Payload String node) throws JsonProcessingException {
        //{ id, userName, avt, receiverId}
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(node);
        String senderId = jsonNode.asText("id");
        String senderUserName = jsonNode.asText("userName");
        String senderAvt = jsonNode.asText("avt");
        String receiverId = jsonNode.asText("receiverId");
        boolean result = userService.addRequestAddFriend(senderId, receiverId);
        if (result) {
            User user = User.builder().id(senderId).userName(senderUserName).avt(senderAvt).build();
            simpMessagingTemplate.convertAndSendToUser(receiverId, "/requestAddFriend", user);
            return user;
        }
        return null;
    }

    @MessageMapping("/accept-friend-request")
    public FriendRequest acceptFriendRequest(@Payload FriendRequest friendRequest) {
        if (userService.addFriend(friendRequest.getSender().getId(), friendRequest.getReceiver().getId())) {
            simpMessagingTemplate.convertAndSendToUser(friendRequest.getSender().getId(), "/acceptAddFriend", friendRequest);
            simpMessagingTemplate.convertAndSendToUser(friendRequest.getReceiver().getId(), "/acceptAddFriend", friendRequest);
            return friendRequest;
        }
        return null;
    }

    @MessageMapping("/decline-friend-request")
    public FriendRequest declineFriendRequest(@Payload FriendRequest friendRequest) {
        if (userService.removeFriendRequest(friendRequest.getSender().getId(), friendRequest.getReceiver().getId())) {
            simpMessagingTemplate.convertAndSendToUser(friendRequest.getSender().getId(), "/declineAddFriend", friendRequest);
            simpMessagingTemplate.convertAndSendToUser(friendRequest.getReceiver().getId(), "/declineAddFriend", friendRequest);
            return friendRequest;
        }
        return null;
    }

    @MessageMapping("/private-group-message")
    public Message recMessageGroup(@Payload Message message) {
//        simpMessagingTemplate.convertAndSendToUser(message.getSender().getId(), "/groupChat", message);
        return message;
    }

    @MessageMapping("QR")
    public SendQR recMessageQR(@Payload SendQR sendQR) {
        simpMessagingTemplate.convertAndSendToUser(sendQR.getIp(), "/QR", sendQR);
        System.out.println(sendQR);
        return sendQR;
    }

}

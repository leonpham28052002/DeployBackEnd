package vn.edu.iuh.fit.chat_backend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
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
            int index = messageFile.getReceiver().getId().indexOf("_");
            if (index == -1){
                System.out.println(messageFile);
                Message messageNew = messageService.retrieveMessageSingle(messageFile);
                simpMessagingTemplate.convertAndSendToUser(messageFile.getReceiver().getId(), "/retrieveMessage", messageNew);
                simpMessagingTemplate.convertAndSendToUser(messageFile.getSender().getId(), "/retrieveMessage", messageNew);
            }else{
                String idGroup =messageFile.getReceiver().getId().substring(messageFile.getReceiver().getId().indexOf("_")+1,messageFile.getReceiver().getId().length());
                List<Member> memberList = messageService.retrieveMessageGroup(messageFile,idGroup);
                if (memberList.size() != 0){
                    for (Member member:memberList) {
                        messageFile.setMessageType(MessageType.RETRIEVE);
                        simpMessagingTemplate.convertAndSendToUser(member.getMember().getId().trim(), "/retrieveMessage", messageFile);
                    }
                }else{
                    simpMessagingTemplate.convertAndSendToUser(messageFile.getSender().getId().trim(), "/retrieveMessage", messageFile);
                }
            }
        } else {
            int index = messageText.getReceiver().getId().indexOf("_");
            if (index == -1){
                System.out.println(messageText);
                Message messageNew = messageService.retrieveMessageSingle(messageText);
                simpMessagingTemplate.convertAndSendToUser(messageText.getReceiver().getId(), "/retrieveMessage", messageNew);
                simpMessagingTemplate.convertAndSendToUser(messageText.getSender().getId(), "/retrieveMessage", messageNew);
            }else{
                String idGroup =messageText.getReceiver().getId().substring(messageText.getReceiver().getId().indexOf("_")+1,messageText.getReceiver().getId().length());
                List<Member> memberList = messageService.retrieveMessageGroup(messageText,idGroup);
                if (memberList.size() != 0){
                    for (Member member:memberList) {
                        messageText.setMessageType(MessageType.RETRIEVE);
                        simpMessagingTemplate.convertAndSendToUser(member.getMember().getId().trim(), "/retrieveMessage", messageText);
                    }
                }else{
                    simpMessagingTemplate.convertAndSendToUser(messageText.getSender().getId().trim(), "/retrieveMessage", messageText);
                }
            }

        }
        return "";
    }

    @MessageMapping("/delete-message")
    public String deleteMessage(@Payload MessageText messageText, @Payload MessageFile messageFile, @Payload String idGroup) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(idGroup);
        String Group = rootNode.get("idGroup").asText();
        String ownerId = rootNode.get("ownerId").asText();
        if (Group.trim().equals("")) {
            if (messageText.getContent() == null) {
                System.out.println(messageFile);
                messageService.deleteMessageSingle(messageFile, ownerId,Group);
                simpMessagingTemplate.convertAndSendToUser(ownerId, "/deleteMessage", messageFile);
            } else {
                System.out.println(messageText.getReceiver());
                messageService.deleteMessageSingle(messageText, ownerId,Group);
                simpMessagingTemplate.convertAndSendToUser(ownerId, "/deleteMessage", messageText);
            }
        } else {
            if (messageText.getContent() == null) {
                System.out.println(messageFile);
                messageService.deleteMessageSingle(messageFile, ownerId,Group);
                simpMessagingTemplate.convertAndSendToUser(ownerId, "/deleteMessage", messageFile);
            } else {
                System.out.println(messageText.getReceiver());
                messageService.deleteMessageSingle(messageText, ownerId,Group);
                simpMessagingTemplate.convertAndSendToUser(ownerId, "/deleteMessage", messageText);
            }
        }
        return "";
    }

    @MessageMapping("/forward-message")
    public Message forwardMessage(@Payload List<MessageText> messageTexts, @Payload List<MessageFile> messageFiles) {

        if (messageTexts.get(0).getContent() == null) {
            for (MessageFile messageFile : messageFiles) {
<<<<<<< HEAD
                int index = messageFile.getReceiver().getId().indexOf("_");
                if (index == -1){
                    messageFile.setSenderDate(LocalDateTime.now());
                    messageFile.setId(UUID.randomUUID().toString());
                    messageService.insertMessageSingleSender(messageFile);
                    messageService.insertMessageSingleReceiver(messageFile);
                    simpMessagingTemplate.convertAndSendToUser(messageFile.getReceiver().getId() + "", "/singleChat", messageFile);
                    simpMessagingTemplate.convertAndSendToUser(messageFile.getSender().getId() + "", "/singleChat", messageFile);
                }else{
                    String idGroup =messageFile.getReceiver().getId().substring(messageFile.getReceiver().getId().indexOf("_")+1,messageFile.getReceiver().getId().length());
                    List<Member> memberList = messageService.insertMessageGroup(messageFile, idGroup);
                    if (memberList.size() !=0) {
                        for (Member member:memberList) {
                            messageFile.setId(UUID.randomUUID().toString());
                            simpMessagingTemplate.convertAndSendToUser(member.getMember().getId() + "", "/groupChat", messageFile);
                        }
                    } else {
                        simpMessagingTemplate.convertAndSendToUser(messageFile.getSender().getId() + "", "/groupChat", messageFile);
                    }
                }
            }
            return null;
        } else {

            for (MessageText messageText : messageTexts) {
                System.out.println("user: "+messageText.getReceiver().getId());
                int index = messageText.getReceiver().getId().indexOf("_");
                if (index == -1){
                    messageText.setSenderDate(LocalDateTime.now());
                    messageText.setId(UUID.randomUUID().toString());
                    messageService.insertMessageSingleSender(messageText);
                    messageService.insertMessageSingleReceiver(messageText);
                    simpMessagingTemplate.convertAndSendToUser(messageText.getReceiver().getId() + "", "/singleChat", messageText);
                    simpMessagingTemplate.convertAndSendToUser(messageText.getSender().getId() + "", "/singleChat", messageText);
                }else{
                    String idGroup =messageText.getReceiver().getId().substring(messageText.getReceiver().getId().indexOf("_")+1,messageText.getReceiver().getId().length());
                    List<Member> memberList = messageService.insertMessageGroup(messageText, idGroup);
                    if (memberList.size() !=0) {
                        for (Member member:memberList) {
                            messageText.setId(UUID.randomUUID().toString());
                            simpMessagingTemplate.convertAndSendToUser(member.getMember().getId() + "", "/groupChat", messageText);
                        }
                    } else {
                        simpMessagingTemplate.convertAndSendToUser(messageText.getSender().getId() + "", "/groupChat", messageText);
                    }
                }

=======
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
>>>>>>> a16d8d700dba856b2d1171d21de0947e4d5bbf55
            }
            return null;
        }
    }

    @MessageMapping("/create-Group")
    public void createGroup(@Payload List<User> members){

    }

    @MessageMapping("/deleteConversation")
    public Conversation deleteConversation(@Payload String conversation) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(conversation);
        String idGroup = rootNode.get("idGroup").asText();
        String idUser = rootNode.get("idUser").asText();
        String ownerId = rootNode.get("ownerId").asText();
        Conversation conversation1 = messageService.deleteConversation(ownerId, idUser, idGroup);
        if (conversation1 != null) {
            simpMessagingTemplate.convertAndSendToUser(ownerId + "", "/deleteConversation", conversation1);
        } else {
            simpMessagingTemplate.convertAndSendToUser(ownerId + "", "/deleteConversation", new Conversation());
        }
        return null;
    }

    @MessageMapping("/private-single-message")
    public Message recMessageTextSingle(@Payload MessageText messageText, @Payload MessageFile messageFile, @Payload String idGroup) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(idGroup);
        String idG = rootNode.get("idGroup").asText();
        if (!idG.trim().equals("")) {
            System.out.println(idG);
            if (messageText.getContent() == null) {
                List<Member> memberList = messageService.insertMessageGroup(messageFile, idG);
                if (memberList.size() !=0) {
                    for (Member member:memberList) {
                        simpMessagingTemplate.convertAndSendToUser(member.getMember().getId() + "", "/groupChat", messageFile);
                    }
                } else {
                    simpMessagingTemplate.convertAndSendToUser(messageFile.getSender().getId() + "", "/groupChat", messageFile);

                }
            } else {
                List<Member> memberList = messageService.insertMessageGroup(messageText, idG);
                if (memberList.size() !=0) {
                    for (Member member:memberList) {
                        simpMessagingTemplate.convertAndSendToUser(member.getMember().getId() + "", "/groupChat", messageText);
                    }
                } else {
                    simpMessagingTemplate.convertAndSendToUser(messageText.getSender().getId() + "", "/groupChat", messageText);
                }
            }
        } else {
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
        return null;
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
    @MessageMapping("/video")
    @SendTo("/video-call")
    public String sendVideo(String videoData) {
        // Xử lý dữ liệu video và gửi đến tất cả các subscriber
        System.out.println(videoData);
        simpMessagingTemplate.convertAndSendToUser("video", "/nhan", videoData);

        return videoData;
    }

}

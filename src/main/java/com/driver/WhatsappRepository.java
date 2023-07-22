package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository  {
     private HashMap<Group, List<User>> groupUserMap=new HashMap<>();
     private HashMap<Group, List<Message>> groupMessageMap=new HashMap<>();
    private HashMap<Message, User> senderMap=new HashMap<>();
     private HashMap<Group, User> adminMap=new HashMap<>();
    private HashSet<String> userMobile=new HashSet<>();
    private int customGroupCount=0;
     private int messageId=0;

    public String createUser(String name, String mobile) throws Exception {
        if(userMobile.contains(mobile)) throw new Exception("User already exists");
        userMobile.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        Group gp=new Group();
        int size= users.size();
        if(size==2){
            gp.setName(users.get(1).getName());
        }
        if(size>2){
            gp.setName("Group "+this.customGroupCount);
            customGroupCount++;
            adminMap.put(gp,users.get(0));
        }
        gp.setNumberOfParticipants(size);
        groupUserMap.put(gp,new ArrayList<>(users));

     return gp;
    }

    public int createMessage(String content) {
        this.messageId++;
        Message message=new Message(messageId,content);
        return messageId;

    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(groupUserMap.containsKey(group)==false) throw new Exception("Group does not exist");

        if(groupUserMap.get(group).contains(sender)==false) throw new Exception("You are not allowed to send message");
        groupMessageMap.putIfAbsent(group,new ArrayList<Message>());
        groupMessageMap.get(group).add(message);

        senderMap.put(message,sender);
        return groupMessageMap.get(group).size();

    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if(groupUserMap.containsKey(group)){
           if(adminMap.get(group).equals(approver)){
               if(groupUserMap.get(group).contains(user)){
                   adminMap.put(group,user);

               }else throw new Exception("User is not a participant");

            }else throw new Exception("Approver does not have rights");

        }else throw new Exception("Group does not exist");
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception{
        boolean found =false;
        Group gp=null;
        for(Group ele:groupUserMap.keySet()){
            if(groupUserMap.get(ele).contains(user)){
                found=true;
                gp=ele;
                break;
            }
        }
        if(found==false) throw new Exception("User not found");
        for(User ele:adminMap.values()){
            if(ele==user) throw new Exception("Cannot remove admin");
        }
        //remove the user from groupUserMap
        groupUserMap.get(gp).remove(user);
        //messages to be removed
        List<Message> toBeRemoved=new ArrayList<>();
        for(Message ele:senderMap.keySet()){
            if(senderMap.get(ele).equals(user)){
                toBeRemoved.add(ele);
            }
        }
       //removing messages from sendermap
        for(Message ele:toBeRemoved){
            senderMap.remove(ele);
        }
        // //removing from groupMessageMap
                groupMessageMap.get(gp).removeAll(toBeRemoved);
      //removing mobile of the user from userMobile
            userMobile.remove(user.getMobile());
             return groupUserMap.get(gp).size()+groupMessageMap.get(gp).size()+senderMap.size();
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        List<Message> list=new ArrayList<>();
        for(Message ele:senderMap.keySet()){
            if(ele.getTimestamp().after(start) && ele.getTimestamp().before(end)){
                list.add(ele);
            }
        }
        if(list.size()<k) throw new Exception("K is greater than the number of messages");
        Collections.sort(list, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                if(o1.getTimestamp().after(o2.getTimestamp())){
                    return -1;
                }
                return 1;
            }
        });
        return list.get(k-1).getContent();
    }
}

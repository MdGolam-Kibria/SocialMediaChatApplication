package com.example.chatapplication;

import lombok.Data;

@Data
public class ModelUser {
    //user same name as firebase database
    String name,email,search,phone,image,cover,uid,onlineStatus,typingTo;

}

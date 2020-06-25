package com.example.chatapplication.modelAll;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ModelPost {
    //use same name as we given while upload post.
    //upload korar time  e jei name gula use korechilam segulai ekhane same to same use korte hobe.
    String pId,pTitle,pDescr,pLikes,pImage,pTime,uid,uEmail,uDp,uName;

}

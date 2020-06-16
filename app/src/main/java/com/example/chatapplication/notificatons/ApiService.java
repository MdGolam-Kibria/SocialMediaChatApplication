package com.example.chatapplication.notificatons;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAy0sN72U:APA91bFoLnF6ro6VVyPglH8CfvPcn3Xt9dpDRUK-Nby2LsBSU7-QaurFNPn9gXwXDpjsBJG-etsOfVld4tAzRoPZna-UHLad05s1kvs9fHXgYwI5UKJoQIgCtQyb_KCm3npMAARFfD0k"
    })
    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}

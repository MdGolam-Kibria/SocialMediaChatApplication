# SocialMediaChatApplication
In this project we develop a social media application
### Social media application../Chat application here all features of the project below
- backend Firebase backend system.
- authentication and authorization using user email
- Forgot password recovery system using user email address with Gmail account if user have a valid Gmail account with this account.
- user based chat.
- group chat.
- Google Sign in method.
- add user based profile and chatting access 😊
- update profile name,image and phone number.
- show active users 👤 .
- search active users 👥  by her name or email.-
- convert profile image Base 64 and add/update profile picture.
- implement seen delivered and send time.
- delete message.
- show relatime user typing or not
- add notification sound
- implement active or not.
- implement Notifications <br/>
              *when a sender send sms to another user user get a notification
- add post ,,,user can post a status with image and without image like FACEBOOK
- implement post date and time.
-add user email in this post page i mean current user email.
- show all user post with image and without image with set like,comment,share options.
- update profile  features using UI.<br/>



```java
// For Firebase Device to Device notifications.

At first connect your app into firebase database ,clould messaging after all follow the below proccess


1. At firat make a class name Token.class

@Data
public class Token{
	String token;
	}

2. make another class called Data.class
@Data
public class Data{
	private String user,body,title,sent;
	private Integer icon;
	}

3.make another class called Sender.class
@Data	
public class Sender{
	private Data data;
	private String to;
	}
4.make another class called Response.class
public class Response{
	private String response success;
	}
5.make another one class called Client.class
public class Client{
 
 private static Retrofit retrofit = null;

    public static Retrofit getRetrofit(String url) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
6.make a service called FirebaseService.class

public class FirebaseService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String tokenRefresh = FirebaseInstanceId.getInstance().getToken();
        if (user != null) {
            updateToken(tokenRefresh);
        }

    }

    private void updateToken(String tokenRefresh) {//This is for save update token in firebase database
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefresh);
        ref.child(user.getUid()).setValue(token);
    }
}
7.make another class called OreoAndAboveNotification.class
public class OreoAndAboveNotification extends ContextWrapper {
    private static final String ID = "some_id";
    private static final String NAME = "firebase_APP";
    private NotificationManager notificationManager;

    public OreoAndAboveNotification(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(ID, NAME, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getNotificationManager().createNotificationChannel(notificationChannel);//here getNotificationManager() method from below
    }

    public NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotifications(String title, String body, PendingIntent pIntent, Uri soundUri, String icon) {
        return new Notification.Builder(getApplicationContext(), ID)
                .setContentIntent(pIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setSmallIcon(Integer.parseInt(icon));
    }
}
8.make one of another class called ApiService.class
public interface ApiService {//below "Authorization:key=your firebase notification server key"
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAy0sN72U:APA91bFoLnF6ro6VVyPglH8CfvPcn3Xt9dpDRUK-Nby2LsBSU7-QaurFNPn9gXwXDpjsBJG-etsOfVld4tAzRoPZna-UHLad05s1kvs9fHXgYwI5UKJoQIgCtQyb_KCm3npMAARFfD0k"
    })
    @POST("fcm/send")//this is unique url for sending FCM notifications
    Call<Response> sendNotification(@Body Sender body);
}
9.after all in your app main activity/fragment create a method and call it ito onCreateView for update your token like below the process


in onCreateView{
 updateToken(FirebaseInstanceId.getInstance().getToken());//call the method and pass your token as a paramiter like this
}

 private void updateToken(String token) {
        DatabaseReference df = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        df.child(mUID).setValue(mToken);
    }

9. After all you need to make another class called FirebaseMessaging.class like below

public class FirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //getCurrent userFromSharedPreferences
        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentCurrentUser = sp.getString("Current_USERID", "None");
        String sent = remoteMessage.getData().get("sent");
        String user = remoteMessage.getData().get("user");
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null && sent.equals(fUser.getUid())) {
            if (!savedCurrentCurrentUser.equals(user)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendoAndAboveNotification(remoteMessage);
                } else {
                    sendNormalNotification(remoteMessage);
                }
            }
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent =PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);
        Uri defSoundtUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundtUri)
                .setContentIntent(pIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int j=0;
        if (i>0){
            j=i;
        }
        notificationManager.notify(j,builder.build());


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendoAndAboveNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent =PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);
        Uri defSoundtUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder =notification1.getNotifications(title,body,pIntent,defSoundtUri,icon);
        int j=0;
        if (i>0){
            j=i;
        }
        notification1.getNotificationManager().notify(j,builder.build());
    }
}

10.when you send notification follow thw below process
		*in your activity or fragment make make a instance like 
			ApiService apiService;//This own create class
		*initialize it in your onCreateView like,
			 apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(ApiService.class);


#####now youcan send notification like

			apiService.sendNotification(pass your needed parameters here);


			

```

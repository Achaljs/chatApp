package com.example.chatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatApp.adapter.ChatRecyclerAdapter;
import com.example.chatApp.model.ChatMessageModel;
import com.example.chatApp.model.ChatroomModel;
import com.example.chatApp.model.UserModel;
import com.example.chatApp.utils.AndroidUtil;
import com.example.chatApp.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    UserModel otherUser;
    String chatroomId;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;

    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton fileSendBtn;
    ImageButton backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ImageView imageView;

    private final Handler handler = new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get UserModel
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(),otherUser.getUserId());

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        imageView = findViewById(R.id.profile_pic_image_view);
        fileSendBtn = findViewById(R.id.file_send_btn);
        FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if(t.isSuccessful()){
                        Uri uri  = t.getResult();
                        AndroidUtil.setProfilePic(this,uri,imageView);
                    }
                });

        backBtn.setOnClickListener((v)->{
            onBackPressed();
        });
        otherUsername.setText(otherUser.getUsername());

        sendMessageBtn.setOnClickListener((v -> {

           // token t= new token();
          //  t.generateAccessToken(ChatActivity.this,handler);
            String message = messageInput.getText().toString().trim();
            if(message.isEmpty())
                return;
            sendMessageToUser(message,null);
        }));

fileSendBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");  // Set the MIME type to allow all file types
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(Intent.createChooser(intent, "Select File"), 1001);
    }
});






        getOrCreateChatroomModel();
        setupChatRecyclerView();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri fileUri = data.getData();
                // Handle the file upload process here
                uploadFile(fileUri);
            }
        }



    }


    void setupChatRecyclerView(){
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query,ChatMessageModel.class).build();

        adapter = new ChatRecyclerAdapter(options,getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    void sendMessageToUser(String message, String fileurl){

        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message,FirebaseUtil.currentUserId(),fileurl,Timestamp.now());
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()){
                            messageInput.setText("");
                            sendNotification(message);
                        }
                    }
                });
    }

    void getOrCreateChatroomModel(){
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if(chatroomModel==null){
                    //first time chat
                    chatroomModel = new ChatroomModel(
                            chatroomId,
                            Arrays.asList(FirebaseUtil.currentUserId(),otherUser.getUserId()),
                            Timestamp.now(),
                            ""
                    );
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                }
            }
        });
    }



    private void uploadFile(Uri fileUri) {
        // Perform the file upload process using Firebase Storage or any other backend service
        // You can use the fileUri to get the file path or upload the file directly

        // Example: Upload file to Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("uploads/" + UUID.randomUUID().toString());

        UploadTask uploadTask = storageRef.putFile(fileUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // File uploaded successfully
            // Get the download URL and send it as a message to the recipient or save it to a database
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                // Send the downloadUrl to the recipient as a message or save it to the database
                sendMessageToUser("", downloadUrl);
            });
        }).addOnFailureListener(e -> {
            // Handle any errors that occur during the file upload
        });
    }



    void sendNotification(String message){

       FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               UserModel currentUser = task.getResult().toObject(UserModel.class);
               try{
                   JSONObject jsonObject  = new JSONObject();

                   JSONObject notificationObj = new JSONObject();
                   notificationObj.put("title",currentUser.getUsername());
                   notificationObj.put("body",message);

                   JSONObject dataObj = new JSONObject();
                   dataObj.put("userId",currentUser.getUserId());

                   jsonObject.put("notification",notificationObj);
                   jsonObject.put("data",dataObj);
                   jsonObject.put("to",otherUser.getFcmToken());

                   callApi(jsonObject);


               }catch (Exception e){

               }

           }
       });

    }

    void callApi(JSONObject jsonObject){
         MediaType JSON = MediaType.get("application/json; charset=utf-8");
         OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/v1/projects//messages:send";
        RequestBody body = RequestBody.create(jsonObject.toString(),JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization","Bearer ya29.c.c0AY_VpZhWVpU5l_gEfuwG6TJcZ2HIzybR2tpepNaI3wbJcY9OBiOzP8qcjRPy7-PnEQTSKPZPBYTt2QpSczMvLHBLcPWwbxGfD1966JdOS15e4ZEJZNyuWr7WbO77xnZtVEgx8uivGUVHaB7-MM9lPpazD_Fhty5nr6PNgqcxh_8p4g-dEH2BoACS95nNNAfi69rXdJsm38TsrHRLGqL9Vf2F91BFO-7UpQmR2gdLkH23L2OG0JPDd3xtmfygzlEEoE77irmBVIY49Y_mU0q3Xu-0PqDzK-AFwU0xUynfzDquiiEu8YgJBQOUwB0PR_o1g71V9KcP8BnFOD1x3c8mgvp6j5g0OM_JyC_eKPqeylBHO73f9xFV9udFE385AIY2gzxy-dMOb0mgc8ez2dOY8j_mc6YyefZbU84ivtqx9_Vsw7VUqsVX1Zjv_SuYZ0QjZRB1M59rclWl2UOlzc-rz9Xtnb68cfRWdUczQpqdzY8_Rf6osaFVyzqvWkg88gJmMpqWeauBRYS3XtqnIuoOw0Udm6QJhjqcfaayhc4pIVlBO4jYQm5b-lhJ2WjtW-8JwkUwBp9MS8UId24hzcIfXd5FMoli2sQ3SfqfXQ2bz2fqc36nVohJzOqIXZ6ar8BbQVrzewjjZ466cxo6WyS_2n_Flj5QwyuhYvvg55dtSiUJSXXRbd3cFe_SwIW1aer-oU_7UkfiUec35SoO5rJ1-6Wznh-_Y_2Jm42UZsqroWc0mbonIIxq5WvjF9OFSO7j4rF9Ryc5UYVYaQr96UU7jj2hXMFvlXhUyJJI2uO93xmvnaSwxY2u-obtJJefhSO2Rmuz9RcVSMkFn2d8iSOlb06oeZtjbmeOn0MS80yXQMhX32ct4FcscertUeoY57cuU-qY0fmU9OpYZ53wrvspSFmm7iyO8mu6-xwXs7lVpuv8wb118jgswoSlRmj0BnsqQiUYknsOa23OjWM_YQqmww2z3otR4Siy7-Vp_JnpRbaZg-X5Y263aW1")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });

    }



















}
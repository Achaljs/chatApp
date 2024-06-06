package com.example.chatApp;

import android.util.Log;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.oauth2.AccessToken;

import java.io.IOException;
import java.io.InputStream;
import android.os.Handler;;

public class token {

    private static final String TAG = "FirebaseAccessTokenGenerator";

    public  void generateAccessToken(ChatActivity context, Handler handler) {

        new Thread(() -> {
            InputStream inputStream = context.getResources().openRawResource(R.raw.key);

            try {
                GoogleCredentials credentials = ServiceAccountCredentials.fromStream(inputStream).createScoped("https://www.googleapis.com/auth/firebase.messaging");
                AccessToken token = credentials.refreshAccessToken();
                String accessToken = token.getTokenValue();

                handler.post(() -> Log.d(TAG, "Access Token: " + accessToken)); // Post back to UI thread
            } catch (IOException e) {
                handler.post(() -> Log.e(TAG, "Error reading credentials file: " + e.getMessage())); // Post back error to UI thread
            }
        }).start();
    }
}

//Access Token= "ya29.c.c0AY_VpZhWVpU5l_gEfuwG6TJcZ2HIzybR2tpepNaI3wbJcY9OBiOzP8qcjRPy7-PnEQTSKPZPBYTt2QpSczMvLHBLcPWwbxGfD1966JdOS15e4ZEJZNyuWr7WbO77xnZtVEgx8uivGUVHaB7-MM9lPpazD_Fhty5nr6PNgqcxh_8p4g-dEH2BoACS95nNNAfi69rXdJsm38TsrHRLGqL9Vf2F91BFO-7UpQmR2gdLkH23L2OG0JPDd3xtmfygzlEEoE77irmBVIY49Y_mU0q3Xu-0PqDzK-AFwU0xUynfzDquiiEu8YgJBQOUwB0PR_o1g71V9KcP8BnFOD1x3c8mgvp6j5g0OM_JyC_eKPqeylBHO73f9xFV9udFE385AIY2gzxy-dMOb0mgc8ez2dOY8j_mc6YyefZbU84ivtqx9_Vsw7VUqsVX1Zjv_SuYZ0QjZRB1M59rclWl2UOlzc-rz9Xtnb68cfRWdUczQpqdzY8_Rf6osaFVyzqvWkg88gJmMpqWeauBRYS3XtqnIuoOw0Udm6QJhjqcfaayhc4pIVlBO4jYQm5b-lhJ2WjtW-8JwkUwBp9MS8UId24hzcIfXd5FMoli2sQ3SfqfXQ2bz2fqc36nVohJzOqIXZ6ar8BbQVrzewjjZ466cxo6WyS_2n_Flj5QwyuhYvvg55dtSiUJSXXRbd3cFe_SwIW1aer-oU_7UkfiUec35SoO5rJ1-6Wznh-_Y_2Jm42UZsqroWc0mbonIIxq5WvjF9OFSO7j4rF9Ryc5UYVYaQr96UU7jj2hXMFvlXhUyJJI2uO93xmvnaSwxY2u-obtJJefhSO2Rmuz9RcVSMkFn2d8iSOlb06oeZtjbmeOn0MS80yXQMhX32ct4FcscertUeoY57cuU-qY0fmU9OpYZ53wrvspSFmm7iyO8mu6-xwXs7lVpuv8wb118jgswoSlRmj0BnsqQiUYknsOa23OjWM_YQqmww2z3otR4Siy7-Vp_JnpRbaZg-X5Y263aW1"



package com.example.socialnetwork;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private Button btn_login;
    private EditText edt_email, edt_password;
    private TextView txt_createAccountLink, txt_resetPasswordLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private ImageView login_google;
    // https://www.youtube.com/watch?v=Zz3412C4BSA google one tap client
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest,signUpRequest;
    private static final int REQ_ONE_TAP = 2;
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = edt_email.getText().toString();
                String password = edt_password.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(LoginActivity.this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
                }else{
                    // 顯示 loading bar
                    loadingBar.setTitle("Login");
                    loadingBar.setMessage("Please wait, while we're allowing you to log into your account...");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "Successfully login.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                // 登入成功，切到主畫面
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }else{
                                String error = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error occurred : "+error, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }
        });



//        login_google.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                oneTapClient.beginSignIn(signInRequest)
//                        .addOnSuccessListener(LoginActivity.this, new OnSuccessListener<BeginSignInResult>() {
//                            @Override
//                            public void onSuccess(BeginSignInResult result) {
//                                IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
//                                activityResultLauncher.launch(intentSenderRequest);
//                            }
//
//                        })
//                        .addOnFailureListener(LoginActivity.this, new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                // No saved credentials found. Launch the One Tap sign-up flow, or
//                                // do nothing and continue presenting the signed-out UI.
//                            }
//                        });
//
//            }
//        });




    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser current_user = mAuth.getCurrentUser();
        if(current_user != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void initViews(){
        btn_login = findViewById(R.id.btn_login_login);
        edt_email = findViewById(R.id.edt_reg_email);
        edt_password = findViewById(R.id.edt_login_password);
        txt_createAccountLink = findViewById(R.id.txt_createAccount);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
        login_google = findViewById(R.id.login_google);
        txt_resetPasswordLink = findViewById(R.id.login_txt_resetPassword);

        txt_createAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        txt_resetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

//        oneTapClient = Identity.getSignInClient(this);
        // 創建帳號請求
//        signUpRequest = BeginSignInRequest.builder()
//                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                        .setSupported(true)
//                        // Your server's client ID, not your Android client ID.
//                        .setServerClientId(getString(R.string.web_client_id))
//                        // Show all accounts on the device.
//                        .setFilterByAuthorizedAccounts(false)
//                        .build())
//                .build();
        // 登入請求
//        signInRequest = BeginSignInRequest.builder()
//                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
//                        .setSupported(true)
//                        .build())
//                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                        .setSupported(true)
//                        // Your server's client ID, not your Android client ID.
//                        .setServerClientId(getString(R.string.web_client_id))
//                        // Only show accounts previously used to sign in.
//                        .setFilterByAuthorizedAccounts(true)
//                        .build())
//                // Automatically sign in when exactly one credential is retrieved.
//                .setAutoSelectEnabled(true)
//                .build();


//        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
//            @Override
//            public void onActivityResult(ActivityResult result) {
//                if(result.getResultCode() == RESULT_OK){
//                    try {
//                        SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
//                        String idToken = credential.getGoogleIdToken();
//                        if (idToken !=  null) {
//                            // Got an ID token from Google. Use it to authenticate
//                            // with your backend.
//                            String email = credential.getId();
//                            Toast.makeText(LoginActivity.this, email, Toast.LENGTH_SHORT).show();
//
//                        }
//                    } catch (ApiException e) {
//                        // ...
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });

        
    }
}
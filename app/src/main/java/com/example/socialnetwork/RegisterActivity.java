package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText email, password, confirmed_password;
    private Button btn_create;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s_email = email.getText().toString();
                String s_password = password.getText().toString();
                String s_confirmed_password = confirmed_password.getText().toString();

                if(TextUtils.isEmpty(s_email)){
                    Toast.makeText(RegisterActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(s_password)){
                    Toast.makeText(RegisterActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(s_confirmed_password)){
                    Toast.makeText(RegisterActivity.this, "Please enter your confirmed password.", Toast.LENGTH_SHORT).show();
                }else if(!s_password.equals(s_confirmed_password)){
                    Toast.makeText(RegisterActivity.this, "Please check if your password equals to your confirmed password.", Toast.LENGTH_SHORT).show();
                }else{
                    // 開始處理與 Firebase 的通訊時，顯示 loading bar
                    loadingBar.setTitle("Creating new Account");
                    loadingBar.setMessage("Please wait, while we are creating your new account...");
                    // 讓 loading bar 會被使用者按旁邊的地方就消失
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();


                    mAuth.createUserWithEmailAndPassword(s_email,s_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "You're authenticated successfully.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                // 成功創立帳號後，將使用者帶到 setup 頁面
                                Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }else{
                                String error = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error occurred : " + error, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser current_user = mAuth.getCurrentUser();
        if(current_user != null){
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void initViews(){
        email = findViewById(R.id.edt_reg_email);
        password = findViewById(R.id.edt_reg_password);
        confirmed_password = findViewById(R.id.edt_reg_confirmed_password);
        btn_create = findViewById(R.id.btn_reg_create_account);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
    }
}
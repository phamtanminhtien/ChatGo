package com.tietha.chatgo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.tietha.chatgo.custom.LoadingDialog;
import com.tietha.chatgo.event.OnSwipeTouchListener;

public class LoginActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;
    private Button btnLogin, btnRegister;
    private EditText userName, passWord;
    private int count = 0;
    private FirebaseAuth mAuth;
    private LoadingDialog mLoadingDialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();
        mLoadingDialog = new LoadingDialog(this);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        btnLogin = findViewById(R.id.signin);
        btnRegister = findViewById(R.id.signup);
        userName = findViewById(R.id.username);
        passWord = findViewById(R.id.password);

        imageView.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            public void onSwipeTop() {
            }

            public void onSwipeRight() {
                if (count == 0) {
                    imageView.setImageResource(R.drawable.good_night_img);
                    textView.setText("Night");
                    count = 1;
                } else {
                    imageView.setImageResource(R.drawable.good_morning_img);
                    textView.setText("Morning");
                    count = 0;
                }
            }

            public void onSwipeLeft() {
                if (count == 0) {
                    imageView.setImageResource(R.drawable.good_night_img);
                    textView.setText("Night");
                    count = 1;
                } else {
                    imageView.setImageResource(R.drawable.good_morning_img);
                    textView.setText("Morning");
                    count = 0;
                }
            }

            public void onSwipeBottom() {
            }

        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionLogin(userName.getText().toString(), passWord.getText().toString());
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionRegister();
            }
        });

    }

    private void ActionRegister() {
        btnRegister.setEnabled(false);
        Intent iLogin = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(iLogin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnRegister.setEnabled(true);
    }

    private void ActionLogin(String email, String password) {
        if (email.equals("") || password.equals("")) {
            Snackbar.make(findViewById(R.id.viewContent), "Empty Email/Password", Snackbar.LENGTH_LONG).show();
            return;
        }
        mLoadingDialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mLoadingDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Dang Nhap Thanh Cong", Toast.LENGTH_SHORT).show();
                    Intent iResult = new Intent();
                    setResult(Activity.RESULT_OK, iResult);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}

package com.tietha.chatgo;

import android.annotation.SuppressLint;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tietha.chatgo.custom.LoadingDialog;
import com.tietha.chatgo.event.OnSwipeTouchListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;
    private Button btnLogin, btnRegister;
    private EditText userName, passWord, name;
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
        setContentView(R.layout.activity_register);


        mAuth = FirebaseAuth.getInstance();
        mLoadingDialog = new LoadingDialog(this);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        btnLogin = findViewById(R.id.signin);
        btnRegister = findViewById(R.id.signup);
        userName = findViewById(R.id.username);
        passWord = findViewById(R.id.password);
        name = findViewById(R.id.name);

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
                ActionLogin();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionRegister(userName.getText().toString(), passWord.getText().toString(), name.getText().toString());
            }
        });

    }

    private void ActionRegister(String email, String password, final String name) {
        if (email.equals("") || password.equals("") || name.equals("")) {
            Snackbar.make(findViewById(R.id.viewContent), "Empty Email/Name/Password", Snackbar.LENGTH_LONG).show();
            return;
        }
        mLoadingDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mLoadingDialog.dismiss();
                if (task.isSuccessful()) {

                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference().child("Users").child(currentUser.getUid());
                    HashMap<String, String> info = new HashMap<>();
                    info.put("name", name);
                    info.put("avatar", "default");
                    info.put("des", "default");
                    info.put("thumb", "default");
                    myRef.setValue(info);

                    Toast.makeText(RegisterActivity.this, "Dang Ky Thanh Cong, Vui Long Dang Nhap", Toast.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(findViewById(R.id.viewContent), "Register Failed", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void ActionLogin() {
        btnLogin.setEnabled(false);
        Intent iLogin = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(iLogin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnLogin.setEnabled(true);
    }
}

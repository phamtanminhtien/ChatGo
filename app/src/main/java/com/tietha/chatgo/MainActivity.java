package com.tietha.chatgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tietha.chatgo.custom.ActionDialog;
import com.tietha.chatgo.custom.LoadingDialog;
import com.tietha.chatgo.fragment.ChatFragment;
import com.tietha.chatgo.fragment.FriendFragment;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private final int REQUEST_CODE_LOGIN = 2509;
    private ActionDialog confirmLogin;
    private MaterialToolbar toolbar;
    private CircleImageView imgToolbarAvatar;
    private FirebaseDatabase mDatabase;
    final Fragment fragment1 = new ChatFragment();
    final Fragment fragment2 = new FriendFragment(this);
    final FragmentManager fm = getSupportFragmentManager();
    Fragment FMactive;
    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        confirmLogin = new ActionDialog(this);
        confirmLogin.setText("Vui lòng đăng nhập để sử dụng các tính năng của ứng dụng!")
                .setButton("Đăng Nhập hoặc Đăng Ký", new ActionDialog.OnClickListener() {
                    @Override
                    public void onClick() {
                        Intent iLogin = new Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(iLogin, REQUEST_CODE_LOGIN);
                    }
                });
        setContentView(R.layout.activity_main);
        initBottomNavigationView();
        initUser();
        setupToolBar();
    }

    private void initBottomNavigationView() {
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        FMactive = fragment1;
        fm.beginTransaction().add(R.id.main_container, fragment2, "2").hide(fragment2).commit();
        fm.beginTransaction().add(R.id.main_container, fragment1, "1").commit();
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_chat:
                        fm.beginTransaction().hide(FMactive).show(fragment1).commit();
                        FMactive = fragment1;
                        return true;
                    case R.id.action_friend:
                        fm.beginTransaction().hide(FMactive).show(fragment2).commit();
                        FMactive = fragment2;
                        return true;
                }
                return false;
            }
        });
    }

    private void setupToolBar() {
        toolbar = findViewById(R.id.toolBar_main);
        imgToolbarAvatar = toolbar.findViewById(R.id.avatar_appbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toolbar.findViewById(R.id.avatar_appbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iProfile = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(iProfile);
            }
        });
    }

    private void initUser() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLogin();
    }

    private void checkLogin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            confirmLogin.show();
        } else {
            updateUI(currentUser);
            OnDesconnect.on(mAuth.getUid());
        }
    }

    private void updateUI(FirebaseUser currentUser) {
        final LoadingDialog waitForLoading = new LoadingDialog(this);
        waitForLoading.show();

        DatabaseReference myDBRef = mDatabase.getReference().child("Users").child(currentUser.getUid());

        DatabaseReference myDBthumbRef = myDBRef.child("thumb");
        myDBthumbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String avatarUrl = dataSnapshot.getValue(String.class);
                Picasso.get().load(avatarUrl).into(imgToolbarAvatar);
                waitForLoading.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_LOGIN:
                switch (resultCode) {
                    case RESULT_OK:
                        updateUI(mAuth.getCurrentUser());
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar, menu);
        return true;
    }

}

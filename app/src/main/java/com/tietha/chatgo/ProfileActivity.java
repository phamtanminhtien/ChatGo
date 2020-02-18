package com.tietha.chatgo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.tietha.chatgo.custom.ActionDialog;
import com.tietha.chatgo.custom.LoadingDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity {

    private final int REQUEST_LOAD_IMAGE = 1212;
    private final int REQUEST_CODE_LOGIN = 2509;
    private ImageButton btnLogout;
    private FirebaseAuth mAuth;
    private ActionDialog confirmLogin, conirmLogout;
    private CircleImageView mAvatar;
    private TextView mName, mDes;
    private LoadingDialog loadingDialog;
    private StorageReference mStorageRef;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        confirmLogin = new ActionDialog(this);
        confirmLogin.setText("Vui lòng đăng nhập để sử dụng các tính năng của ứng dụng!")
                .setButton("Đăng Nhập hoặc Đăng Ký", new ActionDialog.OnClickListener() {
                    @Override
                    public void onClick() {
                        Intent iLogin = new Intent(ProfileActivity.this, LoginActivity.class);
                        startActivityForResult(iLogin, REQUEST_CODE_LOGIN);
                    }
                });
        conirmLogout = new ActionDialog(this);
        conirmLogout.setText("Bạn chắc chắn đăng xuất ứng dụng không?")
                .setButton("Đăng Xuất", new ActionDialog.OnClickListener() {
                    @Override
                    public void onClick() {
                        SignOut();
                    }
                });
        setContentView(R.layout.activity_profile);

        loadingDialog = new LoadingDialog(this);
        initUser();
        setupToolBar();

        btnLogout = findViewById(R.id.imglogout);
        mAvatar = findViewById(R.id.avatar);
        mName = findViewById(R.id.name);
        mDes = findViewById(R.id.des);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iGallery = new Intent();
                iGallery.setType("image/*");
                iGallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(iGallery, "SELECT IMAGE"), REQUEST_LOAD_IMAGE);
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionSignOut();
            }
        });
    }
    
    private void setupToolBar() {
        toolbar = findViewById(R.id.toolBar_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Drawable upArrow =  ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }
    private void ActionSignOut() {
        conirmLogout.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLogin();
    }

    private void initUser() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void SignOut() {
        mAuth.signOut();
        checkLogin();
    }

    private void checkLogin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            confirmLogin.show();
        } else {
            updateUI(currentUser);
        }
    }

    private void updateUI(FirebaseUser currentUser) {
        loadingDialog.show();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String avatarUri = dataSnapshot.child("thumb").getValue(String.class);
                String name = dataSnapshot.child("name").getValue(String.class);
                String des = dataSnapshot.child("des").getValue(String.class);
                loadingDialog.dismiss();
                Picasso.get().load(avatarUri).into(mAvatar);
                mName.setText(name);
                mDes.setText(des);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        OnDesconnect.on(mAuth.getUid());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOAD_IMAGE && resultCode == RESULT_OK) {
            assert data != null;
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setAspectRatio(1, 1).start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                final LoadingDialog waitForUpload = new LoadingDialog(this);
                waitForUpload.show();

                Uri resultUri = result.getUri();
                String imageName = mAuth.getUid();

                File filethumb = new File(resultUri.getPath());
                Bitmap compressedImage = new Compressor.Builder(this)
                        .setMaxWidth(500)
                        .setMaxHeight(500)
                        .setQuality(100)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES).getAbsolutePath())
                        .build()
                        .compressToBitmap(filethumb);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] Xdata = baos.toByteArray();

                //thumb
                final StorageReference thumbRef = mStorageRef.child("thumb").child(imageName + "jpg");
                thumbRef.putBytes(Xdata).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }

                        // Continue with the task to get the download URL
                        return thumbRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(mAuth.getUid())).child("thumb");
                            assert downloadUri != null;
                            myRef.setValue(downloadUri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    waitForUpload.dismiss();
                                }
                            });
                        }
                    }
                });

                //avatar
                final StorageReference riversRef = mStorageRef.child("avatar").child(imageName + "jpg");
                riversRef.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }

                        // Continue with the task to get the download URL
                        return riversRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(mAuth.getUid())).child("avatar");
                            assert downloadUri != null;
                            myRef.setValue(downloadUri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    waitForUpload.dismiss();
                                }
                            });
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}

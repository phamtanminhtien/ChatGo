package com.tietha.chatgo;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.tietha.chatgo.custom.EditTextChat;
import com.tietha.chatgo.model.Message;
import com.tietha.chatgo.util.KeyboardUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private static final int REQUEST_LOAD_IMAGE = 2509;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private String ID;
    private String IDRoom;
    private MaterialToolbar toolbar;
    private CircleImageView mCircleImageViewAvatar;
    private TextView mName;
    private ImageButton mSendImg;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;
    private String avatar;
    private String name;
    private ImageView state;
    final static int TEXT_SEND = 1;
    final static int TEXT_RECEIVE = 2;
    final static int IMG_SEND = 3;
    final static int IMG_RECEIVE = 4;
    final static int STICKER_SEND = 5;
    final static int STICKER_RECEIVE = 6;
    final static int TYPE_MESSAGE_TEXT = 0;
    final static int TYPE_MESSAGE_IMG = 1;
    final static int TYPE_MESSAGE_STICKER = 2;
    Button btnSend;
    EditTextChat edtSend;
    TextView txtnewLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        btnSend = findViewById(R.id.button_chatbox_send);
        edtSend = findViewById(R.id.edittext_chatbox);
        txtnewLine = findViewById(R.id.new_line);
        recyclerView = findViewById(R.id.reyclerview_message_list);
        mSendImg = findViewById(R.id.button_chatbox_img);

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        init();
        setupToolBar();
        updateUI();
        scollView();
    }

    private void scollView() {
        txtnewLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                txtnewLine.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Mess")
                .child(IDRoom)
                .orderByChild("timestamp");
        final FirebaseRecyclerOptions<Message> messs =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Message, ViewHolder>(messs) {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                int layout = R.layout.item_message_text_sent;

                switch (viewType) {
                    case TEXT_SEND:
                        layout = R.layout.item_message_text_sent;
                        break;
                    case TEXT_RECEIVE:
                        layout = R.layout.item_message_text_received;
                        break;
                    case IMG_SEND:
                        layout = R.layout.item_message_img_sent;
                        break;
                    case IMG_RECEIVE:
                        layout = R.layout.item_message_img_received;
                    case STICKER_SEND: // sticker send
                    case STICKER_RECEIVE: //sticker receive
                        break;

                }

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(layout, parent, false);
                return new ViewHolder(view, viewType);
            }

            @Override
            public int getItemViewType(int position) {
                Message mess = getItem(position);
                String from = mess.getFrom();
                int result = TEXT_SEND;
                if (from.equals(mAuth.getUid())) {
                    //send
                    switch (mess.getType()) {
                        case 0:
                            result = TEXT_SEND;
                            break;
                        case 1:
                            result = IMG_SEND;
                            break;
                        case 2:
                            result = STICKER_SEND;
                            break;
                    }
                } else {
                    //received
                    switch (mess.getType()) {
                        case 0:
                            result = TEXT_RECEIVE;
                            break;
                        case 1:
                            result = IMG_RECEIVE;
                            break;
                        case 2:
                            result = STICKER_RECEIVE;
                            break;
                    }
                }
                return result;
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Message model) {
                switch (model.getType()) {
                    case TYPE_MESSAGE_TEXT:
                        holder.setmMess(model.getMess());
                        holder.setmTime(model.getTimestampCreatedLong());
                        break;
                    case TYPE_MESSAGE_IMG:
                        holder.setmImage(model.getImg());
                        holder.setmTime(model.getTimestampCreatedLong());
                        break;
                    case TYPE_MESSAGE_STICKER:
                        break;
                }
                if(position > 0){
                    Message here = ((Message)adapter.getItem(position));
                    Message last = ((Message)adapter.getItem(position-1));
                    if((here.getTimestampCreatedLong() - last.getTimestampCreatedLong()) < 120000 ){
                        holder.removeTime();
                        if(here.getFrom().equals(last.getFrom())){
                            holder.removeAvatar();
                            holder.removeName();
                        }
                    }
                }
            }
        };
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                Message mess = ((Message) adapter.getItem(positionStart));
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                } else if (!mess.getFrom().equals(mAuth.getUid())) {
                    txtnewLine.setVisibility(View.VISIBLE);
                }
                if (mess.getFrom().equals(mAuth.getUid())) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)) {
                    txtnewLine.setVisibility(View.INVISIBLE);
                }
            }
        });
        recyclerView.setAdapter(adapter);
        KeyboardUtil.setKeyboardVisibilityListener(this, new KeyboardUtil.KeyboardVisibilityListener() {
            @Override
            public void onKeyboardVisibilityChanged(boolean keyboardVisible) {
                if (keyboardVisible) {
                    recyclerView.smoothScrollToPosition(adapter.getItemCount());
                }
            }
        });
    }

    private void setupToolBar() {
        toolbar = findViewById(R.id.toolBar_chat);
        mCircleImageViewAvatar = toolbar.findViewById(R.id.avatar_appbar_chat);
        mName = toolbar.findViewById(R.id.name_appbar_chat);
        state = toolbar.findViewById(R.id.state_onl_appbar_chat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void init() {
        Intent thisI = getIntent();
        ID = thisI.getStringExtra("ID");
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        edtSend.setOnImportImgSupportListener(new EditTextChat.OnImportImgSupportListener() {
            @Override
            public void OnImportImgSupport(InputContentInfoCompat inputContentInfo) {
                final DatabaseReference myMessRef = mDatabase.getReference().child("Mess").child(IDRoom);
                final String idKey = myMessRef.push().getKey();
                myMessRef.child(idKey).setValue(new Message(mAuth.getUid(), TYPE_MESSAGE_IMG, "none", inputContentInfo.getLinkUri().toString(), "none", "none", "sent"));
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        if(adapter != null){
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void updateUI() {
        OnDesconnect.on(mAuth.getUid());
        DatabaseReference myBDAuth = mDatabase.getReference().child("Users").child(ID);
        myBDAuth.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                avatar = dataSnapshot.child("thumb").getValue(String.class);
                name = dataSnapshot.child("name").getValue(String.class);
                mName.setText(name);
                Picasso.get().load(avatar).into(mCircleImageViewAvatar);
                Boolean isOnline = dataSnapshot.child("connected").getValue(Boolean.class);
                if (isOnline) {
                    state.setVisibility(View.VISIBLE);
                } else {
                    state.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final DatabaseReference myBDRoomchat = mDatabase.getReference().child("RoomChat").child(mAuth.getUid()).child(ID);
        final DatabaseReference intantBDRoomchat = mDatabase.getReference().child("RoomChat").child(ID).child(mAuth.getUid());

        myBDRoomchat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    IDRoom = myBDRoomchat.push().getKey();
                    myBDRoomchat.child(IDRoom).setValue(true);
                    intantBDRoomchat.child(IDRoom).setValue(true);

                    setOnClickSendImg();
                    setOnClickSend();
                    fetch();
                    adapter.startListening();
                } else {
                    myBDRoomchat.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            IDRoom = dataSnapshot.getKey();

                            setOnClickSendImg();
                            setOnClickSend();
                            fetch();
                            adapter.startListening();
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setOnClickSendImg() {
        mSendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iGallery = new Intent();
                iGallery.setType("image/*");
                iGallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(iGallery, "SELECT IMAGE"), REQUEST_LOAD_IMAGE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOAD_IMAGE && resultCode == RESULT_OK) {
            final DatabaseReference myMessRef = mDatabase.getReference().child("Mess").child(IDRoom);
            final String idKey = myMessRef.push().getKey();
            myMessRef.child(idKey).setValue(new Message(mAuth.getUid(), TYPE_MESSAGE_TEXT, "Loading", "none", "none", "none", "sent"));

            assert data != null;
            Uri imageUri = data.getData();
            final StorageReference riversRef = mStorageRef.child("mess").child( mAuth.getUid()+ID+IDRoom+System.currentTimeMillis() + "jpg");
            riversRef.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                        assert downloadUri != null;
                        myMessRef.child(idKey).setValue(new Message(mAuth.getUid(), TYPE_MESSAGE_IMG, "none", downloadUri.toString(), "none", "none", "sent"));

                    }
                }
            });
        }
    }

    private void setOnClickSend() {
        final DatabaseReference myMessRef = mDatabase.getReference().child("Mess").child(IDRoom);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mess = edtSend.getText().toString();
                String validate = mess.trim();

                if (!validate.isEmpty()) {
                    String idKey = myMessRef.push().getKey();
                    myMessRef.child(idKey).setValue(new Message(mAuth.getUid(), TYPE_MESSAGE_TEXT, validate, "none", "none", "none", "sent"));
                    edtSend.setText("");
                }

            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout root;
        //text
        CircleImageView mAvatar;
        TextView mName;
        TextView mMess;
        TextView mTime;
        //img
        ImageView mImage;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            switch (viewType) {
                case TEXT_SEND:
                    root = itemView.findViewById(R.id.root_list_item);
                    mMess = itemView.findViewById(R.id.text_message_body);
                    mTime = itemView.findViewById(R.id.text_message_time);
                    break;
                case TEXT_RECEIVE:
                    root = itemView.findViewById(R.id.root_list_item);
                    mAvatar = itemView.findViewById(R.id.image_message_profile);
                    Picasso.get().load(avatar).into(mAvatar);
                    mName = itemView.findViewById(R.id.text_message_name);
                    mName.setText(name);
                    mMess = itemView.findViewById(R.id.text_message_body);
                    mTime = itemView.findViewById(R.id.text_message_time);
                    break;
                case IMG_SEND:
                    root = itemView.findViewById(R.id.root_list_item);
                    mImage = itemView.findViewById(R.id.img_message_body);
                    mTime = itemView.findViewById(R.id.text_message_time);
                    break;
                case IMG_RECEIVE:
                    root = itemView.findViewById(R.id.root_list_item);
                    mAvatar = itemView.findViewById(R.id.image_message_profile);
                    Picasso.get().load(avatar).into(mAvatar);
                    mName = itemView.findViewById(R.id.text_message_name);
                    mName.setText(name);
                    mImage = itemView.findViewById(R.id.img_message_body);
                    mTime = itemView.findViewById(R.id.text_message_time);
                    break;
            }

        }

        public void setmImage(final String url) {
            Picasso.get().load(url).into(mImage);
            mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChatActivity.this, ImageViewerActivity.class);
                    intent.putExtra("URL", url);
                    startActivity(intent);
                }
            });
        }

        public void setmMess(String mess) {
            mMess.setText(mess);
        }

        public void setmTime(long time) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            String dateString = formatter.format(new Date(time));
            mTime.setText(dateString);
        }
        public void removeTime(){
            root.removeView(mTime);
        }
        public void removeAvatar(){
            if(mAvatar != null){
            mAvatar.setVisibility(View.INVISIBLE);
            }
        }
        public void removeName(){
            root.removeView(mName);
        }
    }
}

package com.tietha.chatgo.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import com.tietha.chatgo.ChatActivity;
import com.tietha.chatgo.R;
import com.tietha.chatgo.model.User;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;
    private FirebaseAuth mAuth;
    private Context mainCtx;

    public FriendFragment(Context ctx) {
        mainCtx = ctx;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_friend, null);
        recyclerView = root.findViewById(R.id.list_friend);

        linearLayoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        init();
        fetch();
        return root;
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users");
        FirebaseRecyclerOptions<User> users =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, new SnapshotParser<User>() {
                            @NonNull
                            @Override
                            public User parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new User(snapshot.getKey(),
                                        Objects.requireNonNull(snapshot.child("thumb").getValue(String.class)),
                                        Objects.requireNonNull(snapshot.child("name").getValue(String.class)),
                                        Objects.requireNonNull(snapshot.child("des").getValue(String.class)),
                                        Objects.requireNonNull(snapshot.child("connected").getValue(Boolean.class))
                                        );
                            }
                        })
                        .build();
        adapter = new FirebaseRecyclerAdapter<User, ViewHolder>(users) {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_friend_list, parent, false);

                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final User model) {
                if (!model.getId().equals(mAuth.getUid())) {
                    holder.setTxtAvatar(model.getAvatar());
                    holder.setTxtName(model.getName());
                    holder.setTxtDes(model.getDes());
                    holder.setState(model.getState());
                    holder.root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
                            animation1.setDuration(1000);
                            v.startAnimation(animation1);
                            Intent iChat = new Intent(mainCtx, ChatActivity.class);
                            iChat.putExtra("ID", model.getId());
                            startActivity(iChat);
                        }
                    });
                }else{
                    holder.setState(model.getState());
                    holder.setTxtAvatar(model.getAvatar());
                    holder.setTxtName(model.getName() + " ( Me )");
                    holder.setTxtDes(model.getDes());
                    holder.root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
                            animation1.setDuration(1000);
                            v.startAnimation(animation1);
                        }
                    });
                }
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout root;
        TextView txtName;
        TextView txtDes;
        CircleImageView imgAvatar;
        ImageView imgState;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root_list_item);
            txtName = itemView.findViewById(R.id.name_list_item);
            txtDes = itemView.findViewById(R.id.des_list_item);
            imgAvatar = itemView.findViewById(R.id.avatar_list_item);
            imgState = itemView.findViewById(R.id.state_onl);
        }

        public void setState(Boolean state){
            if(state){
                imgState.setVisibility(View.VISIBLE);
            }else{
                imgState.setVisibility(View.INVISIBLE);
            }
        }

        public void setTxtName(String name) {
            txtName.setText(name);
        }

        public void setTxtDes(String des) {
            txtDes.setText(des);
        }

        public void setTxtAvatar(String avatar) {
            Picasso.get().load(avatar).into(imgAvatar);
        }
    }
}

package com.tietha.chatgo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class OnDesconnect {

    public static void on(String id){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myConnectionsRef = database.getReference("Users/"+id+"/connected");
        final DatabaseReference lastOnlineRef = database.getReference("/Users/"+id+"/lastOnline");
        final DatabaseReference connectedRef = database.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    myConnectionsRef.setValue(true);
                    myConnectionsRef.onDisconnect().setValue(false);
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("AAA", "Listener was cancelled at .info/connected");
            }
        });
    }

}

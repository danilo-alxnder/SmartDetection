package com.example.project_mod8;

import android.os.Handler;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GetUserV2 {
    private boolean complete = false;
    private Users result;
    private static final String TAG = "GetUserV2";
    public GetUserV2() {
        result = new Users();
        complete = false;
    }

    public Users getResult() {
        Log.i(TAG, "Start fetching data");
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Rooms");
        ref1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError error) {
            }
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Users res = new Users();
                Log.i(TAG, "On Change, Start fetching data");
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    String userID = messageSnapshot.getKey();
                    if (userID != null) {
                        User user = new User(snapshot.getKey());
                        res.getUserList().add(user);
                    }
                }
                result = res;
                complete = true;
                Log.i(TAG, "finish task, now run");
            }

        });
//        while(!complete){}
//        Log.i(TAG, "finish task 1, now run");
        return result;
    }
}

package com.example.project_mod8;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GetUsers implements Continuation<Void, Task<Users>> {

    @Override
    public Task<Users> then(Task<Void> task) {
        final TaskCompletionSource<Users> tcs = new TaskCompletionSource();
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users/");

        ref1.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onCancelled(DatabaseError error) {
                tcs.setException(error.toException());
            }

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Users res = new Users();

                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    String userID = messageSnapshot.getKey();
                    if (userID != null) {
                        User user = new User(snapshot.getKey());
                        res.getUserList().add(user);
                    }
                }

                tcs.setResult(res);
            }

        });

        return tcs.getTask();
    }

}

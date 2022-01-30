package com.example.project_mod8;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";
    private Users result;
    private boolean complete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        result = new Users();
        complete = false;
        final Button signInButton = (Button) findViewById(R.id.signInButton);
        final EditText editText = (EditText)findViewById(R.id.editTextTextPersonName);

        signInButton.setActivated(false);
//        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
//        ref1.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onCancelled(DatabaseError error) {
//            }
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                Log.i(TAG, "On Change, Start fetching data");
//                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
//                    String userID = messageSnapshot.getKey();
//                    if (userID != null) {
//                        User user = new User(userID);
//                        Log.i(TAG,"User: " + userID);
//
//                        for (DataSnapshot currentRoomSnapShot : messageSnapshot.getChildren())
//                            if (currentRoomSnapShot.getKey().equals("currentRoom")) {
//                                if (!currentRoomSnapShot.getValue(String.class).equals("null"))
//                                    user.setCurrentRoom(currentRoomSnapShot.getValue(String.class));
//                            }
//                        result.getUserList().add(user);
//                    }
//                }
//                complete = true;
//                signInButton.setActivated(true);
//                Log.i(TAG, "finish task, now run");
//            }
//        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestQueue queue = Volley.newRequestQueue(SignInActivity.this);
                final String userID = editText.getText().toString();
                Log.i(TAG, "try to log in with userID: " + userID);
                String url ="https://project-mod8.web.app/login/" + userID;

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                String res = response;
                                Log.i(TAG, "Login res: " + res);

                                if (!res.equals("None")) {
                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("userID", userID);

                                    if (!res.equals("null"))
                                        editor.putString("currentRoom", res);

                                    editor.commit();

                                    Intent intent = new Intent(getBaseContext(), RoomOverview.class);
                                    finish();
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(SignInActivity.this, "Cannot find user with userID " + userID, Toast.LENGTH_LONG).show();
                                }


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

                queue.add(stringRequest);

            }
        });
    }
}
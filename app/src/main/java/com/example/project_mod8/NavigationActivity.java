package com.example.project_mod8;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NavigationActivity extends AppCompatActivity {
    private String roomID;
    private DatabaseReference RoomRef;
    private Room roomFinal;
    private ValueEventListener treeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        final Button navigateButton = (Button) findViewById(R.id.outdoorNavigation);
        navigateButton.setEnabled(false);

        final Button indoorButton = (Button) findViewById(R.id.indoorNavigation);
        indoorButton.setEnabled(false);

        roomID = getIntent().getStringExtra("roomID");
        RoomRef = FirebaseDatabase.getInstance().getReference("Rooms/" + roomID);
        treeValue = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("RoomInfoActivity","Data changed");
                Room room = dataSnapshot.getValue(Room.class);
                roomFinal = room;
                roomFinal.setRoomID(roomID);
                indoorButton.setEnabled(true);
                navigateButton.setEnabled(true);
//                RoomRef.removeEventListener(treeValue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("RoomInfoActivity","remove tree listener");
            }
        };

        RoomRef.addListenerForSingleValueEvent(treeValue);


        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("RoomInfoActivity","geo:(0,0)?q=" + roomFinal.getLocation() + "(" + roomFinal.getRoomID() +")");
                Uri gmmIntentUri = Uri.parse("geo:(0,0)?q=" + roomFinal.getLocation() + "(" + roomFinal.getRoomID() +")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

//                Log.i("RoomInfoActivity","google.navigation:q=" + roomFinal.getLocation() + "&mode=b");
//                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + roomFinal.getLocation() + "&mode=b");
//                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                mapIntent.setPackage("com.google.android.apps.maps");
//                startActivity(mapIntent);
            }
        });

        indoorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), Graph.class);
                intent.putExtra("roomID", roomID);
                startActivity(intent);
            }
        });
    }
}
package com.example.project_mod8;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.Tasks;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button overviewButton = (Button) findViewById(R.id.roomOverviewButton);
        overviewButton.setEnabled(false);
        overviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), RoomOverview.class);
                startActivity(intent);
//                getUserss();
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String highScore = sharedPreferences.getString("userID", "null");
        if (!highScore.equals("null")) {
            TextView textView = (TextView) findViewById(R.id.welcomText);
            textView.setText("Welcome " + highScore);
            overviewButton.setEnabled(true);
        } else {
            Toast.makeText(this, "No userID found, sign in", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent i=new Intent(getBaseContext(),SignInActivity.class);
                    finish();
                    startActivity(i);
                }
            }, 3000);
        }
        Button indoorNavigation = (Button) findViewById(R.id.indoorButton);
        indoorNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), IndoorNavigationActivity.class));
            }
        });
        Button signOutButton = (Button) findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                sharedPreferences.edit().remove("userID").commit();
                if (sharedPreferences.contains("currentRoom"))
                    sharedPreferences.edit().remove("currentRoom").commit();

                Toast.makeText(MainActivity.this, "Signed Out, back to sign in", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent i=new Intent(getBaseContext(),SignInActivity.class);
                        finish();
                        startActivity(i);
                    }
                }, 3000);
            }
        });


        Button graphButton = (Button) findViewById(R.id.graphButton);

        graphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), Graph.class));
            }
        });
    }
}


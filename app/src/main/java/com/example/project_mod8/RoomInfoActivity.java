package com.example.project_mod8;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class RoomInfoActivity extends AppCompatActivity {
    private String roomID;
    private Room roomFinal;
    private String entry;
    private ValueEventListener treeValue;
    private ChildEventListener childListener;
    private DatabaseReference RoomRef;
    private String currentRoomString;
    private static String TAG = "RoomInfoActivity";
    private final static int BICYCLE_SPEED = 18;
    private final static int FLEXIBLE_TIME = 30;
    private final static int PROXIMITY = 100;
    private boolean activity_active;
    private FusedLocationProviderClient client;
    private double latitude, longitude;
    private Button joinRoomButton;
    private Button reserveRoomButton;

    private void checkIsReserved(String userID) {
        RequestQueue queue = Volley.newRequestQueue(RoomInfoActivity.this);

        String url ="https://project-mod8.web.app/isReserve/" + userID;
//                String url ="http://localhost:5000/reserve/" + userID + "/" + roomID + "/" + commuteTime;
        Log.i(TAG, "Is reserve url : " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        if (response.equals("0")) {
                            final Button reserveRoomButton = (Button) findViewById(R.id.reserveRoom);
                            if (currentRoomString != null && !currentRoomString.equals("null")) {
                                Log.i(TAG, "No current room found");
                                reserveRoomButton.setEnabled(false);
                            } else {
                                reserveRoomButton.setEnabled(true);
                            }
                            return;
                        }

                        String[] res1 = response.split("/");
                        String res= res1[1];
                        Log.i(TAG, "Reserve " + res1[0] + "until" + res);

                        if (!res.equals("0")) {

                            Locale locale = new Locale("en", "NL");

                            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale);

                            long milliSeconds= Long.parseLong(res);
                            System.out.println(milliSeconds);

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(milliSeconds);
//                            System.out.println(formatter.format(calendar.getTime()));
                            if (res1[0].equals(roomID)) {
                                roomFinal.setReserveUntil(res);
                                TextView textView = (TextView) findViewById(R.id.Room_info_textView);
                                textView.setText(roomFinal.toStringRoom());
                                joinRoomButton.setEnabled(true);
                            }

                        } else {
                            Toast.makeText(RoomInfoActivity.this, "Cannot reserve your room", Toast.LENGTH_LONG).show();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        queue.add(stringRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_info);
        Log.i("RoomInfoActivity","created");
        TextView textView = (TextView) findViewById(R.id.roomID_TextView);
        roomID = getIntent().getStringExtra("roomID");
        entry = getIntent().getStringExtra("entry");
        activity_active = true;
        client = LocationServices.getFusedLocationProviderClient(this);
        runGps();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String userID = sharedPreferences.getString("userID", "null");
        if (userID == null || userID.equals("null")) {
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

        joinRoomButton = (Button) findViewById(R.id.joinRoomButton);
        joinRoomButton.setEnabled(false);
        reserveRoomButton = (Button) findViewById(R.id.reserveRoom);
        reserveRoomButton.setEnabled(false);

        currentRoomString = sharedPreferences.getString("currentRoom", "null");
        if (currentRoomString != null && !currentRoomString.equals("null")) {
            Log.i(TAG, "No current room found");
            joinRoomButton.setEnabled(false);
        }

        reserveRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RequestQueue queue = Volley.newRequestQueue(RoomInfoActivity.this);

                if (roomFinal == null || roomFinal.getDistance() == null) {
                    Toast.makeText(RoomInfoActivity.this, "room is not ready", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!roomFinal.isOpened()) {
                    Toast.makeText(RoomInfoActivity.this, "room is not open", Toast.LENGTH_LONG).show();
                    return;
                }

                String commuteTime = String.valueOf(Math.round(((roomFinal.getDistance()/1000)/BICYCLE_SPEED)*60) + FLEXIBLE_TIME);
                String url ="https://project-mod8.web.app/reserve/" + userID + "/" + roomID + "/" + commuteTime;
//                String url ="http://localhost:5000/reserve/" + userID + "/" + roomID + "/" + commuteTime;
                Log.i(TAG, "Reserve url : " + url);

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                String res = response;
                                Log.i(TAG, "Reserve res: " + res);

                                if (!res.equals("0")) {

                                    Locale locale = new Locale("en", "NL");

                                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale);

                                    long milliSeconds= Long.parseLong(res);
//                                    System.out.println(milliSeconds);

                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeInMillis(milliSeconds);
//                                    System.out.println(formatter.format(calendar.getTime()));
                                    roomFinal.setReserveUntil(res);
                                    TextView textView = (TextView) findViewById(R.id.Room_info_textView);
                                    textView.setText(roomFinal.toStringRoom());
                                    Toast.makeText(RoomInfoActivity.this, "Reserved until " + formatter.format(calendar.getTime()), Toast.LENGTH_LONG).show();
                                    reserveRoomButton.setEnabled(false);
                                    joinRoomButton.setEnabled(true);
                                } else {
                                    Toast.makeText(RoomInfoActivity.this, "Cannot reserve your room", Toast.LENGTH_LONG).show();
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

        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (roomFinal.getDistance() > PROXIMITY) {
                    Toast.makeText(RoomInfoActivity.this, "Can't join room " + roomID + ", need to be in a 100 meters radius", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!roomFinal.isOpened()) {
                    Toast.makeText(RoomInfoActivity.this, "room is not open", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(RoomInfoActivity.this, "Join " + roomID, Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RoomInfoActivity.this);
                sharedPreferences.edit().putString("currentRoom", roomID).commit();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child("Users").child(userID).child("currentRoom").setValue(roomID);
                databaseReference.child("Rooms").child(roomID).child("currentUser").child(userID).setValue("1");
                joinRoomButton.setEnabled(false);


                RequestQueue queue = Volley.newRequestQueue(RoomInfoActivity.this);

                String url = "https://project-mod8.web.app/cancelReserve/" + userID;
//                String url ="http://localhost:5000/reserve/" + userID + "/" + roomID + "/" + commuteTime;
                Log.i(TAG, "Is reserve url : " + url);


                roomFinal.setReserveUntil(null);
                TextView textView = (TextView) findViewById(R.id.Room_info_textView);
                textView.setText(roomFinal.toStringRoom());

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                if (response.equals("0")) {
//                                    currentReserveTextView.setText("No reservation")
                                    Toast.makeText(RoomInfoActivity.this, "Fail to cancel reservation", Toast.LENGTH_LONG).show();
                                    return;
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RoomInfoActivity.this, "Fail to cancel reservation", Toast.LENGTH_LONG).show();
                    }
                });
                queue.add(stringRequest);

            }
        });



        Log.i("RoomInfoActivity",roomID + " " + entry);

        if (entry == null || entry.equals("noti")) {

            Log.i("RoomInfoActivity","create noti in Shared Prefs");

//            SharedPreferences sharedPreferences
//                    = getSharedPreferences("MySharedPref",
//                    MODE_PRIVATE);
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor myEdit
                    = sharedPreferences.edit();

            // Storing the key and its value
            // as the data fetched from edittext
            Random random = new Random();
            myEdit.putString("cancelOverview","true" + random.nextInt(10000000));
            // Once the changes have been made,
            // we need to commit to apply those changes made,
            // otherwise, it will throw an error
            myEdit.commit();
        }

        textView.setText(roomID);
        RoomRef = FirebaseDatabase.getInstance().getReference("Rooms/" + roomID);

        Button overviewButton = (Button) findViewById(R.id.OverviewButton);
        overviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (entry == null) {
                    Intent intent = new Intent(getBaseContext(), RoomOverview.class);
                    startActivity(intent);
                    finish();
                } else if (entry.equals("enterViaText")) {
                    Log.i("RoomInfoActivity","finishActivity");
                    finish();
                } else if (entry.equals("noti")) {
                    Intent intent = new Intent(getBaseContext(), RoomOverview.class);
                    startActivity(intent);
                    finish();
                }
            }
        });


        Button navigateButton = (Button) findViewById(R.id.navigate_room_button);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), NavigationActivity.class);
                intent.putExtra("roomID", roomID);
                startActivity(intent);
            }
        });
        treeValue = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("RoomInfoActivity","Data changed");
                Room room = dataSnapshot.getValue(Room.class);
                roomFinal = room;
                checkIsReserved(userID);
                roomFinal.setRoomID(roomID);
                TextView textView = (TextView) findViewById(R.id.Room_info_textView);
                textView.setText(roomFinal.toStringRoom());
//                RoomRef.removeEventListener(treeValue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("RoomInfoActivity","remove tree listener");
            }
        };

        RoomRef.addListenerForSingleValueEvent(treeValue);


        childListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i("RoomInfoActivity","onChildAdded");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i("RoomInfoActivity","onChildChanged");
                String roomID = dataSnapshot.getRef().getParent().getKey();
                if (roomID != null) {
                    if (dataSnapshot.getKey() == null) return;
                    switch (dataSnapshot.getKey()) {
                        case "lastUpdate":
                            roomFinal.setLastUpdate(dataSnapshot.getValue(String.class));
                            break;
                        case "lightCondition":
                            roomFinal.setLightCondition(dataSnapshot.getValue(String.class));
                            break;
                        case "location":
                            roomFinal.setLocation(dataSnapshot.getValue(String.class));
                            break;
                        case "peopleCount":
                            roomFinal.setPeopleCount(dataSnapshot.getValue(int.class));
                            break;
                        case "reserved":
                            roomFinal.setReserved(dataSnapshot.getValue(String.class));
                            break;
                        case "soundLevel":
                            roomFinal.setSoundLevel(dataSnapshot.getValue(Double.class));
                            break;
                    }
                    TextView textView = (TextView) findViewById(R.id.Room_info_textView);
                    textView.setText(roomFinal.toStringRoom());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.i("RoomInfoActivity","onChildRemoved");

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i("RoomInfoActivity","onChildMoved");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("RoomInfoActivity","onCancelled");

            }


        };
        RoomRef.addChildEventListener(childListener);
    }

    private void runGps()
    {
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {

                if (!activity_active) {
                    Log.i("RoomInfoActivity","thread gps ended");

                    return;
                }
                getUserLocation();

                handler.postDelayed(this, 5000);
            }
        });
    }

    private void getUserLocation()
    {
        try {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(2000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult == null) {
                        return;
                    }
                    Location final_location = locationResult.getLastLocation();
                    latitude = final_location.getLatitude();
                    longitude = final_location.getLongitude();
//                    Log.i("RoomInfoActivity","update location");

                    if (roomFinal != null) {
                        String[] roomLocation = roomFinal.getLocation().split(",");
                        double distance = calculateDistance(latitude, longitude, Double.parseDouble(roomLocation[0]), Double.parseDouble(roomLocation[1]), "K");
                        roomFinal.setDistance(distance*1000);
                        TextView textView = (TextView) findViewById(R.id.Room_info_textView);
                        textView.setText(roomFinal.toStringRoom());
                    }
                }
            };
            client.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activity_active = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activity_active = false;
        Log.i("RoomInfoActivity","remove child listener");
        RoomRef.removeEventListener(childListener);
    }
}
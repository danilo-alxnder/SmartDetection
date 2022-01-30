package com.example.project_mod8;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import android.content.SharedPreferences;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RoomOverview extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 467;
    private boolean locationPermissionGranted = false;
    private static String TAG = "RoomOverview";

    private DatabaseReference mDatabase;
    private Map<String, DatabaseReference> roomsData;
    private Map<String, ChildEventListener> childEventListenerMap;
    private Map<String, Room> roomMap;
    private Map<String, TextView> roomTextViews;
    private double latitude, longitude;
    private FusedLocationProviderClient client;
    private Room currentRoom;
    private int currentNotiID;
    public boolean activity_active = false;
    private String currentRoomString;
    private TextView currentRoomTextView;
    private TextView currentReserveTextView;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private Button leaveRoomButton;
    private Button cancelReserveButton;
    private Button sortButton;
    private String userID;
    private LinearLayout linearLayout;
    private LinearLayout linearListLayout;
    private List<Room> roomList;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_overview2);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userID = sharedPreferences.getString("userID", "null");
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
        linearLayout = (LinearLayout) findViewById(R.id.room_overview_elem_linear);
        linearListLayout = (LinearLayout) findViewById(R.id.room_overview_list_linear);
        client = LocationServices.getFusedLocationProviderClient(this);
        roomList = new ArrayList<>();
        activity_active = true;
        createNotificationChannel();
        TextView tv = new TextView(this);
        tv.setText("Room List");
        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setTextColor(Color.RED);
        tv.setTextSize(30);
        linearLayout.addView(tv);
        currentRoomString = sharedPreferences.getString("currentRoom", "null");
        if (currentRoomString == null || currentRoomString.equals("null")) {
            Log.i(TAG, "No current room found");
            currentRoomTextView = new TextView(this);
            currentRoomTextView.setText("No current room found");
            currentRoomTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//        tv.setGravity(Gravity.CENTER);
            currentRoomTextView.setTypeface(tv.getTypeface(), Typeface.BOLD);
            currentRoomTextView.setTextSize(15);
            linearLayout.addView(currentRoomTextView);

        } else {
            Log.i(TAG, "set string current room: " + currentRoomString);
            currentRoomTextView = new TextView(this);
            currentRoomTextView.setText("Current room: " + currentRoomString);
            currentRoomTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//        tv.setGravity(Gravity.CENTER);
            currentRoomTextView.setTypeface(tv.getTypeface(), Typeface.BOLD);
            currentRoomTextView.setTextSize(15);
            linearLayout.addView(currentRoomTextView);
        }

        leaveRoomButton = new Button(this);
        leaveRoomButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        leaveRoomButton.setText("Leave current room");
        leaveRoomButton.setId(123);
        linearLayout.addView(leaveRoomButton);

        if (currentRoomString == null || currentRoomString.equals("null")) {
            leaveRoomButton.setEnabled(false);
        }

        leaveRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child("Rooms").child(currentRoom.getRoomID()).child("currentUser").child(userID).removeValue();
                currentRoom = null;
                currentRoomString = "null";
                currentRoomTextView.setText("No current room found");
                databaseReference.child("Users").child(userID).child("currentRoom").setValue("null");

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RoomOverview.this);
                sharedPreferences.edit().remove("currentRoom").commit();
                leaveRoomButton.setEnabled(false);
            }
        });

        currentReserveTextView = new TextView(this);
        currentReserveTextView.setText("No reservation");
        currentReserveTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//        tv.setGravity(Gravity.CENTER);
        currentReserveTextView.setTypeface(tv.getTypeface(), Typeface.BOLD);
        currentReserveTextView.setTextSize(12);
        linearLayout.addView(currentReserveTextView);

        cancelReserveButton = new Button(this);
        cancelReserveButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        cancelReserveButton.setText("Cancel Reservation");
        cancelReserveButton.setId(1234);
        linearLayout.addView(cancelReserveButton);
        cancelReserveButton.setEnabled(false);

        cancelReserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestQueue queue = Volley.newRequestQueue(RoomOverview.this);

                String url = "https://project-mod8.web.app/cancelReserve/" + userID;
//                String url ="http://localhost:5000/reserve/" + userID + "/" + roomID + "/" + commuteTime;
                Log.i(TAG, "Is reserve url : " + url);

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                if (response.equals("0")) {
//                                    currentReserveTextView.setText("No reservation")
                                    Toast.makeText(RoomOverview.this, "Fail to cancel reservation", Toast.LENGTH_LONG).show();

                                    cancelReserveButton.setEnabled(true);
                                    return;
                                }
                                cancelReserveButton.setEnabled(false);
                                currentReserveTextView.setText("No reservation");

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RoomOverview.this, "Fail to cancel reservation", Toast.LENGTH_LONG).show();
                    }
                });
                queue.add(stringRequest);
            }
        });


        sortButton = new Button(this);
        sortButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        sortButton.setText("Sort Rooms");
        sortButton.setId(12345);
        linearLayout.addView(sortButton);
//        sortButton.setEnabled(false);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestQueue queue = Volley.newRequestQueue(RoomOverview.this);

                String url = "https://project-mod8.web.app/getAvailability";
//                String url ="http://localhost:5000/reserve/" + userID + "/" + roomID + "/" + commuteTime;
                Log.i(TAG, "sort url : " + url);

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                            Log.i(TAG, response);
                                try {
                                    JSONObject reader = new JSONObject(response);
                                    JSONArray listRoom  = reader.getJSONArray("list");
                                    for (int i = 0; i < listRoom.length(); i++) {
                                        JSONObject room = listRoom.getJSONObject(i);
                                        String roomID = room.getString("room_id");
                                        String slots = room.getString("slots");
                                        Room tmp = roomMap.get(roomID);
                                        if (tmp != null)
                                            tmp.setAvailableSlot(Integer.parseInt(slots));
                                    }
                                    sortViewList();

                                } catch (JSONException e) {
                                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                                }


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RoomOverview.this, "Fail to cancel reservation", Toast.LENGTH_LONG).show();
                    }
                });
                queue.add(stringRequest);
            }
        });


        roomsData = new HashMap<>();
        roomMap = new HashMap<>();
        roomTextViews = new HashMap<>();
        childEventListenerMap = new HashMap<>();

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                Log.i("RoomOverview","key change: " + key + "/" + prefs.getString("cancelOverview", "null"));
//                prefs.getString("entry", "null");
                if (key.equals("cancelOverview") && prefs.getString("cancelOverview", "null").contains("true")) {
                    finish();
                    return;
                }
            }
        };

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        
        final boolean[] done = {false};
        mDatabase = FirebaseDatabase.getInstance().getReference("Rooms");

        final ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("RoomOverview","Hello");
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String roomID = messageSnapshot.getKey();
                    if (roomID != null) {
                        Room room = messageSnapshot.getValue(Room.class);
                        if (room != null) {
                            room.setRoomID(roomID);
                            roomMap.put(roomID, room);
                            roomList.add(room);
                            roomsData.put(roomID, mDatabase);
                            Log.i("RoomOverview",room.toString());
                            addRoom(linearListLayout, room);
                            if (room.getRoomID().contains(currentRoomString)) {
                                Log.i(TAG, "set object current room");
                                currentRoom = room;
                            }
                        }
                    }
                }
                mDatabase.removeEventListener(this);

                for (String roomID: roomsData.keySet()) {
                    DatabaseReference tmp = FirebaseDatabase.getInstance().getReference("Rooms/" + roomID);
                    Log.i("RoomOverview","Rooms/" + roomID);
                    ChildEventListener tmpp = new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Log.i("RoomOverview","onChildAdded");
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Log.i("RoomOverview","onChildChanged");
                            String roomID = dataSnapshot.getRef().getParent().getKey();
                            if (roomID != null) {
                                Room room = roomMap.get(roomID);
                                if (dataSnapshot.getKey() == null) return;
                                switch (dataSnapshot.getKey()) {
                                    case "lastUpdate":
                                        room.setLastUpdate(dataSnapshot.getValue(String.class));
                                        break;
                                    case "lightCondition":
                                        room.setLightCondition(dataSnapshot.getValue(String.class));
                                        break;
                                    case "location":
                                        room.setLocation(dataSnapshot.getValue(String.class));
                                        break;
                                    case "peopleCount":
                                        room.setPeopleCount(dataSnapshot.getValue(int.class));
                                        break;
                                    case "reserved":
                                        room.setReserved(dataSnapshot.getValue(String.class));
                                        break;
                                    case "soundLevel":
                                        room.setSoundLevel(dataSnapshot.getValue(Double.class));
                                        break;
                                }
                                roomTextViews.get(roomID).setText(room.toStatusStringRoom());
                            }
                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                            Log.i("RoomOverview","onChildRemoved");

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Log.i("RoomOverview","onChildMoved");

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.i("RoomOverview","onCancelled");

                        }
                    };
                    tmp.addChildEventListener(tmpp);
                    roomsData.put(roomID, tmp);
                    childEventListenerMap.put(roomID, tmpp);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        mDatabase.addValueEventListener(valueEventListener);

        getLocationPermission();
        runGps();
        runChecking();
    }

    private void checkIsReserved(String userID) {
        RequestQueue queue = Volley.newRequestQueue(RoomOverview.this);

        String url = "https://project-mod8.web.app/isReserve/" + userID;
//                String url ="http://localhost:5000/reserve/" + userID + "/" + roomID + "/" + commuteTime;
        Log.i(TAG, "Is reserve url : " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        if (response.equals("0")) {
                            currentReserveTextView.setText("No reservation");
                            cancelReserveButton.setEnabled(false);
                            return;
                        }

                        String[] res1 = response.split("/");
                        String res = res1[1];
                        Log.i(TAG, "Reserve " + res1[0] + "until" + res);

                        if (!res.equals("0")) {

                            Locale locale = new Locale("en", "NL");

                            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale);

                            long milliSeconds = Long.parseLong(res);
                            System.out.println(milliSeconds);

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(milliSeconds);
//                            System.out.println(formatter.format(calendar.getTime()));
//                            Toast.makeText(RoomOverview.this, "Reserved until " + formatter.format(calendar.getTime()), Toast.LENGTH_LONG).show();
                            currentReserveTextView.setText("Reserved " + res1[0] + " until " + formatter.format(calendar.getTime()));
                            cancelReserveButton.setEnabled(true);
                        } else {
//                            Toast.makeText(RoomOverview.this, "Cannot reserve your room", Toast.LENGTH_LONG).show();
                            currentReserveTextView.setText("No reservation");
                            cancelReserveButton.setEnabled(false);
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(stringRequest);
    }



    private void addNoti(String title, String subject, String body) {

        Intent intent = new Intent(this, RoomInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("entry", "noti");
        intent.putExtra("roomID", currentRoom.getRoomID());

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
//        Random random = new Random();
//        int tmp = 0;
//        do {
//            tmp = random.nextInt(100000);
//        } while(tmp != currentNotiID);
        notificationManager.notify(12, builder.build());
    }

    private static final String CHANNEL_ID = "PJM8";

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "projectmod8";
            String description = "projcet mod 8";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void addRoom(LinearLayout linearLayout, Room room) {
        TextView tv = new TextView(this);
        tv.setText(room.getRoomID() + ":");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(20,40,0,0);
        tv.setLayoutParams(layoutParams);
        tv.setGravity(Gravity.LEFT);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setTextColor(Color.BLUE);
        tv.setTextSize(20);
        linearLayout.addView(tv);

        tv = new TextView(this);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(Map.Entry<String, TextView> entry: roomTextViews.entrySet()) {
                    if (entry.getValue().equals(view)) {
                        Log.i("RoomOverview","Clicked " + entry.getKey());
                        Intent intent = new Intent(getBaseContext(), RoomInfoActivity.class);
                        intent.putExtra("roomID", entry.getKey());
                        intent.putExtra("entry", "enterViaText");
//                        startActivityForResult(intent, 101);
                        startActivity(intent);
                    }
                }
            }
        });
        tv.setText(room.toStatusStringRoom());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(20,0,0,0);
        tv.setLayoutParams(layoutParams);
        tv.setGravity(Gravity.LEFT);
//        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(15);
        roomTextViews.put(room.getRoomID(), tv);
        linearLayout.addView(tv);

    }

    private void runGps()
    {
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {

                if (!activity_active) {
                    return;
                }
                Log.i(TAG,"get user location");
                getUserLocation();

                handler.postDelayed(this, 5000);
            }
        });
    }

    private void runChecking() {

        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {


                if (currentRoom == null) {
                    Log.i(TAG, "Checking room " + currentRoomString + "/" + null);

                } else {
                    Log.i(TAG, "Checking room " + currentRoomString + "/" + currentRoom.toString());
                }
                if (!activity_active) {
                    Log.i("RoomOverview", "end runChecking");
                    return;
                }

                if (currentRoom != null && !currentRoom.inhabitable()) {
                    Log.i("RoomOverview","Inhabitable");
                    addNoti("Warning!!! Inhabitable", "Inhabitable", currentRoom.getReason());
                }
                checkIsReserved(userID);

                handler.postDelayed(this, 10000);
            }
        });
    }

    private void setDistancesForRooms()
    {
        for (Map.Entry<String, Room> entry : roomMap.entrySet()) {
            Room room = entry.getValue();
            String[] roomLocation = room.getLocation().split(",");
            double distance = calculateDistance(latitude, longitude, Double.parseDouble(roomLocation[0]), Double.parseDouble(roomLocation[1]), "K");
            room.setDistance(distance*1000);
            roomTextViews.get(room.getRoomID()).setText(room.toStatusStringRoom());
        }
    }

    private void sortViewList() {
        Collections.sort(roomList, new Comparator<Room>() {
            @Override
            public int compare(Room a1, Room a2) {
                if (a1.getRoomID().equals(a2.getRoomID())) {
                    return 0;
                }

                boolean isA1Possible = a1.isOpened() && a1.inhabitable() && a1.isAvailable();
                boolean isA2Possible = a2.isOpened() && a2.inhabitable() && a2.isAvailable();

                if (isA1Possible && !isA2Possible) {
                    return 1;
                }

                if (!isA1Possible && isA2Possible) {
                    return -1;
                }

                if (a1.getDistance() == null || a2.getDistance() == null) {
                    return 0;
                }

                return a1.getDistance().compareTo(a2.getDistance());
            }
        });

        linearListLayout.removeAllViews();
        for(Room room: roomList) {
            addRoom(linearListLayout, room);
        }
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
                        Log.i(TAG, "updated location: null");
                        return;
                    }
                    Location final_location = locationResult.getLastLocation();
                    latitude = final_location.getLatitude();
                    longitude = final_location.getLongitude();
                    setDistancesForRooms();
                    Log.i(TAG, "updated location: " + latitude + "," + longitude);

                }
            };
            client.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("RoomOverview", "destroy");
        activity_active = false;

        for(String roomID: roomMap.keySet()) {
            if (roomsData.containsKey(roomID) && childEventListenerMap.containsKey(roomID))
                Log.i("RoomOverview", "removing " + roomID + " childEvenListener");

            roomsData.get(roomID).removeEventListener(childEventListenerMap.get(roomID));
        }
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(listener );
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("RoomOverview", "resume");
        activity_active = true;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentRoomString = sharedPreferences.getString("currentRoom", "null");
        if (currentRoomString == null || currentRoomString.equals("null")) {
            currentRoomTextView.setText("No current room found");
            leaveRoomButton.setEnabled(false);
        } else {
            currentRoomTextView.setText("Current room: " + currentRoomString);
            if (roomsData.containsKey(currentRoomString))
                Log.i(TAG, "assigned room");
                currentRoom = roomMap.get(currentRoomString);
            leaveRoomButton.setEnabled(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("RoomOverview", "pause");
//        activity_active = false;
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
    public void onBackPressed() {
        super.onBackPressed();
    }
}
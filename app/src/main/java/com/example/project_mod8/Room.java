package com.example.project_mod8;

import com.google.firebase.database.IgnoreExtraProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

@IgnoreExtraProperties
public class Room {
    private String roomID;
    private Double distance;
    private Double temperature;
    private String reserved;
    private String lastUpdate;
    private String lightCondition;
    private String location;
    private int peopleCount;

    public Room(String roomID, Double distance, Double temperature, String reserved, String lastUpdate, String lightCondition, String location, int peopleCount, int capacity, Double soundLevel, String reserveUntil) {
        this.roomID = roomID;
        this.distance = distance;
        this.temperature = temperature;
        this.reserved = reserved;
        this.lastUpdate = lastUpdate;
        this.lightCondition = lightCondition;
        this.location = location;
        this.peopleCount = peopleCount;
        this.capacity = capacity;
        this.soundLevel = soundLevel;
        this.opened = System.currentTimeMillis() - Long.parseLong(lastUpdate) <  OPEN_THRESHOLD*60*1000;
        this.reserveUntil = reserveUntil;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    private int capacity;
    private Double soundLevel;
    private boolean opened;
    private String reserveUntil;
    private static final int OPEN_THRESHOLD = 5;
    public String getReserveUntil() {
        return reserveUntil;
    }

    public void setReserveUntil(String reserveUntil) {
        this.reserveUntil = reserveUntil;
    }

    public Double getDistance() {
        return distance;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Room(String roomID, Double distance, Double temperature, String reserved, String lastUpdate, String lightCondition, String location, int peopleCount, Double soundLevel) {
        this.roomID = roomID;
        this.distance = distance;
        this.temperature = temperature;
        this.reserved = reserved;
        this.lastUpdate = lastUpdate;
        this.lightCondition = lightCondition;
        this.location = location;
        this.peopleCount = peopleCount;
        this.soundLevel = soundLevel;
        this.opened = System.currentTimeMillis() - Long.parseLong(lastUpdate) <  OPEN_THRESHOLD*60*1000;
    }

    public Room(String roomID, String reserved, String lastUpdate, String lightCondition, String location, int peopleCount, Double soundLevel) {
        this.roomID = roomID;
        this.reserved = reserved;
        this.lastUpdate = lastUpdate;
        this.lightCondition = lightCondition;
        this.location = location;
        this.peopleCount = peopleCount;
        this.soundLevel = soundLevel;
        this.opened = System.currentTimeMillis() - Long.parseLong(lastUpdate) <  OPEN_THRESHOLD*60*1000;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }


    public Room() {
    }

    public Room(String reserved, String lastUpdate, String lightCondition, String location, int peopleCount, Double soundLevel) {
        this.reserved = reserved;
        this.lastUpdate = lastUpdate;
        this.lightCondition = lightCondition;
        this.location = location;
        this.peopleCount = peopleCount;
        this.soundLevel = soundLevel;
        this.opened = System.currentTimeMillis() - Long.parseLong(lastUpdate) <  OPEN_THRESHOLD*60*1000;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
        this.opened = System.currentTimeMillis() - Long.parseLong(lastUpdate) <  OPEN_THRESHOLD*60*1000;
    }

    public String getLightCondition() {
        return lightCondition;
    }

    public void setLightCondition(String lightCondition) {
        this.lightCondition = lightCondition;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(int peopleCount) {
        this.peopleCount = peopleCount;
    }

    public Double getSoundLevel() {
        return soundLevel;
    }

    public void setSoundLevel(Double soundLevel) {
        this.soundLevel = soundLevel;
    }

    public void setDistance(Double distance) {
        BigDecimal bd = new BigDecimal(Double.toString(distance));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        this.distance = bd.doubleValue();
    }

    public boolean inhabitable() {
        boolean inhabitable = true;

        if (
                soundLevel > 80 || lightCondition.equals("Dark") || temperature < 19 || temperature > 26 //extreme conditions
                || (lightCondition.equals("Dim") && soundLevel > 65) //if both sound and light levels are average
        ) {
            inhabitable = false;
        }

        return inhabitable;
    }

    public String getReason() {
        String reason = "";

        if (lightCondition.equals("Dark") || lightCondition.equals("Dim")) {
            reason += "Light";
        }
        if (soundLevel > 65) {
            if (reason.equals("")) {
                reason += "Sound";
            } else {
                reason += ", sound";
            }
        }
        if (temperature < 19 || temperature > 26) {
            if (reason.equals("")) {
                reason += "Temperature";
            } else {
                reason += "and temperature";
            }
        }

        return reason + " conditions are not suitable";
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomID='" + roomID + '\'' +
                ", reserved='" + reserved + '\'' +
                ", lastUpdate='" + lastUpdate + '\'' +
                ", lightCondition=" + lightCondition +
                ", location='" + location + '\'' +
                ", peopleCount=" + peopleCount +
                ", soundLevel=" + soundLevel +
                ", temperature=" + temperature +
                '}';
    }

    public boolean isOpened() {
        this.opened = System.currentTimeMillis() - Long.parseLong(lastUpdate) <  OPEN_THRESHOLD*60*1000;
        return opened;
    }

    public String toStringRoom() {

        return
                "lastUpdate =" + toDate(lastUpdate) +  "\n" +
                "lightCondition =" + lightCondition + "\n" +
                "location =" + location +  "\n" +
                "capacity =" + capacity +  "\n" +
                "peopleCount =" + peopleCount + "\n" +
                "soundLevel =" + soundLevel + "\n" +
                "temperature=" + temperature + "\n" +
                ((distance != null && !Double.isNaN(distance))? ("distance=" + distance + "\n"):"") +
                ((reserveUntil != null && !reserveUntil.equals(""))? ("reserved until=" + toDate(reserveUntil) + "\n"):"");
    }

    private String toDate(String milli) {
        Locale locale = new Locale("en", "NL");

        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale);

        long milliSeconds= Long.parseLong(milli);
//        System.out.println(milliSeconds);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
//        System.out.println(formatter.format(calendar.getTime()));
        return formatter.format(calendar.getTime());
    }

    public Integer getAvailableSlot() {
        return availableSlot;
    }

    public boolean isAvailable() {
        return availableSlot != null && availableSlot > 0;
    }

    public void setAvailableSlot(Integer availableSlot) {
        this.availableSlot = availableSlot;
    }

    private Integer availableSlot;
    public String toStatusStringRoom() {
//        boolean opened = System.currentTimeMillis() - Long.parseLong(lastUpdate) <  OPEN_THRESHOLD*60*1000;
        boolean inhabitable = false;

        return "opened = " + opened + " \n" +
                               "inhabitable = " + inhabitable + " \n" +
                                ((distance != null && !Double.isNaN(distance))? ("distance=" + distance + "\n"):"") +
                                ((availableSlot != null)? ("Slots =" + availableSlot + "\n"):"");
    }
}

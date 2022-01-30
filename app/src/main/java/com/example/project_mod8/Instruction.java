package com.example.project_mod8;

public class Instruction {
    public enum InsType {TURN, WALK, FIN}
    private InsType type;
    private double angle;
    private double meter;

    public Instruction(InsType type, double angle, double meter) {
        this.type = type;
        this.angle = angle;
        this.meter = meter;
    }

    public InsType getType() {
        return type;
    }

    public void setType(InsType type) {
        this.type = type;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getMeter() {
        return meter;
    }

    public void setMeter(double meter) {
        this.meter = meter;
    }
}

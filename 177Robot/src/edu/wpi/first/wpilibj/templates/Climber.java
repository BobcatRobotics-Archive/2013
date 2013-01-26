/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 * @author Robotics
 */
public class Climber extends Thread {
    //constants 
    private static final int DRIVING = 0;
    private static final int RETRACTING = 1;
    private static final int EXTENDING = 2;
    private static final double throttle = 0.5;
    private static final double stagelength = 36;
    private static final double encoderThresh = 2;
    
    Team177Robot robot;
    private int state = DRIVING;
    private boolean enabled = true;

    /* Limit Switches */
    private DigitalInput lowerlimit = new DigitalInput(8);
    private DigitalInput upperlimit = new DigitalInput(9);
    
    Climber(Team177Robot robot) {
        this.robot = robot;
    }
    
    public void run() {
        
        while (true) {
            SmartDashboard.putBoolean("Climber Enable", enabled);
            if (enabled) {
                switch (state) {
                    case RETRACTING:
                        SmartDashboard.putString("Climber State", "Retracting");
                        Retracting();
                    case EXTENDING:
                        SmartDashboard.putString("Climber State", "Extending");
                        Extending();
                    case DRIVING:
                        SmartDashboard.putString("Climber State", "Driving");
                        Driving();
                }
            } else {
                robot.drive.tankDrive(0, 0);
                //TODO: Fire pneumatic brake
            }
            
            /* Sleep for a while */
            try {
                Thread.sleep(10);
            } catch (Exception e) {}
        }
    }
    
    private void Extending() {
        robot.drive.tankDrive(throttle, throttle);
        double maxdistance = Math.max(robot.locator.getLeftRaw(), robot.locator.getRightRaw());
        if((Math.abs(maxdistance-stagelength) < encoderThresh) || (!upperlimit.get())) {
            state = RETRACTING;
        }
    }
    
    private void Retracting() {
        robot.drive.tankDrive(-throttle, -throttle);
        double mindistance = Math.min(robot.locator.getLeftRaw(), robot.locator.getRightRaw());
        if((mindistance < encoderThresh) || (!lowerlimit.get())) {
            state = EXTENDING;
        }
        
    }
    
    private void Driving() {
        //TODO: Check for pyramid contact
        robot.locator.Reset();
        state = EXTENDING;
    }
    
    public synchronized void enable(boolean e) {
        enabled = e;
    }
}

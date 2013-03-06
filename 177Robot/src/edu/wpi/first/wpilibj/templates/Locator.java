/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;

/**
 * Keep track of robots location in x,y grid with 0,0 being starting location
 * @author SCHROED
 */
public class Locator {
    
    private final EnhancedGyro headingGyro;
    private final Encoder leftEncoder;
    private final Encoder rightEncoder;
    private final UpdateLocation updateLocation;
    
    public Locator(int leftEncoderA, int leftEncoderB, int rightEncoderA, int rightEncoderB, int gyroChannel) {
        headingGyro = new EnhancedGyro(gyroChannel);
        leftEncoder = new Encoder(leftEncoderA, leftEncoderB);
        rightEncoder = new Encoder(rightEncoderA, rightEncoderB);
                
        //Set Default Values
        leftEncoder.setDistancePerPulse(1.0);
        rightEncoder.setDistancePerPulse(1.0);
    
        leftEncoder.start();
        rightEncoder.start();
        updateLocation = new UpdateLocation();       
        
            
        LiveWindow.addSensor("Locater", "left Encoder", leftEncoder);
        LiveWindow.addSensor("Locater", "right Encoder", rightEncoder);
        LiveWindow.addSensor("Locater", "Gyro", headingGyro);
            
    }
    
    public void start() {
        updateLocation.start();
    }
    
    public void startClimberMode() {
        //Stop the locater thread and reset the encoder for use by the climber.
        if(updateLocation.isEnabled()) {
            updateLocation.enable(false);
        }            
        rightEncoder.reset();
        leftEncoder.reset();
    }
    
    public void startLocatorMode() {
        //Start the locater thread
        if(!updateLocation.isEnabled()) {        
            updateLocation.enable(true);
            Reset();
        }
    }
    
    public double GetHeading() {
        return updateLocation.heading;
    }
    
    public double GetX() {
        return updateLocation.x;
    }
    
    public double GetY() {
        return updateLocation.y;
    }
    
    /* Set x/y location to 0,0 and heading to 0 degrees */
    public void Reset() {
        updateLocation.Reset();   
    }
    
    public double getLeftRaw() {
        return leftEncoder.getDistance();
    }
    
    public double getRightRaw() {
        return rightEncoder.getDistance();
    }
    
    public void setDistancePerPulse(float leftDPP, float rightDPP) {
        leftEncoder.setDistancePerPulse(leftDPP);
        rightEncoder.setDistancePerPulse(rightDPP);
    }
    
    private class UpdateLocation extends Thread {

        public double x;
        public double y;
        public double heading;
        private boolean ResetFlag = false;
        private boolean enabled = true;

        UpdateLocation() {
            x = 0;
            y = 0;    
            heading = 0;
            headingGyro.reset();
        }
        
        public void Reset() {
            ResetFlag = true;
        }
        
        public synchronized void enable(boolean e) {
            enabled = e;
        }
        
        public synchronized boolean isEnabled() {
            return enabled;
        }
        
        public void run() {       
            double deltax, deltay;
            double distance;
            double lastLeft = 0;
            double lastRight = 0;
            double left, right;
            
            while (true) {
                if (enabled) {
                    if (ResetFlag) {
                        x = 0;
                        y = 0;
                        headingGyro.reset();
                        ResetFlag = false;
                    }
                    left = leftEncoder.getDistance();
                    right = rightEncoder.getDistance();
                    /* Average the two encoder values */
                    /* TODO - posiably add error checking to detect a failed encoder and ignore it */
                    distance = ((left - lastLeft) + (right - lastRight)) / 2.0;
                    //distance = (right-lastRight ); //left encoder doesn't work on 2012 drivetrain                               
                    heading = headingGyro.GetHeading();

                    /* Do fancy trig stuff */
                    deltax = distance * Math.cos(Math.toRadians(heading));
                    deltay = distance * Math.sin(Math.toRadians(heading));

                    /* Update Location */
                    x += deltax;
                    y -= deltay;

                    /* Update history variables */
                    lastLeft = left;
                    lastRight = right;

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                    //System.out.println("Left: " + left + " Right: " + right);
                }
            }
        }    
    }
}

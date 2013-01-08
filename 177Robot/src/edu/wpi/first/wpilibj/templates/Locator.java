/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Encoder;

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
    }
    
    public void start() {
        updateLocation.start();
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
    
    public void setDistancePerPulse(float leftDPP, float rightDPP) {
        leftEncoder.setDistancePerPulse(leftDPP);
        rightEncoder.setDistancePerPulse(rightDPP);
    }
    
    private class UpdateLocation extends Thread {

        public double x;
        public double y;
        public double heading;
        private boolean ResetFlag = false;

        UpdateLocation() {
            x = 0;
            y = 0;    
            heading = 0;
            headingGyro.reset();
        }
        
        public void Reset() {
            ResetFlag = true;
        }
        
        public void run() {       
            double deltax, deltay;
            double distance;
            double lastLeft = 0;
            double lastRight = 0;
            double left, right;
            
            while (true) {
                if(ResetFlag) {
                    x = 0;
                    y = 0;
                    headingGyro.reset();
                    ResetFlag = false;
                }
                left = leftEncoder.getDistance();
                right = rightEncoder.getDistance();                
                /* Average the two encoder values */ 
                /* TODO - posiably add error checking to detect a failed encoder and ignore it */
                distance = ((left-lastLeft )+(right-lastRight ))/2.0;                               
                heading = headingGyro.GetHeading();
                
                /* Do fancy trig stuff */
                deltax = distance*Math.sin(Math.toRadians(heading));
                deltay = distance*Math.cos(Math.toRadians(heading));
                
                /* Update Location */
                x += deltax;
                y += deltay;
                
                /* Update history variables */
                lastLeft = left;
                lastRight = right;
                
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
        }    
    }
}

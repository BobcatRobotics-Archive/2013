/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DigitalInput;

/**
 *
 * @author Robotics
 */
public class Climber extends Thread {
    
    
    Team177Robot robot;
    double throttle = 0.5;
    final int DRIVING = 0;
    final int RETRACTING = 1;
    final int EXTENDING = 2;
    int state = DRIVING;
    boolean enabled = true;
    
    final double stagelength = 36;
    final double encoderThresh = 2;
    
    /* Limit Switches */
    DigitalInput lowerlimit = new DigitalInput(8);
    DigitalInput upperlimit = new DigitalInput(9);
    
    Climber(Team177Robot robot) {
        this.robot = robot;
    }
    
    
    public void run() {
        if(enabled) {
        switch(state) {
            case RETRACTING:
                Retracting();
            case EXTENDING:
                Extending();
            case DRIVING:
                Driving();
         }
        } else {
            robot.drive.tankDrive(0, 0);
            //TODO: Fire pneumatic brake
        }
                      
     }
    
    public void Extending() {
        robot.drive.tankDrive(throttle, throttle);
        double maxdistance = Math.max(robot.locator.getLeftRaw(), robot.locator.getRightRaw());
        if((Math.abs(maxdistance-stagelength) < encoderThresh) || (upperlimit.get())) {
            state = RETRACTING;
        }
    }
    
    public void Retracting() {
        robot.drive.tankDrive(-throttle, -throttle);
        double mindistance = Math.min(robot.locator.getLeftRaw(), robot.locator.getRightRaw());
        if((mindistance < encoderThresh) || (lowerlimit.get())) {
            state = EXTENDING;
        }
        
    }
    
    public void Driving() {
        //TODO: Check for pyramid contact
        robot.locator.Reset();
        state = EXTENDING;
    }

}

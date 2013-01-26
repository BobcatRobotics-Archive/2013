/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;
/**
 *
 * @author Robotics
 */
public class Climber extends Thread {
    
    
    Team177Robot robot;
    int state;
    double throttle = 0.5;
    final int DRIVING = 0;
    final int RETRACTING = 1;
    final int EXTENDING = 2;
    
    Climber(Team177Robot robot) {
        this.robot = robot;
        this.state = DRIVING;
    }
    
    public void run() {
        switch(state) {
            case RETRACTING:
                Retracting();
            case EXTENDING:
                Extending();
            case DRIVING:
                Driving();
         }
                      
     }
    
    public void Extending() {
        robot.drive.tankDrive(throttle, throttle);
        //TODO: Check for upper limit switch trip
        state = RETRACTING;
    }
    
    public void Retracting() {
        robot.drive.tankDrive(-throttle, -throttle);
        //TODO: Check for lower limit switch trip
        state = EXTENDING;
    }
    
    public void Driving() {
        //TODO: Check for pyramid contact
        state = EXTENDING;
    }

}

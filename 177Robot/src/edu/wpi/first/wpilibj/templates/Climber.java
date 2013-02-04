/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;
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
    private boolean enabled = false;

    /* Limit Switches */
    DigitalInput lowerlimit;
    DigitalInput upperlimit;
    
    /* PTO */
    Solenoid pto;
    /* Mechanical Break */
    Solenoid brake;
    /* Climber Deploy - two way solinoid */
    Solenoid deployIn;
    Solenoid deployOut;
    
    Climber(Team177Robot robot, int lowerLimitSwitch, int upperLimitSwitch, int PTOChannel, int BrakeChannel, int DeployOutChannel, int DeployInChannel) {
        this.robot = robot;
        
        lowerlimit = new DigitalInput(lowerLimitSwitch);
        upperlimit = new DigitalInput(upperLimitSwitch);
        pto = new Solenoid(PTOChannel);
        brake = new Solenoid(2,BrakeChannel);
        deployOut = new Solenoid(2,DeployOutChannel);
        deployIn = new Solenoid(2,DeployInChannel);            
    }
    
    public void run() {
        
        while (true) {            
            SmartDashboard.putBoolean("Climber Upper Limit", upperlimit.get());
            SmartDashboard.putBoolean("Climber Lower Limit", lowerlimit.get());            
            if (enabled) {
                switch (state) {
                    case RETRACTING:
                        SmartDashboard.putString("Climber State", "Retracting");
                        Retracting();
                        break;
                    case EXTENDING:
                        SmartDashboard.putString("Climber State", "Extending");
                        Extending();
                        break;
                    case DRIVING:
                        SmartDashboard.putString("Climber State", "Driving");
                        Driving();
                        break;
                }
            } else {
               SmartDashboard.putString("Climber State", "Disabled");
            }
            /* Sleep for a while */
            try {
                Thread.sleep(10);
            } catch (Exception e) {}
        }
    }
    
    private void Extending() {        
        double maxdistance = Math.max(robot.locator.getLeftRaw(), robot.locator.getRightRaw());
        if((Math.abs(maxdistance-stagelength) < encoderThresh) || (!upperlimit.get())) {
            state = RETRACTING;
        } else {
            robot.drive.tankDrive(throttle, throttle);
        }
    }
    
    private void Retracting() {        
        double mindistance = Math.min(robot.locator.getLeftRaw(), robot.locator.getRightRaw());
        if((mindistance < encoderThresh) || (!lowerlimit.get())) {
            state = EXTENDING;
        } else {
            robot.drive.tankDrive(-throttle, -throttle);
        }
        
    }
    
    private void Driving() {
        //TODO: Check for pyramid contact
        robot.locator.Reset();
        state = EXTENDING;
    }
    
    public void setPTO(boolean on) {
        if(on) {
            if(!pto.get()) {
                pto.set(true);
                brake.set(true);
            } 
        } else {
            if(pto.get()) {
                pto.set(false);
                brake.set(false);
            } 
        }
    }
    public synchronized void enable(boolean e) {    
        if(!e && enabled) { 
            //disable climber                
            robot.drive.tankDrive(0, 0);
            setPTO(false);
            enabled = false;            
        } else if(deployOut.get() && !enabled) {
            //Enable only if the climber has been deployed
            setPTO(true);
            enabled = true;
       }        
    }
    
    public synchronized void test(double value) {    
        if(deployOut.get() && !enabled && pto.get()) {
            // Climber has to be deployed, and not running to test. PTO must be engaged 
            if((value > 0.1 && !upperlimit.get()) || (value < -0.1 && !lowerlimit.get())) {                
                robot.drive.tankDrive(value, value);
            } else {
                robot.drive.tankDrive(0, 0);
            }
        }
    }
        
    public synchronized void toggleDeploy() {
        if (!deployIn.get()) {
            //Climber is deployed, retract it, but only if it's lowered.
            if(!lowerlimit.get()) {
                deployOut.set(false);
                deployIn.set(true);
            }
        } else {
            //Climber is retracted, depoly it
            deployIn.set(true);
            deployOut.set(true);
        }
    }                     
}

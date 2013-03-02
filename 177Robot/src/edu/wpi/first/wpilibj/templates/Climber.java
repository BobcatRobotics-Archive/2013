
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
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
    private static final int BOX_DOWN = 3;
    private static final int BOX_UP = 4;
    private static final int BOX_WAIT = 5;
    
    private static final int STANDBY = 99;
    
    private static final double upthrottle = 1.0;
    private static final double downthrottle = 1.0;
    private static final double upPosition = 75;
    private static final double downPosition = 2;
    private static final double encoderThresh = 0.5;
    private static final double boxPosition = 10;
    
    private static final boolean UseLeftDriveTrain = false;   // Practice bot and real robot are different...
    
    Team177Robot robot;
    private int state = STANDBY;
    private boolean enabled = false;

    /* Limit Switches */
    private DigitalInput lowerlimit;
    private DigitalInput upperlimit;
    
    /* PTO */
    private Solenoid pto;
    /* Mechanical Break */
    private Solenoid brake;
    /* Climber Deploy - two way solinoid */
    private Solenoid deployIn;
    private Solenoid deployOut;
    
    Climber(Team177Robot robot, int lowerLimitSwitch, int upperLimitSwitch, int PTOChannel, int BrakeChannel, int DeployOutChannel, int DeployInChannel) {
        this.robot = robot;
        
        lowerlimit = new DigitalInput(lowerLimitSwitch);
        upperlimit = new DigitalInput(upperLimitSwitch);
        pto = new Solenoid(PTOChannel);
        //brake = new Solenoid(2, BrakeChannel);
        deployOut = new Solenoid(DeployOutChannel);
        deployIn = new Solenoid(DeployInChannel);

        LiveWindow.addActuator("Climmber", "PTO", pto);
        //LiveWindow.addActuator("Climmber", "Brake", brake);
        LiveWindow.addActuator("Climmber", "DeployIn", deployIn);
        LiveWindow.addActuator("Climmber", "DeployOut", deployOut);
        LiveWindow.addSensor("Climber", "Lower Limit", lowerlimit);
        LiveWindow.addSensor("Climber", "Upper Limit", upperlimit);
    }
    
    public void run() {
        
        while (true) {            
            SmartDashboard.putBoolean("Climber Upper Limit", !upperlimit.get());
            SmartDashboard.putBoolean("Climber Lower Limit", !lowerlimit.get());            
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
                    case BOX_DOWN:
                        //Lower climber for boxing.
                        SmartDashboard.putString("Climber State", "Box Down");
                        BoxDown();
                        break;
                    case BOX_WAIT:
                        //delay for climber to retract
                        SmartDashboard.putString("Climber State", "Box Wait");
                        BoxWait();
                        break;
                    case BOX_UP:
                        SmartDashboard.putString("Climber State", "Box Up");
                        BoxUp();
                        break;
                    case STANDBY:
                        SmartDashboard.putString("Climber State", "Standby");
                        robot.drive.tankDrive(0,0);
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
        System.out.println("RightRaw: " + robot.locator.getRightRaw());
        if((!UseLeftDriveTrain && Math.abs(robot.locator.getRightRaw()-upPosition) < encoderThresh) 
                || (UseLeftDriveTrain && Math.abs(robot.locator.getLeftRaw()-upPosition) < encoderThresh)
                || (!upperlimit.get())) {
            state = RETRACTING;
        } else {
            if(UseLeftDriveTrain) {
                robot.drive.tankDrive(upthrottle, 0);
            } else {
                robot.drive.tankDrive(0, upthrottle);
            }
        }
    }
    
    private void Retracting() {        
        if((!UseLeftDriveTrain && Math.abs(robot.locator.getRightRaw() - downPosition) < encoderThresh) 
                || (UseLeftDriveTrain && Math.abs(robot.locator.getLeftRaw() - downPosition) < encoderThresh) 
                || (!lowerlimit.get())) {
            state = EXTENDING;
        } else {
            if(UseLeftDriveTrain) {
                robot.drive.tankDrive(-downthrottle, 0);
            } else {
                robot.drive.tankDrive(0, -downthrottle);
            }
        }  
    }
    
    private void BoxDown() {        
        if(!lowerlimit.get()) {
            if (deployOut.get()) {
                //Climber is deployed, retract it.
                deployOut.set(false);
                deployIn.set(true);
            }
            robot.locator.startClimberMode();
            state = BOX_WAIT;
            boxTimer = Timer.getFPGATimestamp();
        } else {
            if(UseLeftDriveTrain) {
                robot.drive.tankDrive(-downthrottle/2, 0);
            } else {
                robot.drive.tankDrive(0, -downthrottle/2);
            }
        }  
    }
    
    private double boxTimer;
    private void BoxWait() {
        if(Timer.getFPGATimestamp() - boxTimer > 2) {
            state = BOX_UP;
        }
        robot.drive.tankDrive(0, 0);
    }
    
    private void BoxUp() {  
        System.out.println("RightRaw: " + robot.locator.getRightRaw());
        if((!UseLeftDriveTrain && Math.abs(robot.locator.getRightRaw()-boxPosition) < encoderThresh) 
                || (UseLeftDriveTrain && Math.abs(robot.locator.getLeftRaw()-boxPosition) < encoderThresh)
                || (!upperlimit.get())) {
            enable(false);
            state = DRIVING;
        } else {
            if(UseLeftDriveTrain) {
                robot.drive.tankDrive(upthrottle/2, 0);
            } else {
                robot.drive.tankDrive(0, upthrottle/2);
            }
        }
    }
    
    private boolean lastBox = false;
    //to fit in box for start of match, climber must be slightly raised. It has to be lowered before it can be deployed.
    //This function puts the climnber in the correct position
    public synchronized void box(boolean box) {
        //System.out.println("box");
        if(box && !lastBox) {    
            state = BOX_DOWN;
            setPTO(true);
            enabled = true;
        }      
        lastBox = box;
    }
    
   //Lower the climber for deployment.
    public boolean unbox() {
        //System.out.println("Unbox");
        if(lowerlimit.get()) {
            setPTO(true);
            if(UseLeftDriveTrain) {
                robot.drive.tankDrive(-downthrottle/2, 0);
            } else {
                robot.drive.tankDrive(0, -downthrottle/2);
            }
            return false;
        } else {
            robot.drive.tankDrive(0, 0);
            setPTO(false);
            return true;
        }
        
    }
    
    private void Driving() {
        //TODO: Check for pyramid contact
        robot.locator.startClimberMode();
        state = EXTENDING;
    }
    
    public void setPTO(boolean on) {
        //System.out.println("setPTO: " + on);
        if(on) {
            if(!pto.get()) {
                pto.set(true);
               // brake.set(true);
            } 
        } else {
            if(pto.get()) {
                pto.set(false);
               // brake.set(false);
            } 
        }
    }
    
    public synchronized void enable(boolean e) {   
        //System.out.println("enable "+e);
        if(!e && enabled) { 
            //disable climber                
            robot.drive.tankDrive(0, 0);
            setPTO(false);
            enabled = false;            
        } else if(e && deployOut.get() && !enabled) {
            //Enable only if the climber has been deployed
            if(state == STANDBY) {
                state = DRIVING;
            }
            setPTO(true);
            enabled = true;
       }        
    }
    
    public synchronized void test(double value) {    
        if((deployOut.get() || value > 0) && !enabled && pto.get()) {            
            // Climber has to be deployed, and not running to test. PTO must be engaged
            // Climber can be lowered when retracted, but not extended.          
            if((value < -0.1 && upperlimit.get()) || (value > 0.1 && lowerlimit.get())) {         
                if (UseLeftDriveTrain) {
                    robot.drive.tankDrive(-value, 0);
                } else {
                    robot.drive.tankDrive(0, -value);
                }
            } else {
                robot.drive.tankDrive(0, 0);
            }
        } else {
	    robot.drive.tankDrive(0, 0);
	}
    }
        
    public synchronized void toggleDeploy() {
        if (deployOut.get()) {
            //Climber is deployed, retract it, but only if it's lowered.
            if(!lowerlimit.get()) {
                deployOut.set(false);
                deployIn.set(true);
            }
        } else {
            //Climber is retracted, depoly it
            deployIn.set(false);
            deployOut.set(true);
        }
    }
    
    public synchronized boolean isDeployed() {
	return deployOut.get();
    }
}
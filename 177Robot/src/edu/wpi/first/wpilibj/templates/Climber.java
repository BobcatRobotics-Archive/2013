
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
    private static final int EXTEND = 0;
    private static final int RETRACT = 1;
    private static final int WINCH = 2;
    
    private static final int STANDBY = 99;
    
    private static final double winchThrottle = 1.0;
    private static final double cableLength = 38;
    private static final double encoderThresh = 1;
    
    private static final boolean UseLeftDriveTrain = true;   // Practice bot and real robot are different...
    private static final boolean UseRightDriveTrain = false;   // Practice bot and real robot are different...
    
    Team177Robot robot;
    private int state = STANDBY;
    private boolean enabled = false;
    private double delayTimer;

    /* Limit Switches */
    private DigitalInput hookDeployed;
        
    /* PTO */
    private Solenoid pto;
    /* Climber Deploy - two way solinoid */
    private Solenoid deployIn;
    private Solenoid deployOut;
    
    private Solenoid hook;
    
    Climber(Team177Robot robot, int hookDeployedSwitch, int PTOChannel, int DeployOutChannel, int DeployInChannel, int HookChannel) {
        this.robot = robot;
        
        hookDeployed = new DigitalInput(hookDeployedSwitch);        
        pto = new Solenoid(PTOChannel);        
        deployOut = new Solenoid(DeployOutChannel);
        deployIn = new Solenoid(DeployInChannel);
        hook = new Solenoid(2,HookChannel);

        LiveWindow.addActuator("Climber", "PTO", pto);       
        LiveWindow.addActuator("Climber", "DeployIn", deployIn);
        LiveWindow.addActuator("Climber", "DeployOut", deployOut);
        LiveWindow.addSensor("Climber", "Hook Switch", hookDeployed);
    }
    
    public void run() {        
        while (true) {            
            SmartDashboard.putBoolean("Climber Hook Switch", !hookDeployed.get());
            if (enabled) {
                switch (state) {
                    case EXTEND:
                        SmartDashboard.putString("Climber State", "EXTEND");                        
                        hook.set(true);
                        delayTimer = Timer.getFPGATimestamp();
                        state = RETRACT;
                        robot.locator.startClimberMode();
                        break;
                    case RETRACT:
                        SmartDashboard.putString("Climber State", "RETRACT");     
                        if(Timer.getFPGATimestamp() - delayTimer > 0.5) {
                            state = WINCH;
                            hook.set(false);
                            delayTimer = Timer.getFPGATimestamp();
                        }                         
                        break;
                    case WINCH:
                        SmartDashboard.putString("Climber State", "WINCH");
                        if(Timer.getFPGATimestamp() - delayTimer > 0.5) {                        
                            if((!UseLeftDriveTrain && Math.abs(robot.locator.getRightRaw()-cableLength) < encoderThresh) 
                                || (UseLeftDriveTrain && Math.abs(robot.locator.getLeftRaw()-cableLength) < encoderThresh)) {    
                                state = STANDBY;
                            } else {
                                SetClimber(winchThrottle);
                            }
                        }
                        break;
                    case STANDBY:
                        SmartDashboard.putString("Climber State", "Standby");
                        SetClimber(0);
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
    
    private void SetClimber(double value) {
        double left, right;
        if(UseLeftDriveTrain) {
            left = value;
        } else {
            left = 0;
        }
        if(UseRightDriveTrain) {
            right = value;
        } else {
            right = 0;
        }
        robot.drive.tankDrive(left, right);
    }
               
    public void setPTO(boolean on) {
        //System.out.println("setPTO: " + on);
        if(on) {
            if(pto.get()) {
                pto.set(false);
            } 
        } else {
            if(!pto.get()) {
                pto.set(true);
            } 
        }
    }
    
    public synchronized void enable(boolean e) {   
        //System.out.println("enable "+e);
        if(!e && enabled) { 
            //disable climber                
            SetClimber(0);
            setPTO(false);
            enabled = false;            
        } else if(e && deployOut.get() && !enabled) {
            //Enable only if the climber has been deployed
            if(state == STANDBY) {
                state = EXTEND;
            }
            setPTO(true);
            enabled = true;
       }        
    }
    
    public synchronized void test(double value) {    
        System.out.println("RightRaw: " + robot.locator.getRightRaw() +" LeftRaw: " +robot.locator.getLeftRaw());
              
        if((deployOut.get() || value > 0) && !enabled && !pto.get()) {            
            if(value < -0.1 || value > 0.1 ) { 
                SetClimber(-value);                
            } else {
                SetClimber(0);
            }
        } else {
	    SetClimber(0);
	}
    }
        
    public synchronized void toggleDeploy() {
        if (deployOut.get()) {
            //Climber is deployed, retract it, but only if it's lowered.
            deployOut.set(false);
            deployIn.set(true);
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
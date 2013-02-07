/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Shooter Control Logic
 * @author schroeder
 */
public class Shooter extends Thread {
    
    //Timing
    private static final double shooterTimeOut = 5.0;  //seconds to try and reach speed
    private static final double feedTime = 0.5;  //seconds to keep wheel spinning after actuating the feed mechanism
    private static final double pinTime = 0.01;  //seconds to delay between pulling restraining pin and feeding 
    private static final boolean shootOnTimeout = true; //shoot after shooterTimeOut seconds, even if not at speed
    
    //Speed setpoints
    private static final double ElevatedSetpoint1 = 3000;
    private static final double ElevatedSetpoint2 = 3000;
    private static final double NonElevatedSetpoint1 = 3000;
    private static final double NonElevatedSetpoint2 = 3000;
    
    // Mode Constants
    private static final int STANDBY = 0;
    private static final int SPINUP = 1;
    private static final int FEED = 2;
    
    final Encoder shooterEncoder1;
    final ShooterMotor shooterMotor1;
    PIDController shooterControl1;
    
    final Encoder shooterEncoder2;
    final ShooterMotor shooterMotor2;
    PIDController shooterControl2;
    
    final Solenoid shooterFeed;
    final Solenoid shooterPin;
    final Solenoid shooterElevation;
    
    private int shotsRemaining = 0;
    private int shooterMode;
    private boolean elevated = false;
    private boolean spinTest = false;
    private boolean feedTest = false;
    
    public Shooter(int Motor1, int Encoder1A, int Encoder1B, int Motor2, int Encoder2A, int Encoder2B, int Feed, int Pin, int Elevation) {
       shooterEncoder1 = new Encoder(Encoder1A,Encoder1B);
       shooterEncoder1.setDistancePerPulse(60.0/360.0); //360 pulse per revolution, multiplied by 60 to RPM? - need to confirm.
       shooterEncoder1.setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
       
       shooterEncoder2 = new Encoder(Encoder2A,Encoder2B);
       shooterEncoder2.setDistancePerPulse(60.0/360.0); //360 pulse per revolution, multiplied by 60 to RPM? - need to confirm.
       shooterEncoder2.setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
       
       shooterMotor1 = new ShooterMotor(Motor1);
       shooterMotor2 = new ShooterMotor(Motor2);
       
       shooterFeed = new Solenoid(Feed);
       shooterPin = new Solenoid(Pin);
       shooterElevation = new Solenoid(Elevation);
       
       shooterControl1 = new PIDController(0.001, 0, 0, 0.5/3000.0, shooterEncoder1, shooterMotor1); 
       shooterControl1.setAbsoluteTolerance(100.0); //set Tolerance to +/- 100 RPM
       shooterControl1.setSetpoint(NonElevatedSetpoint1);
       shooterControl1.disable();
       
       shooterControl2 = new PIDController(0.001, 0, 0, 0.5/3000.0, shooterEncoder2, shooterMotor2); 
       shooterControl2.setAbsoluteTolerance(100.0); //set Tolerance to +/- 100 RPM
       shooterControl2.setSetpoint(NonElevatedSetpoint2);
       shooterControl2.disable();
                    
       shooterMode = STANDBY;
    }    
    
    class ShooterMotor implements PIDOutput {
        
        final Victor shooterMotor;
        
        public ShooterMotor(int Motor) {
            shooterMotor = new Victor(Motor);            
        }
        
        public void pidWrite(double output) {
            shooterMotor.set(output);           
        }
    }
    
    public void run() {
        boolean feed = false;  //flag to indicate feeder should be actuated
        boolean spin = false;  //flag to indicate shooter wheels should be spining
        boolean pin = false;   //flag to indicate pin should be pulled
        double shootTime = 0;  //Keep track of sequence timing
                
        while(true) {
            switch(shooterMode) {
                case SPINUP:
                    if (!spin) {
                        spin = true;
                        shootTime = Timer.getFPGATimestamp(); //start timeout timer
                    }
                    
                    if(shooterControl1.onTarget() && shooterControl2.onTarget()) {
                        /* Shooter at spped, shoot */
                        shooterMode = FEED;
                    } else if ((Timer.getFPGATimestamp() - shootTime) > shooterTimeOut) {
                        /* Timeout, didn't reach speed */
                        System.out.println("Shooter timed out before reaching speed");
  
                        if (shootOnTimeout) {
                            //Fire anyway 
                            shooterMode = FEED;                      
                        } else {
                            spin = false;
                            shooterMode = STANDBY;
                        }
                    }
                    break;
                case FEED:
                    if(pin == false) {
                        pin = true;
                        shootTime = Timer.getFPGATimestamp();
                    } else if ((Timer.getFPGATimestamp() - shootTime) > feedTime+pinTime) {
                        /* Delay to give shooter time to complete the shot */
                        feed = false;
                        pin = false;
                        if(shotsRemaining > 0) {
                            shotsRemaining--;
                            shooterMode = SPINUP;
                            shootTime = Timer.getFPGATimestamp(); //start timeout timer
                            //may need delay here.
                        } else {
                             shooterMode = STANDBY;
                        }
                    } else if ((Timer.getFPGATimestamp() - shootTime) > pinTime) {
                        feed = true;
                    }                                                 
                    break;
                default:
                    pin = false;
                    feed = false;
                    spin = false;
            }

            /* Set outputs */
            shooterFeed.set(feed || feedTest);
            shooterPin.set(pin || feedTest);
 
            if (spin || spinTest) {
                shooterControl1.enable();
                shooterControl2.enable();
            } else {
                shooterControl1.disable();
                shooterControl2.disable();
            }
            
            SmartDashboard.putNumber("Shooter 1 Speed", shooterEncoder1.getRate());
            SmartDashboard.putBoolean("Shooter 1 on", shooterControl1.isEnable());
            SmartDashboard.putNumber("Shooter 1 Cmd", shooterMotor1.shooterMotor.get());
            
            SmartDashboard.putNumber("Shooter 2 Speed", shooterEncoder2.getRate());
            SmartDashboard.putBoolean("Shooter 2 on", shooterControl2.isEnable());
            SmartDashboard.putNumber("Shooter 2 Cmd", shooterMotor2.shooterMotor.get());
            
            SmartDashboard.putBoolean("Shooter feed", shooterFeed.get());
            SmartDashboard.putBoolean("Shooter pin", shooterPin.get());            
            
            
            /* Sleep for a while */
            try {
                Thread.sleep(10);
            } catch (Exception e) {}
            
        }      
    }
    
    public synchronized boolean isDone()
    {
    return shooterMode == STANDBY;
    }
 
    public synchronized void Fire() {
        if(shooterMode == STANDBY) {
            shooterMode = SPINUP;
        }
    } 
    
    public synchronized void Fire(int cnt) {
        shotsRemaining = cnt;
        if(shooterMode == STANDBY) {        
            shooterMode = SPINUP;
        }
    } 
    
    public synchronized void SetElevated(boolean e) {
        if(e != elevated) {
            //Change detected
            elevated = e;        
            shooterElevation.set(e);
            if(e) {
                shooterControl1.setSetpoint(ElevatedSetpoint1);
                shooterControl2.setSetpoint(ElevatedSetpoint2);
            } else {
                shooterControl1.setSetpoint(NonElevatedSetpoint1);
                shooterControl2.setSetpoint(NonElevatedSetpoint2);
            }
        }
        
    }
    
    public synchronized void SpinTest(boolean test) {
        spinTest = test;
    } 
    
    public synchronized void FeedTest(boolean test) {
        feedTest = test;
    } 
        
    
}

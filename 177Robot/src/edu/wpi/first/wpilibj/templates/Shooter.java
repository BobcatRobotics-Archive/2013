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
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;



/**
 * Shooter Control Logic
 * @author schroeder
 */
public class Shooter extends Thread {
    
    //Timing
    private static final double shooterTimeOut = 5.0;  //seconds to try and reach speed
    private static final double feedTime = 0.5;  //seconds to keep wheel spinning after actuating the feed mechanism
    private static final boolean shootOnTimeout = true; //shoot after shooterTimeOut seconds, even if not at speed
    
    // Mode Constants
    private static final int STANDBY = 0;
    private static final int SPINUP = 1;
    private static final int FEED = 2;
    
    private final Encoder shooterEncoder1;
    private final ShooterMotor shooterMotor1;
    private PIDController shooterControl1;
    
    private final Encoder shooterEncoder2;
    private final ShooterMotor shooterMotor2;
    private PIDController shooterControl2;
    
    private final Solenoid shooterFeed;
    
    private int shooterMode;
    private boolean spinTest = false;
    private boolean feedTest = false;
    
    public Shooter(int Motor1, int Encoder1A, int Encoder1B, int Motor2, int Encoder2A, int Encoder2B, int Feed) {
       shooterEncoder1 = new Encoder(Encoder1A,Encoder1B);
       shooterEncoder1.setDistancePerPulse(60.0/360.0); //360 pulse per revolution, multiplied by 60 to RPM? - need to confirm.
       shooterEncoder1.setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
       
       shooterEncoder2 = new Encoder(Encoder2A,Encoder2B);
       shooterEncoder2.setDistancePerPulse(60.0/360.0); //360 pulse per revolution, multiplied by 60 to RPM? - need to confirm.
       shooterEncoder2.setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
       
       shooterMotor1 = new ShooterMotor(Motor1, "Motor 1");
       shooterMotor2 = new ShooterMotor(Motor2, "Motor 2");
       
       shooterFeed = new Solenoid(Feed);
       
       shooterControl1 = new PIDController(0.001, 0, 0, 0.5/3000.0, shooterEncoder1, shooterMotor1); 
       shooterControl1.setAbsoluteTolerance(250.0); //set Tolerance to +/- 250 RPM
       shooterControl1.setSetpoint(3000);   //This is just a swag
       shooterControl1.disable();
       
       shooterControl2 = new PIDController(0.001, 0, 0, 0.5/3000.0, shooterEncoder2, shooterMotor2); 
       shooterControl2.setAbsoluteTolerance(250.0); //set Tolerance to +/- 250 RPM
       shooterControl2.setSetpoint(3000);   //This is just a swag
       shooterControl2.disable();
       
       LiveWindow.addActuator("Shooter", "Encoder 1", shooterEncoder1);
       LiveWindow.addActuator("Shooter", "Encoder 2", shooterEncoder2);
       LiveWindow.addActuator("Shooter", "Feed", shooterFeed);
       
       shooterMode = STANDBY;
    }    
    
    private class ShooterMotor implements PIDOutput {
        
        private final Victor shooterMotor;
        
        public ShooterMotor(int Motor, String Name) {
            shooterMotor = new Victor(Motor);
            LiveWindow.addActuator("Shooter", Name, shooterMotor);
        }
        
        public void pidWrite(double output) {
            shooterMotor.set(output);           
        }
    }
    
    public void run() {
        boolean shooting = false;
        double shootTime = 0;
        boolean shooterOn = false;
        
        while(true) {
            switch(shooterMode) {
                case SPINUP:
                    if (!shooterOn) {
                        shooterOn = true;
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
                            shooterOn = false;
                            shooterMode = STANDBY;
                        }
                    }
                    break;
                case FEED:
                    if(shooting == false) {
                        shooting = true;
                        shootTime = Timer.getFPGATimestamp();
                    } else if ((Timer.getFPGATimestamp() - shootTime) > feedTime) {
                        /* Delay to give shooter time to complete the shot */
                        //TODO - Add logic to deal with multiple frizbees
                        shooterFeed.set(false);
                        shooting = false;
                        shooterMode = STANDBY;
                    }                                                  
                    break;
                default:
                    shooterOn = false;
                    shooting = false;
            }

            /* Set outputs */
            shooterFeed.set(shooting || feedTest);
 
            if (shooterOn || spinTest) {
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
            
            
            /* Sleep for a while */
            try {
                Thread.sleep(10);
            } catch (Exception e) {}
            
        }      
    }
 
    public synchronized void Fire() {
        if(shooterMode == STANDBY) {
            shooterMode = SPINUP;
        }
    } 
    
    public synchronized void SpinTest(boolean test) {
        spinTest = test;
    } 
    
    public synchronized void FeedTest(boolean test) {
        feedTest = test;
    } 
}

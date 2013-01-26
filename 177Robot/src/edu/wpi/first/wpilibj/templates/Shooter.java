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
    
    private final Encoder shooterEncoder;
    private final ShooterMotor shooterMotor;
    private final Solenoid shooterFeed;
    private PIDController shooterControl;
    
    private int shooterMode;
    private boolean spinTest = false;
    private boolean feedTest = false;
    
    public Shooter(int Motor, int EncoderA, int EncoderB, int Feed) {
       shooterEncoder = new Encoder(EncoderA,EncoderB);
       shooterEncoder.setDistancePerPulse(60.0/360.0); //360 pulse per revolution, multiplied by 60 to RPM? - need to confirm.
       shooterEncoder.setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
       
       shooterMotor = new ShooterMotor(Motor);
       shooterFeed = new Solenoid(Feed);
       
       shooterControl = new PIDController(0.001, 0, 0, 0.5/3000.0, shooterEncoder, shooterMotor); 
       shooterControl.setAbsoluteTolerance(250.0); //set Tolerance to +/- 250 RPM
       shooterControl.setSetpoint(3000);   //This is just a swag
       shooterControl.disable();
       
       LiveWindow.addActuator("Shooter", "Encoder", shooterEncoder);
       LiveWindow.addActuator("Shooter", "Feed", shooterFeed);
       
       shooterMode = STANDBY;
    }    
    
    private class ShooterMotor implements PIDOutput {
        
        private final Victor shooterMotor;
        
        public ShooterMotor(int Motor) {
            shooterMotor = new Victor(Motor);
            LiveWindow.addActuator("Shooter", "Shooter", shooterMotor);
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
                    
                    if(shooterControl.onTarget()) {
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
                shooterControl.enable();
            } else {
                shooterControl.disable();
            }
            
            SmartDashboard.putNumber("Shooter Speed", shooterEncoder.getRate());
            SmartDashboard.putBoolean("Shooter on", shooterControl.isEnable());
            SmartDashboard.putBoolean("Shooter feed", shooterFeed.get());
            SmartDashboard.putNumber("Shooter Cmd", shooterMotor.shooterMotor.get());
            
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

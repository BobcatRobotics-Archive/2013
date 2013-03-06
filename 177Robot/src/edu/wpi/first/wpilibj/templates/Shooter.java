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
    
    private static final boolean UseClosedLoop = true;
    private static final boolean ShooterAlwaysOn = false;
    
    private static final double EncoderCPR = 128;
    
    //Timing
    private static final double shooterTimeOut = 2; //0.5;  //seconds to try and reach speed    
    private static final double feedTime = 0.5;  //seconds to keep wheel spinning after actuating the feed mechanism
    private static final double pinTime = 0.05;  //seconds to delay between pulling restraining pin and feeding 
    private static final double resetTime = 0.05; //minmum time to delay between shots
    private static final boolean shootOnTimeout = true; //shoot after shooterTimeOut seconds, even if not at speed
          
    //Speed setpoints
    private static final double MotorRatio = 1.0;
    private static final double PyramidSetpoint1 = -0.6;
    private static final double PyramidSetpoint2 = PyramidSetpoint1*MotorRatio;
    private static final double LongSetpoint1 = -0.7;
    private static final double LongSetpoint2 = LongSetpoint1*MotorRatio;
    private static final double DumpSetpoint1 = 0.0;//-0.07; 
    private static final double DumpSetpoint2 = DumpSetpoint1*MotorRatio;
    
    private double MotorSetpoint1 = PyramidSetpoint1;
    private double MotorSetpoint2 = PyramidSetpoint2;      
    
    private static final double CLPyramidSetpoint1 = 5300; ///4750;
    private static final double CLPyramidSetpoint2 = PyramidSetpoint1*MotorRatio;
    private static final double CLLongSetpoint1 = 5300; ///4750;
    private static final double CLLongSetpoint2 = LongSetpoint1*MotorRatio;
    private static final double CLDumpSetpoint1 = 0; 
    private static final double CLDumpSetpoint2 = DumpSetpoint1*MotorRatio;
    
    private static final double mP = -0.0001;
    private static final double mI = -0.0002;
    private static final double mD = -0.00001;
    private static final double mF = -1.0/5000.0;
    
    // Mode Constants
    private static final int STANDBY = 0;
    private static final int SPINUP = 1;
    private static final int FEED = 2;
    private static final int RESET = 3;
        
    private final FilteredEncoder shooterEncoder1;
    private final ShooterMotor shooterMotor1;
    private PIDController shooterControl1;
    
    private final FilteredEncoder shooterEncoder2;
    private final ShooterMotor shooterMotor2;
    private PIDController shooterControl2;
    
    private final Solenoid shooterFeed;
    private final Solenoid shooterPin;
    private final Solenoid shooterElevation;
    
    private int shotsRemaining = 0;
    private int shooterMode;
    private boolean elevated = false;
    private boolean spinTest = false;
    private boolean feedTest = false;
    private boolean shooterPaused = false;
    private boolean shooting = false;
    
    public Shooter(int Motor1, int Encoder1A, int Encoder1B, int Motor2, int Encoder2A, int Encoder2B, int Feed, int Pin, int Elevation) {
        shooterEncoder1 = new FilteredEncoder(Encoder1A, Encoder1B);
        shooterEncoder1.setDistancePerPulse(60.0 / EncoderCPR); //pulse per revolution, multiplied by 60 to RPM?
        shooterEncoder1.setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
        shooterEncoder1.start();

        shooterEncoder2 = new FilteredEncoder(Encoder2A, Encoder2B);
        shooterEncoder2.setDistancePerPulse(60.0 / EncoderCPR); //pulse per revolution, multiplied by 60 to RPM?
        shooterEncoder2.setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
        shooterEncoder2.start();

        shooterMotor1 = new ShooterMotor(Motor1);
        shooterMotor2 = new ShooterMotor(Motor2);

        shooterFeed = new Solenoid(Feed);
        shooterPin = new Solenoid(Pin);
        shooterElevation = new Solenoid(Elevation);
 
        shooterControl1 = new PIDController(mP, mI, mD, mF, shooterEncoder1, shooterMotor1);
        shooterControl1.setAbsoluteTolerance(100.0); //set Tolerance to +/- 100 RPM
        shooterControl1.setSetpoint(CLPyramidSetpoint1);
        //shooterControl1.setOutputRange(-1, 0);
        shooterControl1.disable();

        shooterControl2 = new PIDController(mP, mI, mD, mF, shooterEncoder2, shooterMotor2);
        shooterControl2.setAbsoluteTolerance(100.0); //set Tolerance to +/- 100 RPM
        shooterControl2.setSetpoint(CLPyramidSetpoint2);
        //shooterControl2.setOutputRange(-1, 0);
        shooterControl2.disable();

        shooterMode = STANDBY;

        LiveWindow.addSensor("Shooter", "Encoder 1", shooterEncoder1);
        LiveWindow.addSensor("Shooter", "Encoder 2", shooterEncoder2);
        LiveWindow.addActuator("Shooter", "Feed", shooterFeed);
        LiveWindow.addActuator("Shooter", "Pin", shooterPin);
        LiveWindow.addActuator("Shooter", "Elevation", shooterElevation);
        LiveWindow.addActuator("Shooter", "Motor 1", shooterMotor1.shooterMotor);
        LiveWindow.addActuator("Shooter", "Motor 2", shooterMotor2.shooterMotor);
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
            if (!shooterPaused) {
                switch (shooterMode) {
                    case SPINUP:
                        if (!spin) {
                            spin = true;
                            shootTime = Timer.getFPGATimestamp(); //start timeout timer
                            shooterEncoder1.resetStoredData();
                            //shooterEncoder2.resetStoredData();
                        }

                        if (UseClosedLoop && shooterControl1.onTarget() && shooterControl2.onTarget()) {
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
                        if (pin == false) {
                            pin = true;
                            shootTime = Timer.getFPGATimestamp();
                        } else if ((Timer.getFPGATimestamp() - shootTime) > feedTime + pinTime) {
                            /* Delay to give shooter time to complete the shot */
                            feed = false;
                            pin = false;
                            shooterMode = RESET;
                            shootTime = Timer.getFPGATimestamp();
                        } else if ((Timer.getFPGATimestamp() - shootTime) > pinTime) {
                            feed = true;
                        }
                        break;
                    case RESET:
                        //Delay between shots
                        if ((Timer.getFPGATimestamp() - shootTime) > resetTime) {
                            if (shotsRemaining > 0 || shooting) {
                                if (shotsRemaining > 0) {
                                    shotsRemaining--;
                                }
                                shooterMode = SPINUP;
                                shootTime = Timer.getFPGATimestamp(); //start timeout timer
                                //may need delay here.
                            } else {
                                shooterMode = STANDBY;
                            }
                            shooterEncoder1.dumpStoredData();
                            //shooterEncoder2.dumpStoredData();
                        }
                        break;
                    default:
                        pin = false;
                        feed = false;
                        spin = false;
                        if(shooting == true) {
                            shooterMode = SPINUP;
                        }
                }

                /* Set outputs */
                shooterFeed.set(feed || feedTest);
                shooterPin.set(pin || feedTest);

                if(!ShooterAlwaysOn) {
                    if(UseClosedLoop) {
                        if (spin || spinTest) {
                            shooterControl1.enable();
                            shooterControl2.enable();
                        } else {
                            shooterControl1.disable();
                            shooterControl2.disable();
                        }
                    } else {
                        if (spin || spinTest) {
                            shooterMotor1.shooterMotor.set(MotorSetpoint1);
                            shooterMotor2.shooterMotor.set(MotorSetpoint2);                        
                        } else {
                            shooterMotor1.shooterMotor.set(0);
                            shooterMotor2.shooterMotor.set(0);   
                        }
                    }
                } else {
                    if(UseClosedLoop) {
                        shooterControl1.enable();
                        shooterControl2.enable();
                    } else {
                        //For Full court -0.65 on both
                        //pyramid -0.6
                        //pyramid goal -014
                        shooterMotor1.shooterMotor.set(MotorSetpoint1);
                        shooterMotor2.shooterMotor.set(MotorSetpoint2);
                    }
                }
                                      
            } else {
                //shooterMotor1.shooterMotor.set(0);
                //shooterMotor2.shooterMotor.set(0);
                if(UseClosedLoop) {
                    shooterControl1.disable();
                    shooterControl2.disable();
                }
            }
            
            if(UseClosedLoop) {
                SmartDashboard.putNumber("Shooter 1 Speed", shooterEncoder1.getLastRate());
            } else {
                SmartDashboard.putNumber("Shooter 1 Speed", shooterEncoder1.getRate());
            }
            SmartDashboard.putBoolean("Shooter 1 on", shooterControl1.isEnable());
            SmartDashboard.putNumber("Shooter 1 Cmd", shooterMotor1.shooterMotor.get());
            
            if(UseClosedLoop) {
                SmartDashboard.putNumber("Shooter 2 Speed", shooterEncoder2.getLastRate());
            } else {
                SmartDashboard.putNumber("Shooter 2 Speed", shooterEncoder2.getRate());
            }
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
 
    public synchronized void Fire(boolean fire) {
        shooting = fire;
    } 
    
    public synchronized void Fire(int cnt) {
        shotsRemaining = cnt-1;
        if(shooterMode == STANDBY) {        
            shooterMode = SPINUP;
        }
    } 
    
    public synchronized void Reset() {
        shooterMode = STANDBY;
        shotsRemaining = 0;
        SetPyramid();
    }
    
    public synchronized void SetElevated(boolean e) {
        if(e != elevated) {
            //Change detected
            elevated = e;        
            shooterElevation.set(!e); //Default position is up
            if(e) {
                SetPyramid();
            } else {
                SetLong();                
            }
        }        
    }
    
    public synchronized void SetLong() {      
        shooterControl1.setSetpoint(CLLongSetpoint1);
        shooterControl2.setSetpoint(CLLongSetpoint2);

        MotorSetpoint1 = LongSetpoint1;
        MotorSetpoint2 = LongSetpoint2;
    }
    
    public synchronized void SetPyramid() {
        shooterControl1.setSetpoint(CLPyramidSetpoint1);
        shooterControl2.setSetpoint(CLPyramidSetpoint2);
        MotorSetpoint1 = PyramidSetpoint1;
        MotorSetpoint2 = PyramidSetpoint2;
    }
        
    public synchronized void SetDump() {
        shooterControl1.setSetpoint(CLDumpSetpoint1);
        shooterControl2.setSetpoint(CLDumpSetpoint2);
        MotorSetpoint1 = DumpSetpoint1;
        MotorSetpoint2 = DumpSetpoint2;
    }
    
    public synchronized void SetOff() {
        MotorSetpoint1 = 0;
        MotorSetpoint2 = 0;
    }
    
    public synchronized void SpinTest(boolean test) {
        spinTest = test;
    } 
    
    public synchronized void FeedTest(boolean test) {
        feedTest = test;
    } 
    
    public synchronized void Pause() {
        //Cause the thread to do nothing
        System.out.println("Shooter Paused");
        shooterPaused = true;
    }
    
     public synchronized void Resume() {
        //Cause the thread to do nothing
        System.out.println("Shooter Resumed");
        shooterPaused = false;
    }
     
      public synchronized boolean isPaused() {
        return shooterPaused;
    }
    
}

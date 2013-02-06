/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;


import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Team177Robot extends IterativeRobot {
    
    /** Constants to disable subsystems to facilitate testing */
    private static final boolean enableClimber = true;
    private static final boolean enableShooter = true;
    private static final boolean enableVision  = true;
    
    /** Right Joystick Buttons **/
    private static final int shiftButton = 3; //Right Joystick button 3 is the shifter

    /** Left Joystick Buttons **/
    private static final int omniButton = 3;  //Left Joystick button 3 is the omni
    
    /** Operator Joystick Buttons **/
    private static final int feedTestButton = 4; 
    private static final int shootButton = 6; 
    private static final int shooterTestButton = 8; 
    private static final int climberButton = 2;
    private static final int climberDeployToggle = 5;
    private static final int climberTestAxis = 2; //??
    private static final int climberPTOTest = 1;
    private static final int shooterElevateAxis = 6; //Up/down on digital pad

    /** Driver station Digital Channels **/
    // Automode switches are channels 1-3
    private static final int missleSwitchChannel = 4;

    /** Constants **/
    private static final float autoDelayMultiplier = 2.0f; //this is multiplied by DS analog input, 2 gives you the range 0-19 seconds
    
    /** IO Definitions **/
    /* Motors */
    private static final int MotorDriveRL = 1;
    private static final int MotorDriveRR = 2;
    private static final int MotorDriveFL = 3;
    private static final int MotorDriveFR = 4;
    private static final int MotorDriveML = 5;
    private static final int MotorDriveMR = 6;    
    private static final int MotorShooter1 = 7;
    private static final int MotorShooter2 = 8;
    
    /* Analog Inputs */
    private static final int AIOGyro = 1;
    
    /* Digital IO */
    private static final int DIOPressureSwitch = 1;
    private static final int DIOLeftEncoderA = 2;
    private static final int DIOLeftEncoderB = 3;    
    private static final int DIORightEncoderA = 4;
    private static final int DIORightEncoderB = 5;    
    private static final int DIOshooterEncoder1A = 10;//6;
    private static final int DIOshooterEncoder1B = 11; //7;    
    private static final int DIOshooterEncoder2A = 12;//8;
    private static final int DIOshooterEncoder2B = 13;//9;
    private static final int DIOclimberLowerSwitch = 10;
    private static final int DIOclimberUpperSwitch = 11;
    
    /* Solenoids - Module 1 */
    private static final int SolenoidDriveShifter = 1;
    private static final int SolenoidClimberPTO = 2;
    private static final int SolenoidDriveOmni = 3;
    private static final int SolenoidShooterPin = 4;   
    private static final int SolenoidShooterFeed = 5;
    private static final int SolenoidShooterElevation = 6;
    private static final int SolenoidPickupDeploy = 7;
    
    /* Solenoids - Module 2 */
    private static final int SolenoidClimberDeployOut = 1;  //two way solenoid
    private static final int SolenoidClimberDeployIn = 2;
    private static final int SolenoidClimberBrake = 7;  
    
    /* Relays */
    private static final int RelayCompressor = 1;
    
    /* Instansiate Speed Controlers and Drive */    
    /*2012
    Victor rearLeftMotor = new Victor(1);
    Victor rearRightMotor = new Victor(2);

    Victor frontLeftMotor = new Victor(4);
    Victor frontRightMotor = new Victor(3);
    */
    /*2011
    Victor rearLeftMotor = new Victor(2);
    Victor rearRightMotor = new Victor(1);

    Victor frontLeftMotor = new Victor(4);
    Victor frontRightMotor = new Victor(3);
   */
    
    /*2013*/
    Victor rearLeftMotor = new Victor(MotorDriveRL);
    Victor rearRightMotor = new Victor(MotorDriveRR);

    Victor frontLeftMotor = new Victor(MotorDriveFL);
    Victor frontRightMotor = new Victor(MotorDriveFR);
        
    Victor midLeftMotor = new Victor(MotorDriveML);
    Victor midRightMotor = new Victor(MotorDriveMR);                
    
    RobotDrive6 drive = new RobotDrive6(frontLeftMotor,midLeftMotor, rearLeftMotor,frontRightMotor,midRightMotor,rearRightMotor);
    //RobotDrive6 drive = new RobotDrive6(frontLeftMotor, rearLeftMotor, frontRightMotor, rearRightMotor); //For 4 motor drivetrain
    
    /* Instansiate Joysticks */
    Joystick leftStick = new Joystick(1);
    Joystick rightStick = new Joystick(2);
    Joystick operatorStick = new Joystick(3);
    
    /* Instansiate Locator - Scaling set in contructor*/
    /*Left Encoder A,B, Right Encoder A,B, Gyro*/
    Locator locator = new Locator(DIOLeftEncoderA,DIOLeftEncoderB,DIORightEncoderA,DIORightEncoderB,AIOGyro); 

    /* Instnsiate VisionClient to get data from vision subsystem */
    VisionClient vision;
    
    /* Shooter */
    Shooter shooter;
    
    /* Climber */
    Climber climber;
    
    /* Pnumatics */
    Compressor compressor = new Compressor(DIOPressureSwitch,RelayCompressor);  
    Solenoid shifter = new Solenoid(SolenoidDriveShifter);
    Solenoid omni = new Solenoid(SolenoidDriveOmni);
          
    /* Automode Variables */
    int autoMode = 0;
    float autoDelay = 0;
    AutoMode auto;
    
    /* State Variables */
    boolean lastFireButton = false;
    boolean lastDeployButton = false;
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        
        if(enableShooter) {
            shooter = new Shooter(MotorShooter1, DIOshooterEncoder1A, DIOshooterEncoder1B, 
                        MotorShooter2, DIOshooterEncoder2A, DIOshooterEncoder2B, 
                        SolenoidShooterPin, SolenoidShooterFeed,SolenoidShooterElevation);
            
            LiveWindow.addSensor("Shooter", "Encoder 1", shooter.shooterEncoder1);
            LiveWindow.addSensor("Shooter", "Encoder 2", shooter.shooterEncoder2);
            LiveWindow.addActuator("Shooter", "Feed", shooter.shooterFeed);
            LiveWindow.addActuator("Shooter", "Pin", shooter.shooterPin);
            LiveWindow.addActuator("Shooter", "Elevation", shooter.shooterElevation);
            LiveWindow.addActuator("Shooter", "Motor 1", shooter.shooterMotor1.shooterMotor);
            LiveWindow.addActuator("Shooter", "Motor 2", shooter.shooterMotor2.shooterMotor);
            
            //Start Shooter
            shooter.start();
        }
        
        if(enableClimber) {
            climber = new Climber(this, DIOclimberLowerSwitch, DIOclimberUpperSwitch,
                                   SolenoidClimberPTO, SolenoidClimberBrake, 
                                   SolenoidClimberDeployOut, SolenoidClimberDeployIn);
            
            LiveWindow.addActuator("Climmber", "PTO", climber.pto);
            LiveWindow.addActuator("Climmber", "Brake", climber.brake);
            LiveWindow.addActuator("Climmber", "DeployIn", climber.deployIn);
            LiveWindow.addActuator("Climmber", "DeployOut", climber.deployOut);
            LiveWindow.addSensor("Climber", "Lower Limit", climber.lowerlimit);
            LiveWindow.addSensor("Climber", "Upper Limit", climber.upperlimit);
            
            //Start Climber
            climber.start();
        }
        
        if(enableVision) {
            vision = new VisionClient();
            //Start Vision, Connect to RPi
            vision.start();
        }
        
        /* Start Compressor - logic handled by Compressor class */
        compressor.start();
        
        /* Configure and Start the locator */

        /*Set encoder scaling */
        //locator.setDistancePerPulse(1.0f, 1.0f);  //2013
        //locator.setDistancePerPulse(0.15574f, 0.15748f);  //2012
        locator.setDistancePerPulse(0.095874f, 0.095874f);  //2011
        locator.start();
    
        LiveWindow.addSensor("Locater", "left Encoder", locator.leftEncoder);
        LiveWindow.addSensor("Locater", "right Encoder", locator.rightEncoder);
        LiveWindow.addSensor("Locater", "Gyro", locator.headingGyro);
            
        /*Setup LiveWindow */        
        LiveWindow.addActuator("Drive", "Left Front", frontLeftMotor);
        LiveWindow.addActuator("Drive", "Left Mid", midLeftMotor);
        LiveWindow.addActuator("Drive", "Left Rear", rearLeftMotor);
        LiveWindow.addActuator("Drive", "Right Front", frontRightMotor);
        LiveWindow.addActuator("Drive", "Right Mid", midRightMotor);
        LiveWindow.addActuator("Drive", "Right Rear", rearRightMotor);
        LiveWindow.addActuator("Drive", "Shifter", shifter);
        LiveWindow.addActuator("Drive", "Omni", omni);
                        
        /* Turn on watchdog */
        getWatchdog().setEnabled(true);

    }
    
    public void autonomousInit() {  
        locator.Reset(); //This maybe a problem as it takes a couple of seconds for it to actually reset
        if(auto != null) {
            auto.autoInit();
        }
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {  
        if(auto != null && m_ds.getMatchTime() > autoDelay) {
            auto.autoPeriodic();        
        } else {
            drive.tankDrive(0, 0);
        }
        
        SmartDashboard.putNumber("X", locator.GetX());
        SmartDashboard.putNumber("Y", locator.GetY());
        SmartDashboard.putNumber("Heading", locator.GetHeading());
    }

    /**
     * Initialization code for teleop mode should go here.
     */
    public void teleopInit() {
        locator.Reset();
    } 
    
    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        
        /* Climber/Drive Code */ 
        if(enableClimber) {
            if(!operatorStick.getRawButton(climberButton) && !operatorStick.getRawButton(climberPTOTest)) {
                // Regular Driving
                climber.enable(false);
                climber.setPTO(false);
                drive.tankDrive(leftStick, rightStick); // drive with the joysticks                        
                shifter.set(rightStick.getRawButton(shiftButton));
            } else if (operatorStick.getRawButton(climberPTOTest)) {
                // Climber testing
                climber.enable(false);
                climber.setPTO(true);
                shifter.set(false);
                climber.test(operatorStick.getRawAxis(climberTestAxis));                
            } else {
                // Climber Button
                shifter.set(false);            
                climber.enable(true);
            }            
            
            /* Climber Deploy Toggle*/
            if(!lastDeployButton && operatorStick.getRawButton(climberDeployToggle)) {
                climber.toggleDeploy();
            }
            
            lastDeployButton = operatorStick.getRawButton(climberDeployToggle);     
            
        } else {
            drive.tankDrive(leftStick, rightStick); // drive with the joysticks 
            shifter.set(rightStick.getRawButton(shiftButton));
        }
                       
        omni.set(leftStick.getRawButton(omniButton));

        if(enableShooter) {
            /* Shooter */
            if(operatorStick.getRawAxis(shooterElevateAxis) > 0) {
                shooter.SetElevated(true); 
            } else if(operatorStick.getRawAxis(shooterElevateAxis) < 0) {
                shooter.SetElevated(false); 
            }
        
            /* Missle switch is just for inital testing, can be removed if needed elsewhere */
            if(!lastFireButton && (!m_ds.getDigitalIn(missleSwitchChannel) || operatorStick.getRawButton(shootButton))) {
                shooter.Fire();
            }
            lastFireButton = (!m_ds.getDigitalIn(missleSwitchChannel) || operatorStick.getRawButton(shootButton));
        
            /* Shooter Testing */
            shooter.SpinTest(operatorStick.getRawButton(shooterTestButton)); 
            shooter.FeedTest(operatorStick.getRawButton(feedTestButton)); 
        }
                                
        /* Update dashboard */
        SmartDashboard.putNumber("X", locator.GetX());
        SmartDashboard.putNumber("Y", locator.GetY());
        SmartDashboard.putNumber("Heading", locator.GetHeading());
        
        if(enableVision) {
            SmartDashboard.putNumber("Distance", vision.distance);
            SmartDashboard.putNumber("DeltaX", vision.deltax);
            SmartDashboard.putNumber("DeltaY", vision.deltay);
            SmartDashboard.putNumber("Data Age", vision.timeRecieved);
        }
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
        LiveWindow.run();
        getWatchdog().feed();
        Timer.delay(0.01);     
        //drive.tankDrive(0,0);
        drive.setSafetyEnabled(false);
    }
    
    public void disabledPeriodic() {
        try {
            int new_autoMode = (m_ds.getDigitalIn(3)?0:1) + (m_ds.getDigitalIn(2)?0:2) + (m_ds.getDigitalIn(1)?0:4);
            if (new_autoMode != autoMode) {
                //Selected auto mode has changed, update references
                autoMode = new_autoMode;
                switch (autoMode) {
                    case 1:
                        auto = new AutoModeBasicDriveTest(this);
                        break;
                    case 2:
                        auto = new AutoModeDriveToTest(this);
                        break;
                    case 3:
                        auto = new AutoModeParkTest(this);
                        break;
                    default:
                        auto = null;
                }
            }

            autoDelay = (float)m_ds.getAnalogIn(1) * autoDelayMultiplier;
        } catch (Exception e) {
            System.out.println("Error in disabledPeriodic: " + e);
        }


        //Send the selected mode to the driver station
        if(auto == null) {
            SmartDashboard.putString("Auto Mode", "Do Nothing"); 
        } else {
            SmartDashboard.putString("Auto Mode", auto.getName()); 
        }
        SmartDashboard.putNumber("Auto Delay", autoDelay);
        Timer.delay(0.01);
    }
    
}

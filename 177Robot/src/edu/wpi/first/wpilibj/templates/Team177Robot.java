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
    static final boolean enableClimber = true;
    static final boolean enableShooter = true;
    static final boolean enableVision  = false;
    
    /** Right Joystick Buttons **/
    private static final int shiftButton = 3; //Right Joystick button 3 is the shifter

    /** Left Joystick Buttons **/
    private static final int stiffArmButton = 3;  //Left Joystick button 3 is stiff arm
    
    /** Operator Joystick Buttons **/
    private static final int feedTestButton = 10; 
    private static final int shootButton = 6; 
    private static final int setLongShotButton = 4;
    private static final int setPyramidShotButton = 3;
    private static final int setShooterOffButton = 9;
    private static final int shooterTestButton = 8; 
    private static final int climberButton = 2;
    private static final int climberDeployToggle = 5;
    private static final int climberTestAxis = 2; //??
    private static final int climberPTOTest = 1;
    private static final int shooterElevateAxis = 6; //Up/down on digital pad
    private static final int pegLegButton = 7;
    
    /** Driver station Digital Channels **/
    // Automode switches are channels 1-3
    private static final int firstAutoSwitchChannel = 1;
    private static final int secondAutoSwitchChannel = 2;
    private static final int thirdAutoSwitchChannel = 3;
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
    private static final int DIOLeftEncoderA = 5;
    private static final int DIOLeftEncoderB = 4;    
    private static final int DIORightEncoderA = 3;
    private static final int DIORightEncoderB = 2;    
    private static final int DIOshooterEncoder1A = 7;
    private static final int DIOshooterEncoder1B = 6;    
    private static final int DIOshooterEncoder2A = 9;
    private static final int DIOshooterEncoder2B = 8;
    private static final int DIOclimberHookSwitch = 10;
    
    /* Solenoids - Module 1 */
    private static final int SolenoidDriveShifter = 1;
    private static final int SolenoidClimberPTO = 2;
    private static final int SolenoidPegLeg = 3;
    private static final int SolenoidShooterPin = 4;   
    private static final int SolenoidShooterFeed = 5;
    private static final int SolenoidShooterElevation = 6;
    private static final int SolenoidClimberDeployOut = 7;  //two way solenoid
    private static final int SolenoidClimberDeployIn = 8;
    
    /* Solenoids - Module 2 */
    private static final int SolenoidClimberHook = 3;
    private static final int SolenoidStiffArm = 1;
    
    
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
    Solenoid peg = new Solenoid(SolenoidPegLeg);
    Solenoid stiffarm = new Solenoid(2,SolenoidStiffArm);
              
    /* Automode Variables */
    int autoMode = 0;
    float autoDelay = 0;
    AutoMode auto;
    
    /* State Variables */
    boolean lastDeployButton = false;
    boolean lastStiffArmButton = false;
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {       
        if(enableShooter) {
            shooter = new Shooter(this, MotorShooter1, DIOshooterEncoder1A, DIOshooterEncoder1B, 
                        MotorShooter2, DIOshooterEncoder2A, DIOshooterEncoder2B, 
                        SolenoidShooterFeed, SolenoidShooterPin, SolenoidShooterElevation);
           
            //Start Shooter
            shooter.start();
        }
        
        if(enableClimber) {
            climber = new Climber(this, DIOclimberHookSwitch, SolenoidClimberPTO, SolenoidClimberDeployOut, SolenoidClimberDeployIn, SolenoidClimberHook);
                       
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
        
	locator.setDistancePerPulse(0.086128257f, 0.086852995f);  //2013
        //locator.setDistancePerPulse(0.029998f, 0.029998f);  //2013 - Theoretical
        //locator.setDistancePerPulse(0.15574f, 0.15748f);  //2012
        //locator.setDistancePerPulse(0.095874f, 0.095874f);  //2011
        locator.start();

        /*Setup LiveWindow */        
        LiveWindow.addActuator("Drive", "Left Front", frontLeftMotor);
        LiveWindow.addActuator("Drive", "Left Mid", midLeftMotor);
        LiveWindow.addActuator("Drive", "Left Rear", rearLeftMotor);
        LiveWindow.addActuator("Drive", "Right Front", frontRightMotor);
        LiveWindow.addActuator("Drive", "Right Mid", midRightMotor);
        LiveWindow.addActuator("Drive", "Right Rear", rearRightMotor);
        LiveWindow.addActuator("Drive", "Shifter", shifter);
        
        //LiveWindow.addActuator("misc", "Peg", peg);
        LiveWindow.addActuator("misc", "StiffArm", stiffarm);
                        
        /* Turn on watchdog */
        //getWatchdog().setEnabled(true);

    }
    
    public void autonomousInit() {  
        locator.Reset(); //This maybe a problem as it takes a couple of seconds for it to actually reset
	if(auto != null) {
            auto.autoInit();
        }
        if(enableShooter) {
            shooter.Reset();  //Shouldn't be neccisary except for testing
        }
        climber.setPTO(false);
        peg.set(true);
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
        if(enableShooter) {
            if( shooter.isPaused()){            
                shooter.Resume();
            }
            shooter.Reset();
        }        
        locator.Reset();
        peg.set(false);
    } 
    
    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        
        /* Climber/Drive Code */	
	if(enableClimber) {
            
            //if( m_ds.getDigitalIn(missleSwitchChannel) 
            //        && !operatorStick.getRawButton(climberButton) && !operatorStick.getRawButton(climberPTOTest)) {
                // Regular Driving
                climber.enable(false);
                climber.setPTO(false);
                //drive.tankDrive(leftStick, rightStick); // drive with the joysticks                        
                drive.tankDrive(rightStick, leftStick); // drive with the joysticks -Reverse Controls  Must change RobotDrive6.TankDrive as well                 
                shifter.set(rightStick.getRawButton(shiftButton));
            /*} else if (operatorStick.getRawButton(climberPTOTest)) {
                // Climber testing
                climber.enable(false);   //Disable the climber logic
                climber.setPTO(true);    //Enable the PTO
                shifter.set(false);      //Make sure we are in Low Gear
                climber.test(operatorStick.getRawAxis(climberTestAxis));  //Use the stick to control the climber                
            } else if (!m_ds.getDigitalIn(missleSwitchChannel)) {
                shifter.set(false);            
                climber.enable(true);
            } else {
                // Climber Button
                shifter.set(false);            
                climber.enable(true);
            } */           
            
            /* Climber Deploy Toggle*/
            if(!lastDeployButton && (operatorStick.getRawButton(climberDeployToggle)  ||( m_ds.getMatchTime() > 134) && !climber.isDeployed())) {
                climber.toggleDeploy();
            }      
            lastDeployButton = operatorStick.getRawButton(climberDeployToggle);     
            
            
        } else {
            drive.tankDrive(leftStick, rightStick); // drive with the joysticks 
            shifter.set(rightStick.getRawButton(shiftButton));
        }
        
        //peg.set(leftStick.getRawButton(pegButton));
        
        /* Stiff Arm Deploy Toggle*/
       /* if(!lastStiffArmButton && operatorStick.getRawButton(stiffArmButton)) {
            if(stiffarm.get()) {
                stiffarm.set(false);
            } else {
                stiffarm.set(true);
            }               
        }      
        lastStiffArmButton = operatorStick.getRawButton(stiffArmButton);     
         */
        stiffarm.set(leftStick.getRawButton(stiffArmButton));
        peg.set(operatorStick.getRawButton(pegLegButton));
        
        if(enableShooter) {
            /* Shooter */
            if(operatorStick.getRawAxis(shooterElevateAxis) > 0) {
                shooter.SetElevated(false); 
            } else if(operatorStick.getRawAxis(shooterElevateAxis) < 0) {
                shooter.SetElevated(true); 
            }
        
            shooter.Fire(operatorStick.getRawButton(shootButton));
            
            if(operatorStick.getRawButton(setLongShotButton)) {
                shooter.SetLong();
            }
            if(operatorStick.getRawButton(setPyramidShotButton)) {
                shooter.SetPyramid();
            }
            if(operatorStick.getRawButton(setShooterOffButton)) {
                shooter.SetOff();
            }
                
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
        if(enableShooter && !shooter.isPaused()) {
            shooter.Pause();
        }
        LiveWindow.run();
        //getWatchdog().feed();
        //Timer.delay(0.1);     
        //drive.tankDrive(0,0);
        drive.setSafetyEnabled(false);
    }
    
    public void disabledPeriodic() {
        try {
	    int new_automode = (m_ds.getDigitalIn(firstAutoSwitchChannel) ? 0 : 4) + (m_ds.getDigitalIn(secondAutoSwitchChannel) ? 0 : 2) + (m_ds.getDigitalIn(thirdAutoSwitchChannel) ? 0 : 1);
	    if (new_automode != autoMode)
	    {

		autoMode = new_automode;
		/*
		 * Automode Key: 0 - Do nothing 1 - Shoot From Front 2 - Shoot
		 * Through Center 3 - 5 Disc Through Center 4 - Shoot From Side
		 * 5 - Shoot from Side and Pickup Frisbees
		 */
		switch (autoMode)
		{
		    case 1:
			auto = new AutoModeThroughCenter(this);
			break;
		    case 2:
			auto = new AutoModeThroughCenterBlockCenter(this);
			break;
		    case 3:
			auto = new AutoModeCornerDriveToFeeder(this);
			break;
                    case 4:
			auto = new AutoModeThroughCenterDriveToFeeder(this);
			break;
		    case 5:
			auto = new AutoModeCorner(this);
			break;
/*		    case 5:
			auto = new AutoModeShootFromSidePickUpFrisbees(this);
			break;
                    case 6:
                        auto = new AutoModeFromFront(this);			                        
                        break;
 */
		    default:
			auto = new AutoModeDoNothing(this);
			break;
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

/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;


import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
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
    
    /** Right Joystick Buttons **/
    private static final int shiftButton = 3; //Right Joystick button 3 is the shifter

    /** Constants **/
    private static final float autoDelayMultiplier = 2.0f; //this is multiplied by DS analog input, 2 gives you the range 0-19 seconds
    
    /** IO Definitions **/
    
    /* Instansiate Speed Controlers and Drive */    
    Victor frontLeftMotor = new Victor(4);
    Victor frontRightMotor = new Victor(3);
    
    Victor rearLeftMotor = new Victor(1);
    Victor rearRightMotor = new Victor(2);
    
    Victor midLeftMotor = new Victor(5);
    Victor midRightMotor = new Victor(6);
    
    Victor shooterMotor1 = new Victor(7);
    Victor shooterMotor2 = new Victor(8);
    
    //RobotDrive6 drive = new RobotDrive6(frontLeftMotor,midLeftMotor, rearLeftMotor,frontRightMotor,midRightMotor,rearRightMotor);
    RobotDrive6 drive = new RobotDrive6(frontLeftMotor, rearLeftMotor, frontRightMotor, rearRightMotor);

    
    /* Instansiate Joysticks */
    Joystick leftStick = new Joystick(1);
    Joystick rightStick = new Joystick(2);
    Joystick operatorStick = new Joystick(3);
    
     /* Instansiate Locator - Scaling set in contructor*/
    Locator locator = new Locator(2,3,4,5,1); /*Left Encoder A,B, Right Encoder A,B, Gyro*/ 
    
    /* Pnumatics
     * Pressure switch = DIO 1
     * Compressor = Relay 1
     * 
     * Shifter = Solinoid 1
     */
    Compressor compressor = new Compressor(1,1);
    Solenoid shifter = new Solenoid(1);
        
    /* Automode Variables */
    int autoMode = 0;
    float autoDelay = 0;
    AutoMode auto;

    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        
        /* Start Compressor - logic handled by Compressor class */
        compressor.start();
        
        /* Configure and Start the locator */
        locator.setDistancePerPulse(0.15574f, 0.15748f);  /*Set encoder scaling */
        locator.start();
               
        /*Setup LiveWindow */
        LiveWindow.addActuator("Shooter Testing", "Shooter 1", shooterMotor1);
        LiveWindow.addActuator("Shooter Testing", "Shooter 2", shooterMotor2);
         
        LiveWindow.addActuator("Drive", "Left Front", frontLeftMotor);
        LiveWindow.addActuator("Drive", "Left Mid", midLeftMotor);
        LiveWindow.addActuator("Drive", "Left Rear", rearLeftMotor);
        LiveWindow.addActuator("Drive", "Right Front", frontRightMotor);
        LiveWindow.addActuator("Drive", "Right Mid", midRightMotor);
        LiveWindow.addActuator("Drive", "Right Rear", rearRightMotor);

        /* Turn on watchdog */
        getWatchdog().setEnabled(true);

    }
    
    public void autonomousInit() {           
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
        
        /* Drive Code */ 
        drive.tankDrive(leftStick, rightStick); // drive with the joysticks
        shifter.set(rightStick.getRawButton(shiftButton));
                       
        SmartDashboard.putNumber("X", locator.GetX());
        SmartDashboard.putNumber("Y", locator.GetY());
        SmartDashboard.putNumber("Heading", locator.GetHeading());
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
            int new_autoMode = (m_ds.getDigitalIn(3)?0:4) + (m_ds.getDigitalIn(2)?0:2) + (m_ds.getDigitalIn(1)?0:1);
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

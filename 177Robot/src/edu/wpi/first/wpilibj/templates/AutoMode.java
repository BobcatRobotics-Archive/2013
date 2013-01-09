/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import com.sun.squawk.util.MathUtils;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Abstract class to provide the framework for handling multiple autonomous modes
 * @author schroed
 */
public abstract class AutoMode {
    
    Team177Robot robot; //reference to main implementation
    
    BasicPIDController DrivePID;
    BasicPIDController SteerPID;
    
    /* Variables & Constants used for DriveTo PID Controls */ 
    private static double SteerMargin = 3.0; //Margin to consider robot facing target (degrees)
    private static double DriveMargin = 2.0;  //Margin to consider the robot at target (in)
    
    private static double DriveP = 0.05;  //Preportial gain for Drive System
    private static double DriveI = 0.0;   //Integral gain for Drive System
    private static double DriveD = 0.0;   //Derivative gain for Drive System
    private static double DriveMax = 1;   //Max Saturation value for control
    private static double DriveMin = -1;  //Min Saturation value for control
    
    private static double SteerP = 0.02;  //Preportial gain for Steering System
    private static double SteerI = 0.01;  //Integral gain for Steering System
    private static double SteerD = 0.002;  //Derivative gain for Steering System
    private static double SteerMax = 1;   //Max Saturation value for control
    private static double SteerMin = -1;  //Min Saturation value for control
    
    double lastTargetX = 0;
    double lastTargetY = 0;
    
    double lastRanDriveTo = 0;

    public AutoMode(Team177Robot robot) {
        this.robot = robot;
        
        DrivePID = new BasicPIDController(DriveP,DriveI,DriveD);
        DrivePID.setOutputRange(DriveMin, DriveMax);
        SteerPID = new BasicPIDController(SteerP,SteerI,SteerD);
        SteerPID.setOutputRange(SteerMin, SteerMax);
    }
        
    public abstract void autoPeriodic();
    public abstract String getName();
    
    public void autoInit() {
        DrivePID.reset();
        SteerPID.reset();
    }
     
  
    /**
     * 
     * Drive the robot to the specified location
     * 
     *      -----------    +
     *      | Robot   |
     *      |   --->  |    Y
     *      |         |   
     *      -----------    -
     *       -   X    +
     *  Robot starts match at 0,0 heading 0
     * 
     * @param x - x coordinate of target
     * @param y - y coordinate of target
     * @param speed - Speed to drive, a negative value will cause the robot to backup.
     *                A Speed of 0 will cause the robot to turn to the target without moving
     * @return - Boolean value indicating if the robot is at the target or not (true = at target).
     * @author schroed
     */     
    public boolean DriveTo(double x, double y, double speed) 
    {
        double steer, drive;
        //Reinitalize if the target has changed
        if(x != lastTargetX || y != lastTargetY) {
            lastTargetX = x;
            lastTargetY = y;
            DrivePID.reset();
            SteerPID.reset();
            lastRanDriveTo = Timer.getFPGATimestamp();
            SmartDashboard.putNumber("Target X", x);
            SmartDashboard.putNumber("Target Y", y);            
        }
        //Calculate time step
        double now = Timer.getFPGATimestamp();
        double dT = (now - lastRanDriveTo);
        lastRanDriveTo = now;
                
        double deltaX = x - robot.locator.GetX();
        double deltaY = y - robot.locator.GetY();
        double distance = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
        
        //determine angle to target relative to field
        double targetHeading = Math.toDegrees(MathUtils.atan2(deltaY, deltaX));  // +/- 180 degrees
        
        if(speed < 0) {
            //reverse heading if going backwards
            targetHeading += 180;
        }
        
        //Determine  angle to target relative to robot
        double bearing = targetHeading + robot.locator.GetHeading();
        if (bearing > 180) {
            bearing = bearing - 180; //Quicker to turn the other direction
        }
        
        /* Steering PID Control */
        steer = SteerPID.calculate(bearing, dT);
        
        /* Drive PID Control */                
        if(speed == 0) {
            //Just turn to the target, no PI Control
            drive = 0;
        } else {
            drive = DrivePID.calculate(distance, dT)*speed;
        }        

        //Move the robot - Would this work better if we multiplyed by the steering PID output?
        robot.drive.tankDrive(drive+steer, drive-steer);
                
        if((distance < DriveMargin) || (Math.abs(targetHeading) < SteerMargin && speed == 0 )) {
            return true;
        } else {
            return false;
        }        
    }
    
}

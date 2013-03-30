/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

/**
 *
 * @author Robotics
 */
public class AutoModeCorner extends AutoMode
{

    private int stepCount = 0;

    public AutoModeCorner(Team177Robot robot)
    {
	super(robot);
    }

    public void autoPeriodic()
    {
		switch(stepCount)
	{
	    case 0: 
                robot.shooter.SetCorner();
		//Shoot 6 times
                robot.peg.set(true);
		robot.shooter.Fire(6);                
		stepCount++;
                //robot.shifter.set(true); //High Gear!
		break;
            case 1:
                if(robot.shooter.isDone()) {
                    stepCount++;
                    robot.peg.set(false);
                    robot.shooter.SetPyramid();
                }
                robot.drive.tankDrive(0.0,0.0);
                break;
            /*case 2:
                //Driver forward to center line
                if(DriveTo(90,0,1.0)) {  //was 94.2
                    stepCount++;
                }
                break;
            /*case 3:
                //Turn to driver station
                if(DriveTo(108+130,-75,0.0)) {
                    stepCount++;
                }
                break;*/    
	    default:
		robot.drive.tankDrive(0.0,0.0);
	}
    }

    public String getName()
    {
	return "Corner Shoot";
    }
}

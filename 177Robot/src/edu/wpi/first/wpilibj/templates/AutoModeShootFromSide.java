/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

/**
 *
 * @author Robotics
 */
public class AutoModeShootFromSide extends AutoMode
{
    private int stepCount = 0;
    private double startHeading;

    public AutoModeShootFromSide(Team177Robot robot)
    {
	super(robot);
    }
    
    public void autoPeriodic()
    {
	switch(stepCount)
	{
	    case 0:
		//Drive to where we have a clear shot
		if(DriveTo(0, -89, 0.5))
		{
		    if(((startHeading-robot.locator.GetHeading())+360)%360 > 90)
		    {
			robot.drive.tankDrive(0.0,0.0);
			stepCount++;
		    }
		    else
		    {
			robot.drive.tankDrive(-0.75,+0.75);
		    }      
		}
		break;
	    case 1: case 2: case 3:
		//Shoot 3 frisbees
		if(robot.shooter.isDone())
		{
		    robot.shooter.Fire(true);
		    stepCount++;
		}
		break;
	    default:
		robot.drive.tankDrive(0.0,0.0);
		System.out.println("Finished");
	}
    }
    
    public String getName()
    {
	return "ShootFromFront";
    }
}


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

/**
 *
 * @author Robotics
 */
public class AutoModeFromFront extends AutoMode
{
    private int stepCount = 0;

    public AutoModeFromFront(Team177Robot robot)
    {
	super(robot);
    }
    
    public void autoPeriodic()
    {
		switch(stepCount)
	{
	    case 0: case 1:
		//Shoot 2 times
		if(robot.shooter.isDone())
		{
		    robot.shooter.Fire(true);
		    stepCount++;
		}
		break;
	    case 2:
		//Drive to where the 2 frisbees are in front of the pyramid
		if(DriveTo(44, 47, 0.5))
		{
		    stepCount++;
		}
		break;
	    case 3:
		//Pickup frisbees
		stepCount++;
		break;
	    case 4:
		//Drive back to original position
		if (DriveTo(0, 0, 0.5))
		{
		    stepCount++;
		}
		break;
	    case 5: case 6:
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

/**
 *
 * @author Robotics
 */
public class AutoMode5DiscThroughCenter extends AutoMode
{

    private int stepCount = 0;

    public AutoMode5DiscThroughCenter(Team177Robot robot)
    {
	super(robot);
    }

    public void autoPeriodic()
    {
	switch(stepCount)
	{
	    case 0: case 1: case 2:
		//Shoot 3 times
		if(robot.shooter.isDone())
		{
		    robot.shooter.Fire(true);
		    stepCount++;
		}
		break;
	    case 3:
		//Drive to center line
		if(DriveTo(94.2, 0, -0.5))
		{
		    stepCount++;
		}
		break;
	    case 4:
		//TODO: Pickup frisbees
		stepCount++;
		break;
	    case 5:
		//Drive back to original position
		if (DriveTo(0, 0, 0.5))
		{
		    stepCount++;
		}
		break;
	    case 6: case 7:
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
	return "5DiscShootThroughCenter";
    }
}

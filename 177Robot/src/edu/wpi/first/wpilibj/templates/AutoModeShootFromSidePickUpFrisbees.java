/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

/**
 *
 * @author Robotics
 */
public class AutoModeShootFromSidePickUpFrisbees extends AutoMode
{
    private int stepCount = 0;
    private double startHeading;

    public AutoModeShootFromSidePickUpFrisbees(Team177Robot robot)
    {
	super(robot);
    }
    
    public void autoPeriodic()
    {
	switch(stepCount)
	{
	    case 0:
		//Drive to where we have a clear shot
		if(DriveTo(0, -73, 0.5))
		{
		    stepCount++;
		}
		break;
	    case 1:
		//Turn toward center of high goal
		if(DriveTo(216, -47, 0.0))
		{
		    stepCount++;
		}
	    case 2: case 3: case 4:
		//Shoot 3 frisbees
		if(robot.shooter.isDone())
		{
		    robot.shooter.Fire(true);
		    stepCount++;
		}
		break;
	     case 5:
		//Drive to center line
		if(DriveTo(-108, 47, 0.5))
		{
		    stepCount++;
		}
		break;
	     case 6:
		 //TODO: Pick up Frisbees
		stepCount++;
		break;
	     case 7:
		//Drive back to shooting position
		if(DriveTo(0, -73, 0.5))
		{
		    stepCount++;
		}
		break; 
	      case 8: case 9:
		//Shoot 2 frisbees
		if(robot.shooter.isDone())
		{
		    robot.shooter.Fire(true);
		    stepCount++;
		}
		break;
	    default:
		System.out.println("Finished");
	}
    }
    
    public String getName()
    {
	return "ShootFromFront";
    }
}


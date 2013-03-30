/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

/**
 *
 * @author Robotics
 */
public class AutoModeThroughCenter extends AutoMode
{

    private int stepCount = 0;

    public AutoModeThroughCenter(Team177Robot robot)
    {
	super(robot);
    }

    public void autoPeriodic()
    {
	switch(stepCount)
	{
	    case 0: 
		//Shoot 3 times
                robot.peg.set(true);
		robot.shooter.Fire(10);
                //robot.climber.unbox();
		stepCount++;
		break;
	    default:
		robot.drive.tankDrive(0.0,0.0);
                if(robot.shooter.isDone()) {
                    robot.peg.set(false);
                }
	}
    }

    public String getName()
    {
	return "ShootThroughCenter";
    }
}

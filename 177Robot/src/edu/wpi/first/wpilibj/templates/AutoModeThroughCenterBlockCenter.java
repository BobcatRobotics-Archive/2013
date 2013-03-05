/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

/**
 *
 * @author Robotics
 */
public class AutoModeThroughCenterBlockCenter extends AutoMode
{

    private int stepCount = 0;

    public AutoModeThroughCenterBlockCenter(Team177Robot robot)
    {
	super(robot);
    }

    public void autoPeriodic()
    {
	switch(stepCount)
	{
	    case 0: 
		//Shoot 3 times
		robot.shooter.Fire(3);                
		stepCount++;
                //robot.shifter.set(true); //High Gear!
		break;
            case 1:
                if(robot.climber.unbox() && robot.shooter.isDone()) {
                    stepCount++;
                }
                break;
            case 2:
                //Driver forward to center line
                if(DriveTo(94.2,0,1.0)) {
                    stepCount++;
                }
                break;
	    default:
		robot.drive.tankDrive(0.0,0.0);
	}
    }

    public String getName()
    {
	return "Shoot And Defend";
    }
}

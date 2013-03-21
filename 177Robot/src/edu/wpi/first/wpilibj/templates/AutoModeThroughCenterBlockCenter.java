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
		//Shoot 6 times
		robot.shooter.Fire(6);                
		stepCount++;
                //robot.shifter.set(true); //High Gear!
		break;
            case 1:
                if(robot.shooter.isDone()) {
                    stepCount++;
                }
                break;
            case 2:
                //Driver forward to center line
                if(DriveTo(108,0,1.0)) {  //was 94.2
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

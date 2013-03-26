/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

/**
 *
 * @author Robotics
 */
public class AutoModeCornerDriveToFeeder extends AutoMode
{

    private int stepCount = 0;

    public AutoModeCornerDriveToFeeder(Team177Robot robot)
    {
	super(robot);
    }

    public void autoPeriodic()
    {
	switch(stepCount)
	{
	    case 0: 
		//Shoot 3 times
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
                //Driver forward toward center line
                if(DriveTo(58,0,1.0)) {
                    stepCount++;
                }
                break;
            case 3:
                //Turn to left
                if(DriveTo(108,170,0)) {
                    stepCount++;
                }
                break;
            case 4:
                //Driver forward toword edge of field
                if(DriveTo(108,170,1.0)) {
                    stepCount++;
                }
                break;                
            case 5:
                //Turn to face feeder station
                if(DriveTo(380,170,0)) {
                    stepCount++;
                }
                break;                                
            default:
		robot.drive.tankDrive(0.0,0.0);
	}
    }

    public String getName()
    {
	return "Corner Shoot, To Feeder";
    }
}

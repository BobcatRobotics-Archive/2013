/*
 * Basic Automode to test frame work, drive forward for 3 feet then turn 90 degrees to right
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Timer;

/**
 *
 * @author schroed
 */
public class AutoModeDriveToTest extends AutoMode {
    
    private int StepCount = 0;

    public AutoModeDriveToTest(Team177Robot robot) {
        super(robot);
    }

    public void autoContinous() {
        switch(StepCount) {
            case 0:
                //Drive Forward 3 feet
                if(DriveTo(36,0,0.5)) {
                    StepCount++;
                }
                break;        
            case 1:
                //Turn to right
                if(DriveTo(robot.locator.GetX(),-36,0)) {
                    StepCount++;
                }
                break;        
            default:
                robot.drive.tankDrive(0.0,0.0);                                
        }                
        Timer.delay(0.01);
    }

    public void autoPeriodic() {
    }
       
    public String getName() {
        return "DriveTo Test";
    }
}

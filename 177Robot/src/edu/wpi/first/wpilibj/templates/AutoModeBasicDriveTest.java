/*
 * Basic Automode to test frame work, drive forward for 1/2 sec then turn 90 degrees to right
 */
package edu.wpi.first.wpilibj.templates;

/**
 *
 * @author schroed
 */
public class AutoModeBasicDriveTest extends AutoMode {

    int StepCount = 0;
    int SubStepCount = 0;
    double startHeading;
    
    public AutoModeBasicDriveTest(Team177Robot robot) {
        super(robot);
        System.out.println("AutoModeBasicDriveTest Constructor");
    }

    public void autoPeriodic() {
         System.out.println("Step Cnt"+StepCount);
         switch(StepCount) {
            case 0:
                //Drive forward for approx 1/2 second
                robot.drive.tankDrive(-0.5,-0.5);
                if(SubStepCount > 50) {
                    StepCount++;
                    SubStepCount = 0;
                }
                SubStepCount++;
                break;
            case 1:
                if(((startHeading-robot.locator.GetHeading())+360)%360 > 90) {
                    robot.drive.tankDrive(0.0,0.0);
                    StepCount++;
                } else {
                    robot.drive.tankDrive(-0.75,+0.75);
                }                
                break;
            default:
                robot.drive.tankDrive(0.0,0.0);
        }
    }

    public void autoInit() {
        StepCount = 0;
        SubStepCount = 0;
    }
    
    public String getName() {
        return "Basic Drive Test";
    }
        
}

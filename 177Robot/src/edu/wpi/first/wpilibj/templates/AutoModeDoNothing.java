/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

/**
 *
 * @author Robotics
 */
public class AutoModeDoNothing extends AutoMode
{
    public AutoModeDoNothing(Team177Robot robot)
    {
	super(robot);
    }
    
    public void autoPeriodic()
    {
	
    }

    public String getName()
    {
	return "Rookie Mode";
    }
    
}

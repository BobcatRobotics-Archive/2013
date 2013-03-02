/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;

/**
 *
 * @author schroed
 */
public class FilteredEncoder extends Encoder {
    
    private final static int AverageCount = 25;
    private double rateRunningAverage[] = new double[AverageCount];
    private int rateAverageIndex = 0;
    
    private double distanceRunningAverage[] = new double[AverageCount];
    private int distanceAverageIndex = 0;
    private double lastRate;
    
      
   /**
     * Encoder constructor.
     * Construct a Encoder given a and b channels assuming the default module.
     * @param aChannel The a channel digital input channel.
     * @param bChannel The b channel digital input channel.
     */
    public FilteredEncoder(final int aChannel, final int bChannel) {
        super(aChannel, bChannel, false);
        
         for(int i = 0; i < AverageCount; i++) {            
            rateRunningAverage[i] = 0;
            distanceRunningAverage[i] = 0;
        }
    }
    
   /**
     * Get the filtered rate of the encoder.
     * Units are distance per second as scaled by the value from setDistancePerPulse().
     * Assumes this function is called with some periodicity
     *
     * @return The filtered rate of the encoder.
     */
     public double getRate() {        
        double average = 0;
        rateRunningAverage[rateAverageIndex] = super.getRate();
        rateAverageIndex = (rateAverageIndex+1)%AverageCount;
        
        for (int i = 0; i < AverageCount; i++) {
            average += rateRunningAverage[i];
        }
        lastRate = average / AverageCount;
        return lastRate;
    }
     
     public double getLastRate() {
         return lastRate;
     }
    
     /**
     * Get the current rate of the encoder.
     * Units are distance per second as scaled by the value from setDistancePerPulse().
     * 
     * Modified to use a longer period...
     *
     * @return The current rate of the encoder.
     */
    private double lastDistance;
    private double lastTime;
    public double getRateFromDistance() {
        double distance = getDistance();
        double time = Timer.getFPGATimestamp();
        double deltaDist = lastDistance - distance;
        double deltaTime = lastTime - time;
        lastDistance = distance;
        lastTime = time;
        return deltaDist / deltaTime;
    }
    
     /**
     * Get the filtered distance the robot has driven since the last reset.
     *
     * @return The distance driven since the last reset as scaled by the value from setDistancePerPulse().
     */
     public double getDistance() {
        double average = 0;
        distanceRunningAverage[distanceAverageIndex] = super.getDistance();
        distanceAverageIndex = (distanceAverageIndex+1)%AverageCount;
        
        for (int i = 0; i < AverageCount; i++) {
            average += distanceRunningAverage[i];
        }
        return average / AverageCount;          
     }
    
   
}

/**
 * Basic Vision System to Identify FRC Vision Targets
 * Based On: Various OpenCV Sample Code
 * Tuned For: FRC 2012
 * REQUIRES: OpenCV (RaspberryPi Compatible)
 * By Daniel Cohen and FRC Team 177
 */

#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <iostream>
#include <algorithm>
#include <stdio.h>
#include <math.h>

using namespace std;
using namespace cv;

/// Global Variables
Mat rgbimg; Mat templ; Mat result; Mat hsvimg; 
Mat binimg; Mat img; Mat erodeimg; Mat gencanny; Mat canny;


/// Function Headers
void MatchAndExtract();


/**
 * @function main
 */
int main( int argc, char *argv[] )
{
  /// Load image and template
  rgbimg = imread( argv[1], 1 );
  templ = imread( argv[2], 1 );

  /// Convert from rgb/bgr to hsv
  cvtColor(rgbimg, img, CV_BGR2HSV);
  /// Threshold by HSV values
  inRange(rgbimg, Scalar(200, 80, 0), Scalar(300, 255, 200), binimg);
  
  ///Filter noise in image
  Mat element = getStructuringElement(0, Size(4,4));
  erode(binimg, erodeimg, element);

  /// Convert image back to bgr/rgb
  cvtColor(erodeimg, img, CV_GRAY2BGR);
 
  /// Execute Function
  MatchAndExtract();

  return 0;
}

/**
 * @function MatchAndExtract
 * @brief Match VT and Extract Corners
 */
void MatchAndExtract()
{
  /// Copy source image to display
  Mat img_display;
  img.copyTo( img_display );

  /// Create the result matrix
  int result_cols =  img.cols - templ.cols + 1;
  int result_rows = img.rows - templ.rows + 1;

  result.create( result_cols, result_rows, CV_32FC1 );

  /// Do the Matching and Normalize
  matchTemplate( img, templ, result, 4 );
  normalize( result, result, 0, 1, NORM_MINMAX, -1, Mat() );

  /// Localizing the best match with minMaxLoc
  double minVal; double maxVal; Point minLoc; Point maxLoc;
  Point matchLoc;

  minMaxLoc( result, &minVal, &maxVal, &minLoc, &maxLoc, Mat() );
  matchLoc = maxLoc;

  /// Crop image to ROI
  Rect ROI(Point(matchLoc.x, matchLoc.y), Point((matchLoc.x + templ.cols), (matchLoc.y + templ.rows)));
  /// Crop color image
  Mat croppedimg(img, ROI);
  /// Crop grayscale image
  Mat croppedgray(erodeimg, ROI);

  /// Generate blank result matrix
  Mat dst = Mat::zeros(croppedimg.size(), CV_32FC1);
  Mat dst_norm;

  /// Assign constants for Harris Corner algorithm
  int blockSize = 2;
  int apertureSize = 3;
  double k = 0.04;

  /// Locate corners and normalize
  cornerHarris(croppedgray, dst, blockSize, apertureSize, k, BORDER_DEFAULT);
  normalize( dst, dst_norm, 0, 255, NORM_MINMAX, CV_32FC1, Mat() );
  
  /// Initialize vectors for x and y coordinates of corners
  vector<int> cornerxs;
  vector<int> cornerys;
  
  /// Locate coordinates of corners above threshold, put in vectors
  for( int j = 0; j < dst_norm.rows ; j++ ) {
    for( int i = 0; i < dst_norm.cols; i++ ) {
            if( (int) dst_norm.at<float>(j,i) > 100 ) {
	      				cornerxs.push_back(i);
	      				cornerys.push_back(j);
              }
          }
     }

  /// Find indices of four corners
  int minXind = min_element(cornerxs.begin(), cornerxs.end()) - cornerxs.begin();
  int maxXind = max_element(cornerxs.begin(), cornerxs.end()) - cornerxs.begin();
  int minYind = min_element(cornerys.begin(), cornerys.end()) - cornerys.begin();
  int maxYind = max_element(cornerys.begin(), cornerys.end()) - cornerys.begin();
  
  /// Print coordinates to STOUT 
  cout << "P1: (" << cornerxs[minXind] << ", " << cornerys[minXind] << ")" << endl;
	cout << "P2: (" << cornerxs[maxXind] << ", " << cornerys[maxXind] << ")" << endl;
	cout << "P3: (" << cornerxs[minYind] << ", " << cornerys[minYind] << ")" << endl;
	cout << "P4: (" << cornerxs[maxYind] << ", " << cornerys[maxYind] << ")" << endl;


  return;
}



/*
 * Basic Vision System to Identify FRC Vision Targets Contours
 * Based On: Various OpenCV Sample Code
 * Tuned For: FRC 2012
 * REQUIRES: OpenCV (ARM Compatible)
 * By Daniel Cohen and FRC Team 177
 */

/* TODO: Modify to run with libCURL images from camera */

#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <iostream>
#include <string>
#include <cmath>
#include "ServerSocket.h"
#include "SocketException.h"

#define PI 3.141592653589793238462643383279502884197

using namespace std;
using namespace cv;

/// Global Variables
Mat rgbimg; Mat templ; Mat result; Mat dilateimg;
Mat binimg; Mat img; Mat erodeimg; Mat gencanny; Mat canny;
Size dilatesize(20, 20);

// Constants
const int width_in = 24;
const int height_in = 18;


/// Function Headers
void RunServer(string file);
string process();
template <class T> string convertNum(T number);

/**
 * @function main
 */
int main( int argc, char** argv)
{
	//Initialize the Server 
	RunServer(argv[1]);

}

/**
* @function process
**/
string process(string file, int p0, int d0) {

  /// Load image and template
  rgbimg = imread( file, 1 );

  /// Threshold by BGR values
  inRange(rgbimg, Scalar(200, 80, 0), Scalar(255, 255, 204), binimg);
  
  ///Filter noise in image
  Mat element = getStructuringElement(0, Size(4,4));
  erode(binimg, erodeimg, element);
  Mat dilateelement = getStructuringElement(0, dilatesize);
  dilate(erodeimg, dilateimg, dilateelement);


  Mat canny_output;
  vector<vector<Point> > contours;
  vector<Vec4i> hierarchy;


  /// Detect edges using canny
  Canny( dilateimg, canny_output, 200, 400, 3 );
  /// Find contours
  findContours( canny_output, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0) );

  /// Find biggest contour
  int maxi = 0;
  double maxsize = contourArea(contours[0]);
  double area = 0; 
  for (int i = 0; i < contours.size(); i++) {
    area = contourArea(contours[i]);
    cout << "i: " << i << " Area: " << area << endl;
    if (area > maxsize) {
         maxsize = area;
         maxi = i;
    }
  }

  /// Find rectangle of best fit
  RotatedRect minRect = minAreaRect(Mat(contours[maxi]));

  /// Lots of math to find distance and bearing to target
  double xdist = abs(320 - minRect.center.x); 
  double ydist = abs(240 - minRect.center.y);
  double d = (p0*d0)/minRect.size.height;
  double m_width_in = (minRect.size.width/minRect.size.height)*height_in;
  double psi = atan(m_width_in/d);
  double alpha = asin(d/width_in*sin(psi));
  double theta = (PI/2) - psi - alpha;
  double deltax = atan(((height_in/minRect.size.height)*xdist)/d);
  double deltay = atan(((height_in*ydist)/minRect.size.height)/d);

  /// Print pertinant results
  /*cout << "W: " << minRect.size.width - dilatesize.width  << endl;
  cout << "H: " << minRect.size.height - dilatesize.height << endl;
  cout << "Center (X, Y): (" << minRect.center.x << ", " << minRect.center.y << ")" << endl;*/

  /// Return pertinant results to Server thread
  string output = "$" + convertNum<double>(d) + "," + convertNum<double>(deltax) + "," + convertNum<double>(deltay) + "$";

  return output;
}

void RunServer(string file) {

try {
  /// Set up server socket
  ServerSocket server (10177);
  
  while (true) {
	ServerSocket new_sock;
	server.accept(new_sock);
	
	  try {
	    while(true) { 
	    /// Echo results of current image as fast as possible  	    
	    new_sock << process(file, 63, 115);
            }
          } catch (SocketException&) {}
   }
} catch (SocketException& e) { cout << e.description() << endl; }

}


/// Generic function to convert number to string
template <class T>
string convertNum(T number)
{
   stringstream ss;//create a stringstream
   ss << number;//add number to the stream
   return ss.str();//return a string with the contents of the stream
}

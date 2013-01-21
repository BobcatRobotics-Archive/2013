/*
 * Basic Vision System to Identify FRC Vision Targets Contours
 * Based On: Various OpenCV Sample Code
 * Tuned For: FRC 2012
 * REQUIRES: OpenCV (ARM Compatible)
 * By Daniel Cohen and FRC Team 177
 */



#include "CVHeader.h"
#include "ServerSocket.h"
#include "SocketException.h"
#include "CurlUtils.h"

/// Global Variables
Mat rgbimg; Mat templ; Mat result; Mat dilateimg;
Mat binimg; Mat img; Mat erodeimg; Mat gencanny; Mat canny;
Size dilatesize(20, 20);


// Constants
const int width_in = 24;
const int height_in = 18;


/// Function Headers
void RunServer();
string process(int p0, int d0);
template <class T> string convertNum(T number);
double toDegrees(double angle);

/**
 * @function main
 */
int main( int argc, char** argv)
{
        cout << "Starting 177 Vision Server" << endl;
	cout << "Format: Distance, DeltaX, DeltaY" << endl;
	cout << "Version: 2.1" << endl;

	//Initialize the Server 
	RunServer();

}

/**
* @function process
**/
string process(int p0, int d0) {
  /// Print diagnostic message
  cout << "Starting frame..." << endl;

  /// Load image from camera (via curl)
  rgbimg = CurlUtils::fetchImg("http://10.1.77.11/axis-cgi/jpg/image.cgi");

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

  /// Check to see if any targets in the image (Avoids SEGFAULT!)
  if(contours.size() > 0) {
      /// Find biggest contour
      int maxi = 0;
      double maxsize = contourArea(contours[0]);
      double area = 0; 
      for (int i = 0; i < contours.size(); i++) {
        area = contourArea(contours[i]);
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
      string output = convertNum<double>(d) + "," + convertNum<double>(toDegrees(deltax)) + "," + convertNum<double>(toDegrees(deltay));

      return output;
  } else {
    cout << "Error: No Target in View" << endl;
    return "";
  }

}

void RunServer() {

try {
  /// Set up server socket
  ServerSocket server (10177);
  
  while (true) {
	ServerSocket new_sock;
	server.accept(new_sock);
	
	  try {
	    while(true) { 
	        /// Echo results of current image as fast as possible
		new_sock << process(63, 115);
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


/// Converts Radians to Degrees
double toDegrees(double angle)
{
  return angle*(180/PI);
}

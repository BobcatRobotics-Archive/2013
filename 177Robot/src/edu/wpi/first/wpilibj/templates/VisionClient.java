/*
 * VisionClient in Thread Form
 * Use to fetch and parse vision data
 */

import java.util.*;
import java.net.*;
import java.io.*;

public class VisionClient implements Runnable
{
    double distance, deltax, deltay;

	public void run()
	{
		try
		{
		        /// Open socket
			Socket sock = new Socket("10.1.77.91", 10177);
			byte[] buffer = new byte[1000];
			
			/// When data is available
			while (sock.getInputStream().read(buffer) > 0)
			{
			        /// Convert bytes to strings
				String packet = new String(buffer, "UTF-8");
      				/// Split on commas
				String[] data = packet.split(",");
				distance = Double.parseDouble(data[0]);
				deltax = Double.parseDouble(data[1]);
				deltay = Double.parseDouble(data[2]);
       			}
			
		}
		catch(IOException e)
		{
			System.out.println("ERROR: " + e);
		}
	}
}
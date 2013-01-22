#
# Custom Makefile for Team 177 VisionServer
# Does not compile depreciated files
#

# Calibration Variables
P0=63
D0=115
THRESH=4000

all: visionserver

visionserver: visionserver.o curlutils.o serversocket.o socket.o
	g++ visionserver.o curlutils.o serversocket.o socket.o -o VisionServer `pkg-config --libs opencv` -lcurl

visionserver.o: VisionServer.cpp
	g++ -c -DP0=$(P0) -DD0=$(D0) -DTHRESH=$(THRESH) VisionServer.cpp -o visionserver.o

curlutils.o: CurlUtils.cpp
	g++ -c CurlUtils.cpp -o curlutils.o

serversocket.o: ServerSocket.cpp
	g++ -c ServerSocket.cpp -o serversocket.o

socket.o: Socket.cpp
	g++ -c Socket.cpp -o socket.o

clean:
	rm -rf *.o VisionServer

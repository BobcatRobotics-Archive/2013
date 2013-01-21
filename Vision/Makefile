#
# Custom Makefile for Team 177 VisionServer
# Does not compile depreciated files
#

all: visionserver

visionserver: visionserver.o curlutils.o serversocket.o socket.o
	g++ visionserver.o curlutils.o serversocket.o socket.o -o VisionServer `pkg-config --libs opencv` -lcurl

visionserver.o: VisionServer.cpp
	g++ -c VisionServer.cpp -o visionserver.o

curlutils.o: CurlUtils.cpp
	g++ -c CurlUtils.cpp -o curlutils.o

serversocket.o: ServerSocket.cpp
	g++ -c ServerSocket.cpp -o serversocket.o

socket.o: Socket.cpp
	g++ -c Socket.cpp -o socket.o

clean:
	rm -rf *.o VisionServer

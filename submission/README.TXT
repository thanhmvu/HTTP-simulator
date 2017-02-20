# HTTP-simulator

PROJECT TITLE: HTTP Simulator  
PURPOSE OF PROJECT: Simulate a client-server transaction  
VERSION: 1.0  
DATE: 02/19/2017  
AUTHORS: Thanh Vu & Ha Vu  

### HOW TO RUN:  
The source files are located in ./src/ folder. In order to run the app, you should run the two .jar files at the root folder. First, run the server:
> java -jar HTTPSimulator-Server.jar  

Then open a new terminal and run the experiment: 
> java -jar HTTPSimulator-ExpController.jar

### WHAT TO EXPECT:  
While the program is running, the print-out of "[CA]" means ClientApp is taking; "[TL]" means TransportLayer.  

On the client side, the program prints the flow of activities in the network and any response packets client receives. It also displays the final web page when all embedded files are retrieved. On the server side, the program prints out the request packets that server receives. 

Once the experiment is done, the output will be correctness.csv and expResults.csv at the root folder.  
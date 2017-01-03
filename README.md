# CS_Capstone
This is a simple chat room type program developed and coded as an improved and expanded version of a previous team project, which can be found [here](https://github.com/canevaa/CapstoneProject), I was part of working on. This old project was more of a proof of concept than anything else and provided a good base moving forwards.

Some features the old project was missing were:
* A nice user interface
* Simplifed setup and use
* Portability
* Multiple user interaction
* Multiple user connections to the server

These were some of the features that I was hoping to add and bring into the new, improved project.

------
#### Setup and use
**Java 8 must be installed on both the client and the server.**

To use this project there needs to be a server setup and running, this server either needs to be on your local network or be addressable via IP address and have the desired port port-forwarded to the server.

To run the server on a desired port the port must be passed in via the command-line as shown below (batch included file for use on Windows machines):
```batch
java -jar Server.jar 5001
```
If a port is not specified or the specified port is invaild the server deafaults to running on port 5000.

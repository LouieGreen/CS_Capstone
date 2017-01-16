# CS_Capstone
This is a simple chat room type program developed and coded as an improved and expanded version of a previous team project, which can be found [here](https://github.com/canevaa/CapstoneProject), I was part of working on. This old project was more of a proof of concept than anything else and provided a good base moving forwards.

These were some of the features that I was hoping to add and bring into the new, improved project:
* A nice user interface
* Simplifed setup and use
* Portability
* Multiple user interaction
* Multiple user connections to the server

#### The goal of this project
This project is meant to be coded completely in Java and not to use any outside libraries, this is so that all work done is my own and the project requires no extra setup or maintenance.

## Setup and use
**Java 8 must be installed on both the client and the server. Depending on the OS of the computer running the server the Java JDK might be necessary.**

To use this project there needs to be a server setup and running, this server either needs to be on your local network or be addressable via IP address and have the desired port port-forwarded to the server.

To run the server on a desired port the port with the desired password, they must be passed in via the command-line as shown below, the order does not matter (batch included file for use on Windows machines):
```
java -jar Server.jar                    - port 5000, no password
java -jar Server.jar "PORT"             - port specified, no password
java -jar Server.jar "PASSWORD"         - port 5000, specified password
java -jar Server.jar "PASSWORD" "PORT"  - specified password, specified port
java -jar Server.jar "PORT" "PASSWORD"  - specified port, specified password
```

If a port is not specified or the specified port is invaild the server deafaults to running on port 5000. If the password is not specified the password defaults to no password.

## Hiddden Features
When in messaging client:

**CTRL + M:** mutes notification noises, such as getting new messages and users connecting/disconnecting.

**Escape:** clears all chat messages from the screen.

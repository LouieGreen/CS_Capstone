package application;

public class User {
	private String userServer = null;
	private String userName = null;
	private String userColor = null; //Black, Blue, Green, Purple, Red
	private int userPort = 5000;
	
	 //////////////////////////////////////////////
	//				Constructors				//
   //////////////////////////////////////////////
	
	public User(){};
	
	public User(String server, String name, String color, int port) {
		userServer = server;
		userName = name;
		userColor = color;
		userPort = port;
	}
	
	 //////////////////////////////////////////////
	//				Public Methods				//
   //////////////////////////////////////////////
	
   ///////			  Setters			///////
	
	public void setServer(String server){
		userServer = server;
	}
	
	public void setName(String name){
		userName = name;
	}
	
	public void setColor(String color){
		userColor = color;
	}
	
	public void setPort(int port){
		userPort = port;
	}
	
   ///////			  Getters			///////
	
	public String getServer(){
		return userServer;
	}
	
	public String getName(){
		return userName;
	}
	
	public String getColor(){
		return userColor;
	}
	
	public int getPort(){
		return userPort;
	}
	
	@Override
	public String toString(){
		return ("Username: " + userName + "\n" + 
				"Server  : " + userServer + "\n" +
				"Color   : " + userColor);
	}
}

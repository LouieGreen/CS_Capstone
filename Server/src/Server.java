import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashSet;

public class Server {

    private static int portNum = 5000;
    private static String ipAddress;
    private static String connectPassword = "";
	private static RSAPublicKey serverPubKey;
	private static RSAPrivateKey serverPrivKey;
	private static byte[] serverAesKey;
	private static SecureRandom random = new SecureRandom();;
    private static HashSet<String> names = new HashSet<String>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    public static void main(String[] args) throws Exception {
        //all of this block is just figuring out password/port with no special order
    	ipAddress = InetAddress.getLocalHost().getHostAddress();
        //if both password and port are specified
    	if(args.length == 2) {
	    	try {
	    		portNum = Integer.parseInt(args[0]);
	    	}
	    	catch(Exception e) {
	    		connectPassword = args[0];
	    	}
	    	try {
	    		portNum = Integer.parseInt(args[1]);
	    	}
	    	catch(Exception e) {
	    		connectPassword = args[1];
	    	}
	    	System.out.println("Server started, with password \"" + connectPassword + "\": " + ipAddress + ":" + portNum + "\n");
    	}
        //if just one of the options is specified
    	else if(args.length == 1) {
    		try {
	    		portNum = Integer.parseInt(args[0]);
	    		System.out.println("Server started, no password: " + ipAddress + ":" + portNum + "\n");
	    	}
	    	catch(Exception e) {
	    		connectPassword = args[0];
	    		System.out.println("Server started, with password \"" + connectPassword + "\": " + ipAddress + ":" + portNum + "\n");
	    	}
    	}
    	else {
    		System.out.println("Server started, no password: " + ipAddress + ":" + portNum + "\n");
    	}

        //hash password for checking against user password
        if(connectPassword.lenth() != 0) {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(pass.getBytes());
			String connectPassword = new String(messageDigest.digest());
		}

        //setup server keys, 2048 bit RSA
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        serverPubKey = (RSAPublicKey) keyPair.getPublic();
        serverPrivKey = (RSAPrivateKey) keyPair.getPrivate();

        //create SecureRandom object with random seed
        SecureRandom random = new SecureRandom();
	    byte seed[] = random.generateSeed(20);
		random.setSeed(seed);

        //generate AES key and IV
		serverAesKey = AES.generateKey();

        //create socket on specified port number
        ServerSocket listener = new ServerSocket(portNum);

        //create a new handler object that starts up a thread with the socket
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        }
        finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
    	private boolean isFailedConnection;
        private String username;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private RSAPublicKey userPubKey;
		private byte[] userAesKey;
        private String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                //send server PubKey as string encoded as Base64
                out.println(Base64.getEncoder().encodeToString(serverPubKey.getEncoded()));

                //get user public key
                byte[] userPubKeyBytes = Base64.getDecoder().decode(in.readLine());
				userPubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(userPubKeyBytes));

				//get password before sending AES key
				String userEnteredPassword = RSA.decrypt(serverPrivKey, in.readLine());

                //tell user if they were right or not
				if(!userEnteredPassword.equals(connectPassword)) {
					out.println(RSA.encrypt(userPubKey, "INCORRECT-PASSWORD"));
                	isFailedConnection = true;
                }
				else {
					out.println(RSA.encrypt(userPubKey, "CORRECT-PASSWORD"));
				}

                if(!isFailedConnection) {
                	//send and receive AES keys
                    out.println(RSA.encrypt(userPubKey, Base64.getEncoder().encodeToString(serverAesKey)));
                    userAesKey = Base64.getDecoder().decode(RSA.decrypt(serverPrivKey, in.readLine()));

	                //get user info and add them to list
	                sendMessage(out, "REQUESTNAME");
	                username = AES.decrypt(userAesKey, in.readLine());

                    //ckeck for duplicte usernames
	                for(String existingUser: names) {
	                	if(existingUser.split(" ")[0].equals(username.split(" ")[0])) {
	                		sendMessage(out, "DUPLICATE-USERNAME");
	                		isFailedConnection = true;
	                	}
	                }

	                if(!isFailedConnection) {
                        //tell the user they are conneceted
		                names.add(username);
		                sendMessage(out, "CONNECTED");

		                //sends all connected usernames to new user
		                for(String n: names) {
		                	sendMessage(out, "NEWUSER " + n);
		                }

		                //send new username to all other users
		                for(PrintWriter w: writers) {
		                	sendMessage(w, "NEWUSER " + username);
		                	sendMessage(w, "USER-UPDATE-MESSAGE " + username + " " + "has connected to the server.");
		                }

		                //add new user to list of currently connected users
		                writers.add(out);

                        //write to server terminal the user who connected and when
		                System.out.println(username.split(" ")[0] + " connected at - " + timeStamp);

		                // Accept messages from this client and broadcast them.
		                while (true) {
                            //while the socket is not closed
		                	if(!socket.isClosed()) {
			                    String input = AES.decrypt(userAesKey, in.readLine());
			                    if (input == null) {
			                        return;
			                    }
                                //this if handles normal user messaging, falls to else when user closes chat
			                    if(!input.startsWith("CLOSING-CLIENT")) {
				                    for (PrintWriter writer : writers) {
				                    	sendMessage(writer, "MESSAGE " + username + " " + input);
				                    }
			                    }
                                //once the user leave we need to deal with telling other users and closing the socket
			                    else {
                                    //not sure I need "isFailedConnection" test here, might be vestigial after changes
			                    	if (username != null && !isFailedConnection) {
			    	                    names.remove(username);
			    	                }
                                    //tell each user the user disconnected and to remove them from their name list
			                    	for (PrintWriter writer : writers) {
			    	            		sendMessage(writer, "DISCONNECTED-MESSAGE " + username + " " + "has disconnected from the server.");
			    	            		sendMessage(writer, "REMOVEUSER " + username);
			    	                }
                                    //write to terminal that the user disconnected
			    	            	System.out.println(username.split(" ")[0] + " disconnected at - "+ timeStamp);
                                    //close the stream and remove the writer
			    	                if (out != null && !isFailedConnection) {
			    	                	out.close();
			    	                    writers.remove(out);
			    	                }
                                    //close the socket
			    	                try {
			    	                    socket.close();
			    	                }
			    	                catch (IOException e1) {
			    	                	e1.printStackTrace();
			    	                }
			                    }
		                	}
		                	else {
		                		return;
		                	}
		                } //end while true
	                } //end if(!isFailedConnection)
                } //end if(!isFailedConnection)
            } //end try
            catch (Exception e) {
            	e.printStackTrace();
            }
        } //end run
    } //end Handler

    //abstracted and automated the AES encryption process for each message
    private static void sendMessage(PrintWriter out, String message) {
    	out.println(AES.encrypt(serverAesKey, AES.getInitVector(random), message));
    }
}

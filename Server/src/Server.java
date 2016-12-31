import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashSet;

public class Server {

    private static int portNum = 5000;
	private static RSAPublicKey serverPubKey;
	@SuppressWarnings("unused")
	private static RSAPrivateKey serverPrivKey;
    private static HashSet<String> names = new HashSet<String>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    
    public static void main(String[] args) throws Exception {
    	if(args.length > 0) {
    		try{
    			portNum = Integer.parseInt(args[0]);
    		}
    		catch(Exception e) {
    			System.out.println("Input a proper port number. Defaulting to port 5000.");
    		}
    	}
    	
        System.out.println("The chat server is running on port: " + portNum);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        serverPubKey = (RSAPublicKey) keyPair.getPublic();
        serverPrivKey = (RSAPrivateKey) keyPair.getPrivate();
        
        ServerSocket listener = new ServerSocket(portNum);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private RSAPublicKey userPubKey;
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
			    try {
					userPubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(userPubKeyBytes));
				} 
			    catch (Exception e) {
					e.printStackTrace();
				}
                
                out.println(RSA.encrypt(userPubKey, "a."));
                
                //get user info and add them to list
                out.println("REQUESTNAME");
                name = in.readLine();
                names.add(name);

                out.println("CONNECTED");
                
                //sends all connected usernames to new user
                for(String n: names){
                	out.println("NEWUSER " + n);
                }
                
                //send new username to all other users
                for(PrintWriter w: writers){
                	w.println("NEWUSER " + name);
                	w.println("USER-UPDATE-MESSAGE " + name + " " + "has connected to the server.");
                }
                
                //add new user to list of current users
                writers.add(out);
                
                System.out.println(name.split(" ")[0] + " connected at - "+ timeStamp);
                
                // Accept messages from this client and broadcast them. Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + " " + input);
                    }
                }
            } catch (IOException e) {
            	for (PrintWriter writer : writers) {
            		writer.println("DISCONNECTED-MESSAGE " + name + " " + "has disconnected form the server.");
                    writer.println("REMOVEUSER " + name);
                }
            	System.out.println(name.split(" ")[0] + " disconnected at - "+ timeStamp);
            } finally {
                // This client is going down! Remove its name and its print writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                	//catch nothing!!!!!
                }
            }
        }
    }
}

package application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ChatController {
	
	private final User user = UserInfoController.getUser();
	private final KeyCombination kb = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHIFT_DOWN);
	private ArrayList <String> nameList = new ArrayList<>();
	private ArrayList <String> colorList = new ArrayList<>();
	private BufferedReader in;
	private Socket socket;
	private PrintWriter out;
	
	private RSAPublicKey userPubKey;
	private RSAPrivateKey userPrivKey;
	private RSAPublicKey serverPubKey;
	
	@SuppressWarnings("unused")
	private byte[] serverAesKey;
	private byte[] userAesKey;
	
	///// @FXML Objects /////
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private AnchorPane root;
    @FXML private GridPane grid;
    @FXML private ScrollPane namesScroll;
    @FXML private ScrollPane chatScroll;
    @FXML protected TextFlow chatFlow;
    @FXML protected TextFlow namesFlow;
    @FXML protected TextArea input;
    
    @FXML 
    void initialize() {
        
        Task<Void> task = new Task<Void>() {
			@Override
			public Void call() throws Exception {
				// Make connection and initialize streams
				@SuppressWarnings("unused")
				String junk = null;
				
				Scanner scanner = null;
			    String serverAddress = user.getServer();
			    try {
			    	//generate and store public and private keys
			    	KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			        keyGen.initialize(2048);
			        KeyPair keyPair = keyGen.generateKeyPair();
			        userPubKey = (RSAPublicKey) keyPair.getPublic();
			        userPrivKey = (RSAPrivateKey) keyPair.getPrivate();
			        
			        //connect to server and setup streams
					socket = new Socket(serverAddress, user.getPort());
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					out = new PrintWriter(socket.getOutputStream(), true);
				} catch (Exception e) {
					Platform.runLater (() -> input.setText("Failed to connect to: " + serverAddress + ":" + user.getPort()));
				}
			    
			    //		Set up RSA Keys		//
			    //get server public key ..... re-create key from bytes ..... send our public key
			    byte[] serverPubKeyBytes = Base64.getDecoder().decode(in.readLine());
			    serverPubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(serverPubKeyBytes));
			    out.println(Base64.getEncoder().encodeToString(userPubKey.getEncoded()));
			    
			    //generate AES key and IV
				userAesKey = AES.generateKey();
			    byte[] iv = AES.generateInitVector();
				
				//send and receive AES keys
			    serverAesKey = Base64.getDecoder().decode(RSA.decrypt(userPrivKey, in.readLine()));
			    out.println(RSA.encrypt(serverPubKey, Base64.getEncoder().encodeToString(userAesKey)));
			    
			    /*
			    System.out.println("Server: " + Base64.getEncoder().encodeToString(serverAesKey));
			    System.out.println("Client: " + Base64.getEncoder().encodeToString(userAesKey));
			    String encString = in.readLine();
			    System.out.println(AES.decrypt(serverAesKey, Base64.getDecoder().decode(encString.substring(0, 24)), encString.substring(24)));
			    System.out.println("Enc message: " + Base64.getEncoder().encodeToString(iv) + AES.encrypt(userAesKey, iv, "This is a test message to the server encrypted in AES."));
			    out.println(Base64.getEncoder().encodeToString(iv) + AES.encrypt(userAesKey, iv, "This is a test message to the server encrypted in AES."));
			    */
			    
			    // Process all messages from server, according to the protocol.
			        while (true) {
			            String line = in.readLine();
			            
				        if(line != null) {
				        	scanner = new Scanner(line);
				        	
				            if (line.startsWith("MESSAGE") || line.startsWith("USER-UPDATE-MESSAGE") || line.startsWith("DISCONNECTED-MESSAGE")) {
				            	addChatMessage(scanner);
				            }
				            else if (line.startsWith("CONNECTED")) {
				                input.setEditable(true);
				            }
				            else if (line.startsWith("REQUESTNAME")) {
				                out.println(user.getName() + " " + user.getColor());
				            }
				            else if(line.startsWith("NEWUSER")){
				            	junk = scanner.next(); //eats NEWUSER
				            	nameList.add(scanner.next()); //gets name
				            	colorList.add(scanner.next()); //gets color
				            	
				            	Platform.runLater (() -> updateUserList());
				            }
				            else if(line.startsWith("REMOVEUSER")){
				            	junk = scanner.next(); //eats REMOVEUSER
				            	String nameToRemove = scanner.next(); //get user name
				            	
				            	junk = scanner.nextLine(); //clear rest of line
				            	
				            	int i = nameList.indexOf(nameToRemove);
				            	nameList.remove(i);
				            	colorList.remove(i);
				            	
				            	Platform.runLater (() -> updateUserList());
				            }
				            scanner.close();
				        } //end if statement
			        } //end while statement
			    }
			};
			Thread th = new Thread(task);
			th.setDaemon(true);
			th.start();
        
        input.setOnKeyPressed(e -> {
        	if (e.getCode() == KeyCode.ESCAPE){
        		chatFlow.getChildren().clear();
			}
			
			if(kb.match(e)){
				input.appendText("\n");
			}
			
			if((e.getCode() == KeyCode.ENTER) && !kb.match(e)){
				String userText = (input.getText());
				out.println(userText);
				input.clear();
				e.consume();
				chatScroll.setVvalue(1);
			}
        });
    } //end init
    
    private void updateUserList() {
    	namesFlow.getChildren().clear();
		
		for(int i=0; i<nameList.size(); i++){ 
			Text t = new Text();
			t.setText(nameList.get(i) + "\n");
			t.setStyle("-fx-fill: " + colorList.get(i) + ";");
			namesFlow.getChildren().add(t);
		}
	}
    
    private void addChatMessage(Scanner scanner) {
    	String header = scanner.next(); //eats header message
    	String inUserName = scanner.next(); // gets name
    	String inUserColor = scanner.next(); //gets color
    	String inUserMessage = scanner.nextLine().trim(); //gets message
    	
    	Text t;
    	if(header.equals("MESSAGE")) {
    		t = new Text(inUserName + ": " +  inUserMessage + "\n"); //create text object with message
    	}
    	else {
    		t = new Text(inUserName + " " + inUserMessage + "\n"); //create text object with message
    	}
    	t.setStyle("-fx-fill: " + inUserColor + ";"); //set color
    	Platform.runLater (() -> {
    		chatFlow.getChildren().add(t); //add to chat
    		chatScroll.setVvalue(1); //set scroll to bottom so it scrolls with text
    	});
    }
}

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
	
	@SuppressWarnings("unused")
	private RSAPublicKey serverPubKey;

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private AnchorPane root;
    @FXML
    private GridPane grid;
    @FXML
    private ScrollPane chatScroll;
    @FXML
    protected TextFlow chatFlow;
    @FXML
    private ScrollPane namesScroll;
    @FXML
    protected TextFlow namesFlow;
    @FXML
    protected TextArea input;
    
    @FXML
    void initialize() {
        assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'chat.fxml'.";
        assert grid != null : "fx:id=\"grid\" was not injected: check your FXML file 'chat.fxml'.";
        assert chatScroll != null : "fx:id=\"chatScroll\" was not injected: check your FXML file 'chat.fxml'.";
        assert chatFlow != null : "fx:id=\"chatFlow\" was not injected: check your FXML file 'chat.fxml'.";
        assert namesScroll != null : "fx:id=\"namesScroll\" was not injected: check your FXML file 'chat.fxml'.";
        assert namesFlow != null : "fx:id=\"namesFlow\" was not injected: check your FXML file 'chat.fxml'.";
        assert input != null : "fx:id=\"input\" was not injected: check your FXML file 'chat.fxml'.";
        
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
			    
			    System.out.println(RSA.decrypt(userPrivKey, in.readLine()));
			    
			    // Process all messages from server, according to the protocol.
			        while (true) {
			            String line = in.readLine();
			            if (line.startsWith("REQUESTNAME")) {
			                out.println(user.getName() + " " + user.getColor());
			            }
			            else if(line.startsWith("NEWUSER")){
			            	scanner = new Scanner(line);
			            	
			            	junk = scanner.next(); //eats NEWUSER
			            	nameList.add(scanner.next()); //gets name
			            	colorList.add(scanner.next()); //gets color
			            	
			            	scanner.close();
			            	
			            	Platform.runLater (() -> updateUserList());
			            }
			            else if (line.startsWith("CONNECTED")) {
			                input.setEditable(true);
			            }
			            else if(line.startsWith("USER-UPDATE-MESSAGE")){
			            	scanner = new Scanner(line);
			            	
			            	junk = scanner.next(); //eats CONNECTED-MESSAGE
			            	String inUserName = scanner.next(); // gets name
			            	String inUserColor = scanner.next(); //gets color
			            	String inUserMessage = scanner.nextLine().trim(); //gets message
			            	
			            	Text t = new Text(inUserName + " " + inUserMessage + "\n"); //create text object with message
			            	t.setStyle("-fx-fill: " + inUserColor + ";"); //set color
			            	Platform.runLater (() -> {
			            		chatFlow.getChildren().add(t); //add to chat
			            		chatScroll.setVvalue(1); //set scroll to bottom so it scrolls with text
			            	});
			            }
			            else if(line.startsWith("DISCONNECTED-MESSAGE")) {
			            	scanner = new Scanner(line);
			            	
			            	junk = scanner.next(); //eats DISCONNECTED-MESSAGE
			            	String inUserName = scanner.next(); // gets name
			            	String inUserColor = scanner.next(); //gets color
			            	String inUserMessage = scanner.nextLine().trim(); // gets message
			            	
			            	Text t = new Text(inUserName + " " + inUserMessage + "\n"); //create text object with message
			            	t.setStyle("-fx-fill: " + inUserColor + ";"); //set color
			            	Platform.runLater (() -> {
			            		chatFlow.getChildren().add(t); //add to chat
			            		chatScroll.setVvalue(1); //set scroll to bottom so it scrolls with text
			            	});
			            }
			            else if(line.startsWith("REMOVEUSER")){
			            	scanner = new Scanner(line);
			            	junk = scanner.next(); //eats REMOVEUSER
			            	String nameToRemove = scanner.next(); //get user name
			            	
			            	junk = scanner.nextLine(); //clear rest of line
			            	
			            	int i = nameList.indexOf(nameToRemove);
			            	nameList.remove(i);
			            	colorList.remove(i);
			            	
			            	Platform.runLater (() -> updateUserList());
			            }
			            else if (line.startsWith("MESSAGE")) {
			            	scanner = new Scanner(line);
			            	
			            	junk = scanner.next(); //eat MESSAGE
			            	String inUserName = scanner.next(); //gets name
			            	String inUserColor = scanner.next(); //gets color
			            	String inUserMessage = scanner.nextLine().trim(); //gets message
			            	
			            	Text t = new Text(inUserName + ": " +  inUserMessage + "\n"); //create text object with message
			            	t.setStyle("-fx-fill: " + inUserColor + ";"); //set color
			            	Platform.runLater (() -> {
			            		chatFlow.getChildren().add(t); //add to chat
			            		chatScroll.setVvalue(1); //set scroll to bottom so it scrolls with text
			            	});
			            	
			            	scanner.close();
			            }
			        }
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
    }
    
    private void updateUserList() {
    	namesFlow.getChildren().clear();
		
		for(int i=0; i<nameList.size(); i++){
			Text t = new Text();
			t.setText(nameList.get(i) + "\n");
			t.setStyle("-fx-fill: " + colorList.get(i) + ";");
			namesFlow.getChildren().add(t);
		}		
	}
}
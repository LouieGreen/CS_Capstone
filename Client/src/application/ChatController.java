package application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class ChatController {

	private final User user = UserInfoController.getUser();
	private final KeyCombination kb = new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_ANY);
	private ArrayList <String> nameList = new ArrayList<>();
	private ArrayList <String> colorList = new ArrayList<>();
	private BufferedReader in;
	private Socket socket;
	private boolean muted = false;
	private static PrintWriter out;

	private ArrayList<String> previousMessages = new ArrayList<>();
	private int position = 0;
	private int pressedPrevious = 0;

	private RSAPublicKey userPubKey;
	private RSAPrivateKey userPrivKey;
	private RSAPublicKey serverPubKey;

	private byte[] serverAesKey;
	private byte[] userAesKey;
	private SecureRandom random = new SecureRandom();

	///// @FXML Objects /////
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private AnchorPane root;
    @FXML private GridPane grid;
    @FXML private ScrollPane namesScroll;
    @FXML private ScrollPane chatScroll;
    @FXML private TextFlow chatFlow;
    @FXML private TextFlow namesFlow;
    @FXML private TextArea input;

    @FXML
    void initialize() {

		//all of this is in a task because that is the way I figured out how Java does concurrency, this gets handed off to be its own thread. this thread handles all the recieving of messages
        Task<Void> task = new Task<Void>() {
			@Override
			public Void call() throws Exception {
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

			    //send password over to server
			    out.println(RSA.encrypt(serverPubKey, user.getPassword()));
			    System.out.println(user.getPassword());
			    String isCorrectOrNot = RSA.decrypt(userPrivKey, in.readLine());
			    if(isCorrectOrNot.equals("INCORRECT-PASSWORD")) {
			    	goBackToInfoController("Incorrect password, try again.");
			    }

			    //create SecureRandom object with random seed
			    byte seed[] = random.generateSeed(20);
				random.setSeed(seed);

			    //generate AES key and IV
				userAesKey = AES.generateKey();

				//send and receive AES keys
			    serverAesKey = Base64.getDecoder().decode(RSA.decrypt(userPrivKey, in.readLine()));
			    out.println(RSA.encrypt(serverPubKey, Base64.getEncoder().encodeToString(userAesKey)));

			    // Process all messages from server, according to the protocol. (pretty self explanatory)
		        while (true) {
		            String line = AES.decrypt(serverAesKey, in.readLine());

			        if(line != null) {
			        	scanner = new Scanner(line);

			            if (line.startsWith("MESSAGE")) {
			            	addChatMessage(scanner);
			            	if(!muted && !line.startsWith("MESSAGE " + user.getName())) {
			            		playSound("notification.wav");
			            	}
			            }
			            else if(line.startsWith("USER-UPDATE-MESSAGE") || line.startsWith("DISCONNECTED-MESSAGE")) {
			            	addChatMessage(scanner);
			            	if(!muted) {
			            		playSound("connection.wav");
			            	}
			            }
			            else if (line.startsWith("CONNECTED")) {
			                input.setEditable(true);
			            }
			            else if (line.startsWith("REQUESTNAME")) {
			            	sendMessage(out, user.getName() + " " + user.getColor());
			            }
			            else if(line.startsWith("NEWUSER")) {
			            	scanner.next(); //eats NEWUSER
			            	nameList.add(scanner.next()); //gets name
			            	colorList.add(scanner.next()); //gets color

			            	Platform.runLater (() -> updateUserList());
			            }
			            else if(line.startsWith("REMOVEUSER")) {
			            	scanner.next(); //eats REMOVEUSER
			            	String nameToRemove = scanner.next(); //get user name
			            	scanner.nextLine(); //clear rest of line

			            	int i = nameList.indexOf(nameToRemove);
			            	nameList.remove(i);
			            	colorList.remove(i);

			            	Platform.runLater (() -> updateUserList());
			            }
			            else if(line.startsWith("DUPLICATE-USERNAME")) {
			            	goBackToInfoController("Duplicate username, enter another.");
			            }
			            scanner.close();
			        } //end if statement
		        } //end while statement
		    }
		};
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();

		//this is the thread that handles user input
        input.setOnKeyPressed(e -> {
			//excape clears chat
        	if (e.getCode() == KeyCode.ESCAPE) {
        		chatFlow.getChildren().clear();
			}

			// ctrl+m mutes and unmutes chat notifications
			if(kb.match(e)){
				muted = !muted;
			}

			//arrow up moves through old sent messages
			if (e.getCode() == KeyCode.UP) {
				if(!previousMessages.isEmpty() && position >= 1 && position <= previousMessages.size()) {
					if(pressedPrevious == 2) {
						position--;
					}
					position--;
					input.clear();
					input.setText(previousMessages.get(position));
					input.positionCaret(input.getText().length());
					pressedPrevious=1;
				}
			}

			//arrow down move through old sent messages
			if (e.getCode() == KeyCode.DOWN) {
				if(!previousMessages.isEmpty() && position >= 0 && position < previousMessages.size()) {
					if(pressedPrevious==1) {
						position++;
					}
					input.clear();
					input.setText(previousMessages.get(position));
					input.positionCaret(input.getText().length());
					position++;
					pressedPrevious=2;
				}
			}

			//enter sends typed messages
			if((e.getCode() == KeyCode.ENTER) && !kb.match(e)) {
				String userText = input.getText();
				if(userText.trim().length() > 0) {
					previousMessages.add(userText);
					sendMessage(out, userText);
					chatScroll.setVvalue(1);
					position=previousMessages.size();
					pressedPrevious=0;
				}
				e.consume();
				input.clear();
			}
        });

		//I needed a neat way to close a client session, so when the user requests to close the window, one more message is sent letting the server know
        Platform.runLater(() -> {
	        Stage stage = (Stage) root.getScene().getWindow();

			stage.setOnCloseRequest(e -> {
				if(out != null){
					sendMessage(out, "CLOSING-CLIENT");
					out.close();
				}
				Platform.exit();
			});
        });

    } //end init

	//when a user connects or leaves the userList needs to be updated... Clear list, then loop over list and readd them
    private void updateUserList() {
    	namesFlow.getChildren().clear();

		for(int i=0; i<nameList.size(); i++){
			Text t = new Text();
			t.setText(nameList.get(i) + "\n");
			t.setStyle("-fx-fill: " + colorList.get(i) + ";");
			namesFlow.getChildren().add(t);
		}
	}

	//this function adds messages to the chat pane
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
    } //addChatMessage

	//this function will kick the user back to the uerInfo window
    private void goBackToInfoController(String text) {
		//sets the appropiate flag in the userInfo window and resets the other
    	if(text.startsWith("Duplicate")) {
    		if(UserInfoController.getIncorrectPassword()) {
    			UserInfoController.setIncorretPassword(false);
    		}
    		UserInfoController.setDuplicateUser(true);
    	}
    	else if(text.startsWith("Incorrect")) {
    		if(UserInfoController.getDuplicateUser()) {
    			UserInfoController.setDuplicateUser(false);
    		}
    		UserInfoController.setIncorretPassword(true);
    	}

		//loads the userInfo window and closes this one
    	Platform.runLater (() -> {
    		try {
            	Stage stage = (Stage) root.getScene().getWindow();
				stage.close();
				URL fxmlUrlInfo = this.getClass().getClassLoader().getResource("resources/userInfo.fxml");
				Pane info = FXMLLoader.<Pane> load(fxmlUrlInfo);
				stage.setScene(new Scene(info));
				stage.setTitle("ERROR: " + text);
				stage.show();
    		}
    		catch(Exception e) {
    			e.printStackTrace();
    		}
    	});
    } //end goBackToInfoController

	//this was abstracted out so the same piece of code wasn't being copied over and over
    private void sendMessage(PrintWriter out, String message) {
    	out.println(AES.encrypt(userAesKey, AES.getInitVector(random), message));
    }

	//plays the various sounds depending on what is passed in
    private static synchronized void playSound(String file) {
    	new Thread(new Runnable() {
    		public void run() {
    			try {
    				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(this.getClass().getClassLoader().getResource("resources/" + file));
    				Clip clip = AudioSystem.getClip();
    			    clip.open(audioInputStream);
    			    clip.start();
    			}
    			catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    	}).start();
    } //end playSound
} //end class

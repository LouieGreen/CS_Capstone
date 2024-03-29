package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class UserInfoController {

	// create the global objects for future use //
	private static User user = new User();
	private static boolean failedConnection = false;
	private static boolean isIncorretPassword = false;
	private boolean passwordIsHidden = false;

	///// @FXML Objects /////
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private AnchorPane root;
    @FXML private GridPane grid;
    @FXML private Label serverLabel;
    @FXML private Label usernameLabel;
    @FXML private Label portLabel;
    @FXML private TextField serverText;
    @FXML private TextField userText;
    @FXML private TextField portText;
    @FXML private TextField passwordText;
    @FXML private TextField plainTextPassword;
    @FXML private Label colorLabel;
    @FXML private RadioButton blackButton;
    @FXML private ToggleGroup color;
    @FXML private RadioButton blueButton;
    @FXML private RadioButton greenButton;
    @FXML private RadioButton purpleButton;
    @FXML private RadioButton redButton;
    @FXML private Label saveLabel;
    @FXML private RadioButton saveButton;
    @FXML private ToggleGroup save;
    @FXML private RadioButton deletButton;
    @FXML private Button submit;
    @FXML private Label notFinished;
    @FXML private Label passwordLabel;
    @FXML private CheckBox showPassword;

    @FXML
    void initialize() {


    	String operatingSystem = System.getProperty("os.name");
    	File settingsFile = new File(".savedSettings.txt");
    	Path pathToSettingsFile = FileSystems.getDefault().getPath(".savedSettings.txt");

    	try {
    		String colorFromFile = null;
    		Scanner input = null;

    		//check if files settingsFile exists and if so load in settings
    		if(settingsFile.exists()){
    			input = new Scanner(settingsFile);
    			serverText.setText(input.nextLine());
    			userText.setText(input.nextLine());
    			portText.setText(input.nextLine());
    			colorFromFile = input.nextLine();

    			if(colorFromFile.equals("Black")){ blackButton.setSelected(true); }
        		else if(colorFromFile.equals("Blue")){ blueButton.setSelected(true); }
        		else if(colorFromFile.equals("Green")){ greenButton.setSelected(true); }
        		else if(colorFromFile.equals("Purple")){ purpleButton.setSelected(true); }
        		else if(colorFromFile.equals("Red")){ redButton.setSelected(true); }

       			saveButton.setSelected(true);
    			input.close();
    		}

    		//sets text from color selection radio buttons
    		blackButton.setUserData("Black");
    		blueButton.setUserData("Blue");
    		greenButton.setUserData("Green");
    		purpleButton.setUserData("Purple");
    		redButton.setUserData("Red");

    		//allows toggle of hidden password for password checking
    		showPassword.setFocusTraversable(false);
    		plainTextPassword.setManaged(passwordIsHidden);
    		plainTextPassword.setVisible(passwordIsHidden);
    		plainTextPassword.textProperty().bindBidirectional(passwordText.textProperty());

			//listener for when the show password button is clicked to reverse it
    		showPassword.setOnAction(e -> {
    			passwordIsHidden = !passwordIsHidden;
    			plainTextPassword.setManaged(passwordIsHidden);
        		plainTextPassword.setVisible(passwordIsHidden);
    		});

			//listener for the submit button click
    		submit.setOnAction(e -> {
    			//check server field is filled in -- check username is filled in -- check that the field isn't just spaces -- check the port field is filled -- check color is selected
    			if(serverText.getText().length() != 0 && userText.getText().length() != 0 && !userText.getText().trim().isEmpty() && portText.getText().length() != 0 && color.getSelectedToggle() != null) {

					//convert port text to integer and set user object info for use later
					//remove spaces from username because they break things
					//get the server name entered
					//get the color the user would like to use

					Integer port = new Integer(portText.getText().trim());
    				user.setPort(port);
    				user.setName(userText.getText().replaceAll(" ", ""));
    				user.setServer(serverText.getText().trim());
    				user.setColor(color.getSelectedToggle().getUserData().toString());

					//password is allowed to be blank so test to see if it is, if not get the users
    				if(passwordText.getText().length() == 0) {
    					user.setPassword(new String(""));
    				}
    				else {
    					user.setPassword(passwordText.getText().trim());
    				}

    				//save settings to hidden file (Windows is picky so a special case for it)
    				if(saveButton.isSelected()) {
    					try {
							//if it is Windows we need to unhide the file before we can change it (if it exists)
    						if(settingsFile.exists() && operatingSystem.contains("Windows")) {
    							Files.setAttribute(pathToSettingsFile, "dos:hidden", false);
    						}
							//write UserInfo to the file
    						PrintWriter p = new PrintWriter(settingsFile);
    						p.println(user.getServer() + "\n" + user.getName() + "\n" + user.getPort() + "\n"  + user.getColor());
    						p.close();
							//if Windows set file to hidden again
    						if(operatingSystem.contains("Windows")) {
    							Files.setAttribute(pathToSettingsFile, "dos:hidden", true);
    						}
    					}
    					catch (FileNotFoundException e1) {
    						e1.printStackTrace();
    					} catch (IOException e1) {
							e1.printStackTrace();
						}
    				}
					//if user selects to delete the settingsFile this deletes it
    				else if(deletButton.isSelected()){
    					if(settingsFile.exists()){
    						settingsFile.delete();
    					}
    				}

					//closes UserInfo stage and attempts to load the Chat stage
    				Stage stage = (Stage) submit.getScene().getWindow();
    				stage.close();
    				try {
						URL fxmlUrlChat = this.getClass().getClassLoader().getResource("resources/chat.fxml");
						Pane chat = FXMLLoader.<Pane> load(fxmlUrlChat);
						stage.setScene(new Scene(chat));
						stage.setTitle("Chat Client: "+ user.getName());
						stage.show();
					}
    				catch (IOException e1) {
						e1.printStackTrace();
					}
    			}
    			else {
					//if the user is missing a field then this is shown
    				notFinished.setOpacity(1);
    			}
    		});

			//these is used for when the user is kicked back from being denied from the server
    		if(failedConnection) {
    			notFinished.setOpacity(1);
    			notFinished.setFont(new Font(16));
    			notFinished.setTextFill(Color.RED);
    	    	notFinished.setText("Duplicate username, enter another.");
    	    	userText.clear();
    	    	Platform.runLater (() ->userText.requestFocus());
        	}
    		else if(isIncorretPassword) {
    			notFinished.setOpacity(1);
    			notFinished.setFont(new Font(16));
    			notFinished.setTextFill(Color.RED);
    	    	notFinished.setText("Incorrect password.");
    		}
        	else {
        		notFinished.setOpacity(0);
        		notFinished.setText("Please fill out fields.");
        	}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	//these are the functions used by the chatcontroller to tell the user what happemed
    protected static void setIncorretPassword(boolean flag){
    	isIncorretPassword = flag;
    }
    protected static boolean getIncorrectPassword() {
    	return isIncorretPassword;
    }
    protected static void setDuplicateUser(boolean flag) {
    	failedConnection = flag;
	}
    protected static boolean getDuplicateUser() {
    	return failedConnection;
    }
	protected static User getUser() {
    	return user;
    }

}

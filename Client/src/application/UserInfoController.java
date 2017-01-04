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

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class UserInfoController {
	
	private static User user = new User();

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private AnchorPane root;
    @FXML
    private GridPane grid;
    @FXML
    private Label serverLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label portLabel;
    @FXML
    private TextField serverText;
    @FXML
    private TextField userText;
    @FXML
    private TextField portText;
    @FXML
    private Label colorLabel;
    @FXML
    private RadioButton blackButton;
    @FXML
    private ToggleGroup color;
    @FXML
    private RadioButton blueButton;
    @FXML
    private RadioButton greenButton;
    @FXML
    private RadioButton purpleButton;
    @FXML
    private RadioButton redButton;
    @FXML
    private Label saveLabel;
    @FXML
    private RadioButton saveButton;
    @FXML
    private ToggleGroup save;
    @FXML
    private RadioButton deletButton;
    @FXML
    private Button submit;
    @FXML
    private Label notFinished;

    @FXML
    void initialize() {
        assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert grid != null : "fx:id=\"grid\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert serverLabel != null : "fx:id=\"serverLabel\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert usernameLabel != null : "fx:id=\"usernameLabel\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert portLabel != null : "fx:id=\"portLabel\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert serverText != null : "fx:id=\"serverText\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert userText != null : "fx:id=\"userText\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert portText != null : "fx:id=\"portText\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert colorLabel != null : "fx:id=\"colorLabel\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert blackButton != null : "fx:id=\"blackButton\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert color != null : "fx:id=\"color\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert blueButton != null : "fx:id=\"blueButton\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert greenButton != null : "fx:id=\"greenButton\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert purpleButton != null : "fx:id=\"purpleButton\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert redButton != null : "fx:id=\"redButton\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert saveLabel != null : "fx:id=\"saveLabel\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert saveButton != null : "fx:id=\"saveButton\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert save != null : "fx:id=\"save\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert deletButton != null : "fx:id=\"deletButton\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert submit != null : "fx:id=\"submit\" was not injected: check your FXML file 'userInfo.fxml'.";
        assert notFinished != null : "fx:id=\"notFinished\" was not injected: check your FXML file 'userInfo.fxml'.";

    	File settingsFile = new File(".savedSettings.txt");
    	boolean settingsFileExists = false;
    		
    	try {
    		String colorFromFile = null;
    		Scanner input = null;
    			
    		//check if files exists
    		if(settingsFile.exists()){
    			settingsFileExists = true;
    			input = new Scanner(settingsFile);
    		}
    			
    		if(settingsFileExists){
    			serverText.setText(input.nextLine());
    		}
    		
    		if(settingsFileExists){
    			userText.setText(input.nextLine());
    		}
    		
    		if(settingsFileExists){
    			portText.setText(input.nextLine());
    		}
    		
    		if(settingsFileExists){
    			colorFromFile = input.nextLine();
    		}
    		
    		if(settingsFileExists && colorFromFile.equals("Black")){
    			blackButton.setSelected(true);
    		}
    		else if(settingsFileExists && colorFromFile.equals("Blue")){
    			blueButton.setSelected(true);
    		}
    		else if(settingsFileExists && colorFromFile.equals("Green")){
    			greenButton.setSelected(true);
    		}
    		else if(settingsFileExists && colorFromFile.equals("Purple")){
    			purpleButton.setSelected(true);
    		}
    		else if(settingsFileExists && colorFromFile.equals("Red")){
    			redButton.setSelected(true);
    		}
    			
    		blackButton.setUserData("Black");
    		blueButton.setUserData("Blue");
    		greenButton.setUserData("Green");
    		purpleButton.setUserData("Purple");
    		redButton.setUserData("Red");
    			

    		if(settingsFileExists && input.nextLine().equals("true")){
    			saveButton.setSelected(true);
    		}
    			
    		if(settingsFileExists){
    			input.close();
    		}
    			
    		submit.setOnAction(e -> {
    			//check server field is filled in --     check username is filled in and that the field isn't just spaces --         check the port field is filled   -- check color is selected
    			if(serverText.getText().length() != 0 && userText.getText().length() != 0 && !userText.getText().trim().isEmpty() && portText.getText().length() != 0 && color.getSelectedToggle() != null) {
    				user.setName(userText.getText().replaceAll(" ", ""));
    				user.setServer(serverText.getText());
    				user.setColor(color.getSelectedToggle().getUserData().toString());
    				
    				Integer port = new Integer(portText.getText().trim());
    				user.setPort(port);
    				
    				//save settings to file
    				if(saveButton.isSelected()) {
    					try {
    						PrintWriter p = new PrintWriter(settingsFile);
    						p.println(user.getServer() + "\n" + user.getName() + "\n" + user.getPort() + "\n"  + user.getColor() + "\n" + saveButton.isSelected());
    						p.close();
    						Path pathToSettingsFile = FileSystems.getDefault().getPath(".savedSettings.txt");
    						Files.setAttribute(pathToSettingsFile, "dos:hidden", true);
    					}
    					catch (FileNotFoundException e1) {
    						e1.printStackTrace();
    					} catch (IOException e1) {
							e1.printStackTrace();
						}
    				}
    				else if(deletButton.isSelected()){
    					if(settingsFile.exists()){
    						settingsFile.delete();
    					}
    					
    				}
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
    				notFinished.setOpacity(1);
    			}
    		});
    	} 
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public static User getUser() {
    	return user;
    }
        
}

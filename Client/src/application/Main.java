package application;
	
import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		
		// load userInfo and show, which goes off into the controller class
		// then once the user clicks the submit button the userInfo stage is closed
		// but the stage info is saved, then the chat pane is loaded and set to the 
		// current scene and shown
		
		try {
			URL fxmlUrlInfo = this.getClass().getClassLoader().getResource("resources/userInfo.fxml");
			Pane info = FXMLLoader.<Pane> load(fxmlUrlInfo);
			
			Scene scene = new Scene(info);
			
			primaryStage.setTitle("Chat Client");
			primaryStage.getIcons().add(new Image("resources/icon32.png")); // borrowed from http://wfarm3.dataknet.com/static/resources/icons/set99/c42baec7.png
			primaryStage.setScene(scene);
			primaryStage.show();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		launch(args);
	}
}

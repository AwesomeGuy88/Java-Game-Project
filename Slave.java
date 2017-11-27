import java.io.IOException;
import java.util.HashSet;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;



//Contains access to the data managed by slave data
//Sets up the screen and displays objects based on stored data
//Object interaction is neglected, simply displays objects
public class Slave extends SlaveData {

    static Scene gameScene;

    private Timeline gameLoop = new Timeline();
    private Canvas canvas;

    private final double secondsPerFrame = config.secondsPerFrame; // the inverse of the desired fps (currently 60fps)

    public void gameStart(Stage theStage) {
    	prepareGame(theStage);
    	prepareActionHandlers();

		KeyFrame kf = new KeyFrame(
            Duration.seconds(secondsPerFrame),
            new EventHandler<ActionEvent>()
            {
                public void handle(ActionEvent ae)
                {
                	String send = "keys: ";
                	for (String k : activeKeys) {
                		send += k + ", ";
                	}
            		gameStart.sendString(send);
                }
            });

        gameLoop.getKeyFrames().add( kf );
        gameLoop.play();

        theStage.setScene(gameScene);
        theStage.show();
    }

    public void prepareGame(Stage theStage) {
        theStage.setTitle( "Master Tanks Online Edition" );
    	activeKeys = new HashSet<String>(); // contains the names of the keys currently pressed
        // use a HashSet so duplicates are not possible

      	canvas = new Canvas( 1024, 768 ); // an image on which we can draw stuff such as text and images

    	// creates a background image on which the canvas is drawn.
        StackPane stack = new StackPane();
        stack.setMaxSize(canvas.getWidth(), canvas.getHeight());
        stack.setStyle("-fx-background-image: url('background.png');");
        stack.getChildren().add(canvas);

    	//Preparing the scene and
    	gameScene = new Scene( stack ); // background stack is our root
    	theStage.setScene( gameScene ); // allows the scene to be displayed

        gameLoop.setCycleCount( Timeline.INDEFINITE );

    }

	private void prepareActionHandlers() // this describes what to do when certain keyboard events occur
    {
    	gameScene.setOnKeyPressed(new EventHandler<KeyEvent>() // when a key is released...
			{
	    		@Override
	    		public void handle(KeyEvent event)
	    		{
	    			if (event.getCode().toString() == "ESCAPE") {
	    				Quit();
            		}
	    			if (!activeKeys.contains(event.getCode().toString())) {
		    			activeKeys.add(event.getCode().toString()); // it is added to the list
	    			}
	    		}
			});

    	gameScene.setOnKeyReleased(new EventHandler<KeyEvent>() // when a key is released...
			{
	    		@Override
	    		public void handle(KeyEvent event)
	    		{
	    			activeKeys.remove(event.getCode().toString()); // it is added to the list
	    		}
			});
    }

	public void Quit() {
		gameLoop.stop();
		try { gameStart.clientSocket.close(); }
		catch (IOException e) { e.printStackTrace(); }
	    Platform.exit();
	}

}

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;


//Contains access to the data managed by slave data
//Sets up the screen and displays objects based on stored data
//Object interaction is neglected, simply displays objects
public class SlaveTesting {

	//Controls
	HashSet<String> activeKeys;
	String l = "0";
	String r = "0";
	String u = "0";
	String d = "0";
	String s = "0";
    private int time = -90;
    private int gameLength = config.gameTime;

    static Scene gameScene;
    private Timeline gameLoop = new Timeline();
    private Canvas canvas;
    private GraphicsContext gc;

	//Objects
	ArrayList<wallSprite> walls = new ArrayList<wallSprite>();
	ArrayList<tankSprite> tanks = new ArrayList<tankSprite>();
	ArrayList<powerUpSprite2> powerUps = new ArrayList<powerUpSprite2>();
	Image bulletImage;
	Image powerImage;
	String[] objectInfo;
	String[] objectPos;

    String input;
    private final double secondsPerFrame = config.secondsPerFrame; // the inverse of the desired fps (currently 60fps)

    public void gameStart(Stage theStage) {
    	prepareGame(theStage);
    	prepareActionHandlers();
    	bulletImage = new Image("bullet.png");
    	powerImage = new Image("powerUp.png");

    	gameStart.sendString("0");
    	input = gameStart.receiveString();

    	int wallC = 0; // for alternating wall colours
    	int tankC = 1; // for tank colours
    	while (!input.equals("start")) {
    		System.out.println("input: "+input);
    		// objectID,objType,x,y,angle,state,alive
    		objectInfo = input.split(",");
    		if (objectInfo[1].equals("1")) {
    			walls.add(new wallSprite(Integer.parseInt(objectInfo[2]),Integer.parseInt(objectInfo[3]),wallC%2+1,objectInfo[0]));
    			wallC++;
    			// TODO walls too high?
    		} else if (objectInfo[1].equals("0")) {
    			tanks.add(new tankSprite(Integer.parseInt(objectInfo[2]),Integer.parseInt(objectInfo[3]),tankC,objectInfo[0]));
    			tankC++;
    			System.out.println("tank made");
    		} else if (objectInfo[1].equals("3")) {
    			powerUps.add(new powerUpSprite2(objectInfo[0],Integer.parseInt(objectInfo[2]),Integer.parseInt(objectInfo[3])));
    		}

        	gameStart.sendString("0");
        	input = gameStart.receiveString();
    		System.out.println(input);
    	}
    	for (wallSprite wall : walls){ wall.render(gc); }
   		for (tankSprite tank : tanks){ tank.render(gc); }
		System.out.println("objects done");

		KeyFrame kf = new KeyFrame(
            Duration.seconds(secondsPerFrame),
            new EventHandler<ActionEvent>()
            {
                public void handle(ActionEvent ae)
                {
               		gameStart.sendString(l+r+u+d+s);
               		System.out.println(l+r+u+d+s);
                	input = gameStart.receiveString();
               		System.out.println(input);
                    gc.clearRect(0, 0, 1024, 768);

               		time++;
                    if (time < 0) {
	                	for (wallSprite wall : walls){ wall.render(gc); }
	               		for (tankSprite tank : tanks){ tank.render(gc); }
	               		for (powerUpSprite2 powerUp : powerUps){ powerUp.render(gc); }

                    	gc.setTextAlign(TextAlignment.CENTER);
				    	gc.setFill(Color.YELLOW);
 				    	//gc.setFont(game.getFont("buttonFont"));
						gc.fillText( Integer.toString(1-time/30),
					            	 Math.round(canvas.getWidth()/2),
					            	 Math.round(canvas.getHeight()/2-5) );
						gc.fillText( "READY!",
				            	 Math.round(canvas.getWidth()/2),
				            	 Math.round(canvas.getHeight()/2-90) );
                    } else if (time < gameLength) {
	                    // scoring
                		gc.setTextAlign(TextAlignment.LEFT);
				        gc.setTextBaseline(VPos.CENTER);
 				    	gc.setFill(Color.YELLOW);
 				    	//TODO gc.setFont(game.getFont("textFont"));
 						//gc.fillText(player1name + " score: " + Integer.toString(tank1Score), 10, 15);
 						//gc.fillText(player2name + " score: " + Integer.toString(tank2Score), 10, 40);
 						gc.fillText("Time remaining: " + Integer.toString((gameLength - time)/30 + 1) + " seconds", 10, 65);
                		objectInfo = input.split("/");

                		try {
	                   		int t = 0; // count to 2 for tanks
	                   		for (String info: objectInfo) {
	               				objectPos = info.split(",");
	                   			if (t<2){
	                   				tanks.get(t).setX(Integer.parseInt(objectPos[1]));
	                   				tanks.get(t).setY(Integer.parseInt(objectPos[2]));
	                   				tanks.get(t).setDirection(Integer.parseInt(objectPos[3]));
	                   				if (objectPos[4].equals("-1")) {
	                   					tanks.get(t).setPowerActive(false);
	                   				} else {
	                   					tanks.get(t).setPowerActive(true);
		                   				tanks.get(t).setPower(Integer.parseInt(objectPos[4]));
	                   				}
	                   			} else if (t==2) {
	                   				if (objectPos[1].equals("1")) {
	                   					gc.drawImage( powerImage, Integer.parseInt(objectPos[2]), Integer.parseInt(objectPos[3]) );
	                   				}
	                   			} else {
	                   				gc.drawImage( bulletImage, Integer.parseInt(objectPos[0]), Integer.parseInt(objectPos[1]) );
	                   			}
                   				t++;
	                   		}
                		} catch (IndexOutOfBoundsException e) {
                			e.printStackTrace();
                		}
                    	for (wallSprite wall : walls){ wall.render(gc); }
                   		for (tankSprite tank : tanks){ tank.render(gc); }

	                    if (gameLength-time < 150) { // countdown
	                    	gc.setTextAlign(TextAlignment.CENTER);
    				    	gc.setFill(Color.YELLOW);
    						gc.fillText( Integer.toString((gameLength - time)/30 + 1),
    					            	 Math.round(canvas.getWidth()/2),
    					            	 Math.round(canvas.getHeight()/2-5) );
	                    }
                	} else {
                		Quit();
                	}
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
        gc = canvas.getGraphicsContext2D(); // we'll use this GraphicsContext object to draw stuff on the canvas

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
		    			if (event.getCode().toString() == "LEFT") {
		    				l = "1";
	            		} if (event.getCode().toString() == "RIGHT") {
	            			r = "1";
	            		} if (event.getCode().toString() == "UP") {
	            			u = "1";
	            		} if (event.getCode().toString() == "DOWN") {
	            			d = "1";
	            		} if (event.getCode().toString() == "SPACE") {
	            			s = "1";
	            		}
	    			}
	    		}
			});

    	gameScene.setOnKeyReleased(new EventHandler<KeyEvent>() // when a key is released...
			{
	    		@Override
	    		public void handle(KeyEvent event)
	    		{
	    			activeKeys.remove(event.getCode().toString()); // it is added to the list
            		if (event.getCode().toString() == "LEFT") {
            			l = "0";
            		} if (event.getCode().toString() == "RIGHT") {
            			r = "0";
            		} if (event.getCode().toString() == "UP") {
            			u = "0";
            		} if (event.getCode().toString() == "DOWN") {
            			d = "0";
            		} if (event.getCode().toString() == "SPACE") {
            			s = "0";
            		}
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

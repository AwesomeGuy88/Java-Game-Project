import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.sql.Timestamp;
import java.util.Date;


public class Master {

    static Scene gameScene;

    private Timeline gameLoop = new Timeline();
    private Canvas canvas;
    private GraphicsContext gc;
    private PrintWriter logFile;
    private boolean isPaused = false;

    private tankSprite tank1;
    private String[] tank1Controls;
    private int tank1Score = 0;
    private String player1name = config.player1name;

    private tankSprite tank2;
    private String tankInput;
    /*  0 – Left
		1 – Right
		2 – Up
		3 – Down
		4 – Spacebar (Shoot)
     */
    private String sendPos = "";
    private int tank2Score = 0;
    private String player2name = "Computer";

    private int newIDs = 104;

    private ArrayList<bulletSprite> bullets = new ArrayList<bulletSprite>();
    private ArrayList<wallSprite> walls = new ArrayList<wallSprite>();
    private HashSet<String> activeKeys; // i'll use this to store the keys that have been pressed

    private powerUpSprite powerUp;
    private MediaPlayer gameSong;

    private int time = -90;
    private int gameLength = config.gameTime;

    private final double secondsPerFrame = config.secondsPerFrame; // the inverse of the desired fps (currently 60fps)

    public void start(Stage theStage) {

    	prepareGame(theStage);
    	prepareActionHandlers();
        createGameObjects(); // must come after prepGame

 ////////////////////////// LOOP

        KeyFrame kf = new KeyFrame(
            Duration.seconds(secondsPerFrame),
            new EventHandler<ActionEvent>()
            {
                public void handle(ActionEvent ae)
                {
                    time++;
                	gameStart.sendString(tank1Score+","+tank2Score);
                	sendPos = "1,"+(int)tank1.getX()+","+(int)tank1.getY()+","+tank1.getDirection()+",";
                	if (tank1.getPowerActive())
                		sendPos += tank1.getPower();
               		else
               			sendPos += -1;
                	sendPos	+= "/2,"+(int)tank2.getX()+","+(int)tank2.getY()+","+tank2.getDirection()+",";
                	if (tank2.getPowerActive())
                		sendPos += tank2.getPower();
               		else
               			sendPos += -1;
                	sendPos += "/3,"+powerUp.getStatus()+","+(int)powerUp.getX()+","+(int)powerUp.getY();
                	for (bulletSprite bullet: bullets) {
                		sendPos += "/"+(int)bullet.getX()+","+(int)bullet.getY();
                	}
                	gameStart.receiveString();
                	gameStart.sendString(sendPos);
                	tankInput = gameStart.receiveString();

                    if (time < 0) {
	                    gc.clearRect(0, 0, 1024, 768);
	                    // render everything
	                    tank1.render(gc);
	                    tank2.render(gc);
	                    for (wallSprite wall : walls){ wall.render(gc); }

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
	                    gc.clearRect(0, 0, 1024, 768);

	                    // scoring
                		gc.setTextAlign(TextAlignment.LEFT);
				        gc.setTextBaseline(VPos.CENTER);
 				    	gc.setFill(Color.YELLOW);
 				    	//TODO gc.setFont(game.getFont("textFont"));
 						gc.fillText(player1name + " score: " + Integer.toString(tank1Score), 10, 15);
 						gc.fillText(player2name + " score: " + Integer.toString(tank2Score), 10, 40);
 						gc.fillText("Time remaining: " + Integer.toString((gameLength - time)/30 + 1) + " seconds", 10, 65);

	                    // player actions
	                    updateTankResponse(tank1, tank1Controls);
	                    tank1.updateVelocity();
	                    tank1.updatePosition(walls);
	                    tank1.render(gc);
	                    if(powerUp.isRetrievedBy(tank1)) {
	                    	tank1.powerUp();
	                    	powerUp.operational = false;
	                    }

                    	updateTankResponse(tank2, tankInput);
	                    tank2.updateVelocity();
	                    tank2.updatePosition(walls);
	                    tank2.render(gc);
                    	if (tank1.tanksCollide(walls, tank2, gc)) {
                    		tank1Score++;
                    		tank2Score++;
	                    }
                    	if(powerUp.isRetrievedBy(tank2)) {
	                    	tank2.powerUp();
	                    	powerUp.operational = false;
	                    }

                    	bulletHandelling();

	                    if(powerUp.operational)
	                    	powerUp.render(gc);
	                    powerUp.powerUpGen(walls);

	                    for (wallSprite wall : walls){ wall.render(gc); }
	                    for (bulletSprite bullet : bullets){ bullet.render(gc); }

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

        theStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
			    logFile.close();
            }
        });

        gameLoop.getKeyFrames().add( kf );
        gameLoop.play();

        theStage.setScene(gameScene);
        theStage.show();
    }

    private void bulletHandelling() {
        //Collisions with bullets
        int i = 0;
        while (i < bullets.size()) {
        	bullets.get(i).updatePosition(walls);
        	if (tank1.isShotBy(bullets.get(i))) {
        		if (tank1.bubbleShield) {
        			tank1.powerDown();
        		} else {
            		tank2Score++;
            		tank1.respawn(walls, tank2, gc);
        		}
        		bullets.remove(i);
        	} else if (tank2.isShotBy(bullets.get(i))) {
        		if (tank2.bubbleShield) {
        			tank2.powerDown();
        		} else {
            		tank1Score++;
            		tank2.respawn(walls, tank1, gc);
        		}
            	bullets.remove(i);
        	} else if (powerUp.operational && powerUp.isShotBy(bullets.get(i))) {
        		powerUp.operational = false;
                bullets.remove(i);
        	} else if (bullets.get(i).counter()) {
            	bullets.remove(i);
        	} else {
            	i++;
        	}
        }
    }

////////////////////////////////////API STRING PREP
// TODO error codes...

    private String getExistingObjects() {
    	// TODO alive variable
    	// excludes walls?????
    	String objects = "[00000000, 0, 1][00000001, 0, 1]";
    	objects += "["+powerUp.getID()+", 3, 1]";
    	for (bulletSprite bullet: bullets) {
    		objects += "["+bullet.getID()+", 2, 1]";
    	}
    	return objects;
    }
    // TODO alive variable
    private String getObject(String objectID) {
    	int objID = Integer.parseInt(objectID);
    	int objType = 0;
    	int angle = 0;
    	int state = 0;
    	String alive = "0";
    	int x = 0;
    	int y = 0;
    	String object = "";
    	// return deets of the objectID
    	if (objID == 0) {
    		objType = 0;
    		x = (int)tank1.getX() + 12;
    		y = (int)tank1.getY() + 12;
    		angle = tank1.getDirection();
    		state = tank1.getPowerUp() + 1;
    		alive = "1";
    	} else if (objID == 1) {
    		objType = 0;
    		x = (int)tank2.getX() + 12;
    		y = (int)tank2.getY() + 12;
    		angle = tank2.getDirection();
    		state = tank2.getPowerUp() + 1;
    		alive = "1";
    	} else if (objectID == powerUp.getID()) {
    		// TODO multiple power ups
    		objType = 3;
    		x = (int)powerUp.getX() + 4;
    		y = (int)powerUp.getY() + 4;
    		angle = 0;
    		state = 0;
    		alive = powerUp.getStatus();
    	} else {
    		// search through all other objects to see which objectID matches up...
    		for (bulletSprite bullet: bullets) {
        		if (objectID == bullet.getID()) {
            		objType = 2;
            		x = (int)bullet.getX() + 4;
            		y = (int)bullet.getY() + 4;
            		angle = 0;
            		state = 0;
            		alive = "1";
        			break;
        		}
        	}
    		for (wallSprite wall: walls) {
        		if (objectID == wall.getID()) {
            		objType = 1;
            		x = (int)wall.getX() + 6;
            		y = (int)wall.getY() + 6;
            		angle = 0;
            		state = 0;
            		alive = "1";
        			break;
        		}
        	}
    	}
    	object = objectID+","+Integer.toString(objType)+","+Integer.toString(x)+","+Integer.toString(y)+","
    			+Integer.toString(angle)+","+Integer.toString(state)+","+alive;
    	return object;
    }
    private String timeLeft() {
    	return Integer.toString(time-gameLength);
    }
    private String gameState() { // TODO what is 'all objects loaded'?
    	if (time < 0) { return "0"; }
    	else if (time < gameLength) { return "2"; }
    	else { return "4"; }
    }
    private String getScores() {
    	return Integer.toString(tank1Score) + "," + Integer.toString(tank2Score);
    }

////////////////////////////////////SET UP THE IN GAME KEYBOARD

    private void prepareActionHandlers() // this describes what to do when certain keyboard events occur
    {
    	gameScene.setOnKeyPressed(new EventHandler<KeyEvent>() // when a key is released...
			{
	    		@Override
	    		public void handle(KeyEvent event)
	    		{
	    			if (event.getCode().toString() == "ESCAPE") {
	    				// TODO remove this?
	    				Quit();
            		}
	    			if (!activeKeys.contains(event.getCode().toString())) {
	    				// write to log file
	    				Date date= new Date();
	    				logFile.printf("%-25s %s%n", new Timestamp(date.getTime()), event.getCode().toString());
		    			activeKeys.add(event.getCode().toString()); // it is added to the list
	    				if (event.getCode().toString() == "P") {
	    					if (isPaused) {
	    						gameLoop.play();
	    						isPaused = false;
	    					} else {
	    						gameLoop.pause();
	    						isPaused = true;
	    				        gc.setTextAlign(TextAlignment.CENTER);
	    				        gc.setTextBaseline(VPos.CENTER);
	    				    	gc.setFill(Color.YELLOW);
	    				    	gc.setFont(new Font(40));

	    						gc.fillText(
	    					            "** PAUSED **",
	    					            Math.round(canvas.getWidth()  / 2),
	    					            Math.round(canvas.getHeight() / 2)
	    					        );
	    					}
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
	    		}
			});
    }

	public void Quit() {
        gameSong.stop();
		gameLoop.stop();
	    logFile.close();
	    gameStart.sendString("quit");
		try { gameStart.clientSocket.close(); }
		catch (IOException e) { e.printStackTrace(); }
	    Platform.exit();
	}

///////////////////////////////////INITIALIZE SCENE AND VARIABLES

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

		try {
			logFile = new PrintWriter("logFile.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		player2name = config.player2name;

    	playSong();
    }

//////////////////////////THE TANK'S REACTION TO KEYBOARD INPUTS

	private void updateTankResponse(tankSprite tank, String[] tankControls) {
		if (activeKeys.contains(tankControls[1])^activeKeys.contains(tankControls[3])) {
			tank.incrementDirection(activeKeys.contains(tankControls[3]), walls, time); // rotate clockwise
		}

		tank.setMovement(activeKeys.contains(tankControls[0])^activeKeys.contains(tankControls[2]), activeKeys.contains(tankControls[0]));

		if (activeKeys.contains(tankControls[4])) {
			tank.shoot(time, "00000"+Integer.toString(newIDs));
			newIDs++;
		}
	}
    /*  0 – Left
		1 – Right
		2 – Up
		3 – Down
		4 – Spacebar (Shoot) */
	private void updateTankResponse(tankSprite tank, String tankControls) {
		int l = Integer.parseInt(tankControls.substring(0,1));
		int r = Integer.parseInt(tankControls.substring(1,2));
		int u = Integer.parseInt(tankControls.substring(2,3));
		int d = Integer.parseInt(tankControls.substring(3,4));
		int s = Integer.parseInt(tankControls.substring(4,5));

		if (l==1^r==1) { tank.incrementDirection(r==1, walls, time); } // rotate clockwise
		tank.setMovement(u==1^d==1, u==1);
		if (s==1) {
			tank.shoot(time, "00000"+Integer.toString(newIDs));
			newIDs++;
		}
	}

///////////////////////////////////CREATE WALLS AND TANKS

    private void createGameObjects() {
    	String objectID, objType, angle, x, y, state, alive;

		tank1 = new tankSprite(50, 340, config.Tank,"00000000");
    	tank1Controls = new String[] {"W", "A", "S", "D", "SPACE", "C"};
    	tank1.loadBullets(bullets);
    	objectID = tank1.getID();
    	objType = "0";
    	x = Integer.toString( (int)tank1.getX() );
    	y = Integer.toString( (int)tank1.getY() );
    	angle = Integer.toString( tank1.getDirection() );
    	state = Integer.toString( tank1.getPowerUp() + 1) ;
    	alive = "1";
    	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
    	gameStart.receiveString();

    	tank2 = new tankSprite(900, 340, 4, "00000001");
    	tank2.loadBullets(bullets);
    	objectID = tank2.getID();
    	objType = "0";
    	x = Integer.toString( (int)tank2.getX() );
    	y = Integer.toString( (int)tank2.getY() );
    	angle = Integer.toString( tank2.getDirection() );
    	state = Integer.toString( tank2.getPowerUp() + 1) ;
    	alive = "1";
    	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
    	gameStart.receiveString();

    	// **************
    	// TODO: concatenate strings in sprite classes
    	powerUp = new powerUpSprite("00000"+Integer.toString(newIDs));
    	newIDs++;
    	objectID = powerUp.getID();
    	objType = "3";
    	x = Integer.toString( (int)powerUp.getX() );
    	y = Integer.toString( (int)powerUp.getY() );
    	angle = "0";
    	state ="1"; // TODO make actual state??
    	alive = powerUp.getStatus();
    	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
    	gameStart.receiveString();

    	// walls...
    	objType = "1";
    	angle = "0";
    	state ="0";
    	alive = "1";
    	if (config.Map == 1) {
	    	for (int i = 0; i < 13; i++){
	        	walls.add(new wallSprite(300-i*12,130,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();

	    	} for (int i = 13; i < 20; i++){
	    		walls.add(new wallSprite(156,142+(i-13)*12,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();
	    	}

	    	for (int i = 20; i < 33; i++){
	    		walls.add(new wallSprite(712+(i-20)*12,130,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();
	    	} for (int i = 33; i < 40; i++){
	    		walls.add(new wallSprite(856,142+(i-33)*12,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();
	    	}

	    	for (int i = 40; i < 53; i++){
	    		walls.add(new wallSprite(300-(i-40)*12,638,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();
	    	} for (int i = 53; i < 60; i++){
	    		walls.add(new wallSprite(156,626-(i-53)*12,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();
	    	}

	    	for (int i = 60; i < 73; i++){
	    		walls.add(new wallSprite(712+(i-60)*12,638,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();
	    	} for (int i = 73; i < 80; i++){
	    		walls.add(new wallSprite(856,626-(i-73)*12,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();
	    	}

	    	int i = 80;
	    	// <3
	    	walls.add(new wallSprite(506,450-20,1,"0000000"+Integer.toString(80+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(518,438-20,1,"0000000"+Integer.toString(81+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(530,426-20,1,"0000000"+Integer.toString(82+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(542,414-20,1,"0000000"+Integer.toString(83+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(554,402-20,1,"0000000"+Integer.toString(84+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(554,390-20,1,"0000000"+Integer.toString(85+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(554,378-20,1,"0000000"+Integer.toString(86+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(554,366-20,1,"0000000"+Integer.toString(87+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(542,354-20,1,"0000000"+Integer.toString(88+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(530,342-20,1,"0000000"+Integer.toString(89+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(518,342-20,1,"0000000"+Integer.toString(90+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;

	    	walls.add(new wallSprite(494,438-20,1,"0000000"+Integer.toString(91+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(482,426-20,1,"0000000"+Integer.toString(92+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(470,414-20,1,"0000000"+Integer.toString(93+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(458,402-20,1,"0000000"+Integer.toString(94+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(458,390-20,1,"0000000"+Integer.toString(95+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(458,378-20,1,"0000000"+Integer.toString(96+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(458,366-20,1,"0000000"+Integer.toString(97+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(470,354-20,1,"0000000"+Integer.toString(98+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(482,342-20,1,"0000000"+Integer.toString(99+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(494,342-20,1,"0000000"+Integer.toString(100+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
	    	walls.add(new wallSprite(506,354-20,1,"0000000"+Integer.toString(101+2)));
        	objectID = walls.get(i).getID();
        	x = Integer.toString( (int)walls.get(i).getX() );
        	y = Integer.toString( (int)walls.get(i).getY() );
        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
        	gameStart.receiveString();
        	i++;
    	} else if (config.Map == 2) {

			for (int i = 0; i < 56; i++){
		    	walls.add(new wallSprite(180+i*12,310,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();
			}
	    	for (int i = 0; i < 12; i++){
				walls.add(new wallSprite(180+46*12,310-(i+1)*12,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();
			} // 68

			for (int i = 0; i < 56; i++){
				walls.add(new wallSprite(180+i*12,768-310,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();
			}
	    	for (int i = 0; i < 12; i++){
				walls.add(new wallSprite(180+10*12,768-310+(i+1)*12,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();
			} // 136

	    	for (int i = 0; i < 12; i++){
				walls.add(new wallSprite(180+10*12,i*12,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();
			} // 148

	    	for (int i = 0; i < 12; i++){
				walls.add(new wallSprite(180+46*12,768-i*12,i%2+1,"0000000"+Integer.toString(i+2)));
	        	objectID = walls.get(i).getID();
	        	x = Integer.toString( (int)walls.get(i).getX() );
	        	y = Integer.toString( (int)walls.get(i).getY() );
	        	gameStart.sendString(objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive);
	        	gameStart.receiveString();
			} // 160
    	}

    	gameStart.sendString("0");
    	gameStart.receiveString();
    }

//////////////////////////Prepare Media files

	private void playSong() {
	    final URL songUrl = getClass().getResource("gameSong.mp3");
        final Media media = new Media(songUrl.toString());
        gameSong = new MediaPlayer(media);
        gameSong.setVolume(0);//TODO temp
        gameSong.play();
        gameSong.setCycleCount(MediaPlayer.INDEFINITE);
	}
}




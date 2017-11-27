import javafx.scene.text.Text;

//Configuration file
//This file contains variables that modify game settings

public class config {
	// [online gameMode: 1 = master, 2 = slave]
	public static int gameMode = 0;

	public static int gameTime = 3600; // the number of frames that a game will last for 1800

	// which map is chosen, 0 is no walls
	public static int Map = 1;

	// which tank is chosen (0 to 3)
	// TODO implement pgup pgdn tank cycling
	public static int Tank = 0;

	//The index of tanks that are available to the user
	public static int unlockedTanks = 3;

	//Power up variables
	public static int tankSpeed = 3;

	public static final int shootDelay = 10; // the number of frames between each bullet shot

	//Bullet speed
	public static int bulletSpeed = 16;

	public static int bulletLifespan = 80; // the number of frames the bullet will exist for

	public static final int rotateDelay = 3; // the number of frames before each increment of the tank's rotation
	public static final int rotateIncrements = 16; // 360/this = the angle that the tank rotates per increment

	public static final double secondsPerFrame = 0.0333; // the inverse of the desired fps (currently 60fps)

	public static final int tankInvun = 30; // number of frames that the tank is invulnerable after spawning

	public static String player1name = "Player 1";
	public static String player2name = "Player 2";

	public static final int powerUpLifespan = 400; // frames before power up sprite disappears

	public static int currentScore;

	//HIGHSCORES DATA
	public static Text[] names = new Text[10];
    public static Text[] scores = new Text[10];

}

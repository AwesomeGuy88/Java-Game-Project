import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Random;

////////////////////////
//    TODO:::: multiple powerUpSprite functionality
////////////////////////

public class powerUpSprite2 extends Sprite
{

	static Random R = new Random();
	private int x;
	private int y;
	static double powerUpLifespan = config.powerUpLifespan;
	boolean operational;
	int active;
	private int counter = 0;
	boolean activated;
	tankSprite currentTank;

	public powerUpSprite2(String objID, int x, int y)
    {
		this.objID = objID.substring(objID.length()-8);

    	// initialize the powerup object, should not be active
        positionX = x;
        positionY = y;
        image = new Image("powerUp.png");
        width = image.getWidth(); // for collision detection
        height = image.getHeight();
        operational = true;

        xVertices = new double[] {positionX + width, positionX + width, positionX, positionX};
        yVertices = new double[] {positionY, positionY + height, positionY + height, positionY};
        // 0 = top right, 1 = bottom right, 2 = bottom left, 3 = top left
    }

	//Returns false if power up has reached its lifespan. Needs to be called once every frame
	public boolean lifespan() {

		if(counter >= config.powerUpLifespan) {
			operational = false;
		}
		counter++;

		return operational;
	}

}

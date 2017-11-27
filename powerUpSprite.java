import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Random;

////////////////////////
//    TODO:::: multiple powerUpSprite functionality
////////////////////////

public class powerUpSprite extends Sprite
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

	public powerUpSprite(String objID)
    {
		this.objID = objID.substring(objID.length()-8);

    	// initialize the powerup object, should not be active
        positionX = 100;
        positionY = 350;
        image = new Image("powerUp.png");
        width = image.getWidth(); // for collision detection
        height = image.getHeight();
        operational = true;

        xVertices = new double[] {positionX + width, positionX + width, positionX, positionX};
        yVertices = new double[] {positionY, positionY + height, positionY + height, positionY};
        // 0 = top right, 1 = bottom right, 2 = bottom left, 3 = top left
    }



	//Creates power ups in random locations and at random times, spawning variables available in config
	public void powerUpGen(ArrayList<wallSprite> walls) {
		boolean validPos = true;
		x = R.nextInt(976);
		y = R.nextInt(720);

		active++;
		if(active == powerUpLifespan) {
			active = 0;
			operational = false;
	        positionX = x;
	        positionY = y;
			xVertices = new double[] {positionX + width, positionX + width, positionX, positionX};
	        yVertices = new double[] {positionY, positionY + height, positionY + height, positionY};
			for (wallSprite wall : walls){
				if (wall.intersects(this) && this.intersects(wall)) { // rotation is 0 which is parallel to the walls
		            validPos = false;
		        }
		    }

			if(validPos) {
		        operational = true;
			}
		}

		//Despawning the effects of the power up after 15 seconds
		if(activated) {
			counter++;
			if(counter >= powerUpLifespan) {
				currentTank.powerDown();
				counter = 0;
				activated = false;
			}
		}
	}


	public boolean isShotBy(bulletSprite bullet) {
		return this.intersects(bullet);
	}

	public boolean isRetrievedBy(tankSprite tank) {
		if(this.intersects(tank) & operational == true) {
			activated = true;
			operational = false;
			currentTank = tank;
			return true;
		} else {
			return false;
		}
	}

	public String getStatus() {
		if (operational)
			return "1";
		else
			return "0";
	}
}

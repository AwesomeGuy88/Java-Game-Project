import java.util.ArrayList;

import javafx.scene.image.Image;

public class bulletSprite extends Sprite
{

    private double velocityX;
    private double velocityY;
    private double speed = config.bulletSpeed; // bullet speed
    private int counter = 0;
	private int lifespan = config.bulletLifespan; // the number of frames the bullet will exist for
	private int rotateIncrements = config.rotateIncrements; // 360/this = the angle that the tank rotates per increment

	public bulletSprite(double x, double y, int direction, String objID) {
		this.objID = objID.substring(objID.length()-8);

		image = new Image("bullet.png");
        width = image.getWidth(); // for collision detection
        height = image.getHeight();
        positionX = x + 20 + 28* Math.sin(Math.PI * direction*2/rotateIncrements);
        positionY = y + 20 + 28* -1 * Math.cos(Math.PI * direction*2/rotateIncrements);
        velocityX = speed * Math.sin(Math.PI * direction*2/rotateIncrements);
        velocityY = speed * -1 * Math.cos(Math.PI * direction*2/rotateIncrements);

        xVertices = new double[] {positionX + width, positionX + width, positionX, positionX};
        yVertices = new double[] {positionY, positionY + height, positionY + height, positionY};
        // 0 = top right, 1 = bottom right, 2 = bottom left, 3 = top left
	}

    public void updatePosition(ArrayList<wallSprite> walls)
    {
        positionX += velocityX;
        for (int i = 0; i < 4; i++) { xVertices[i] += velocityX; }
        if (outOfBounds()) {
            positionX -= velocityX;
            for (int i = 0; i < 4; i++) { xVertices[i] -= velocityX; }
            velocityX = velocityX * -1;
        } else {
	        for (wallSprite wall : walls){
	        	if (wall.intersects(this) || outOfBounds()) {
	                positionX -= velocityX;
	                for (int i = 0; i < 4; i++) { xVertices[i] -= velocityX; }
	                velocityX = velocityX * -1;
	        	}
	        }
        }

        positionY += velocityY;
        for (int i = 0; i < 4; i++) { yVertices[i] += velocityY; }
        if (outOfBounds()) {
            positionY -= velocityY;
            for (int i = 0; i < 4; i++) { yVertices[i] -= velocityY; }
            velocityY = velocityY * -1;
        } else {
	        for (wallSprite wall : walls){
	        	if (wall.intersects(this)) {
	                positionY -= velocityY;
	                for (int i = 0; i < 4; i++) { yVertices[i] -= velocityY; }
	                velocityY = velocityY * -1;
	        	}
	    	}
        }
    }

    public boolean counter() {
    	counter++;
    	return counter > lifespan;
    }
}

import javafx.scene.image.Image;

public class wallSprite extends Sprite
{
	public wallSprite(int x, int y, int type, String objID) {
		this.objID = objID.substring(objID.length()-8);

		image = new Image("wallImage" + type + ".png");
        width = image.getWidth();
        height = image.getHeight();
        positionX = x;
        positionY = y;

        xVertices = new double[] {positionX + width, positionX + width, positionX, positionX};
        yVertices = new double[] {positionY, positionY + height, positionY + height, positionY};
        // 0 = top right, 1 = bottom right, 2 = bottom left, 3 = top left
	}
}

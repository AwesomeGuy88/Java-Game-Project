import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Sprite {
	protected Image image;
    protected double positionX;
    protected double positionY;
    protected double width;
    protected double height;
    // the hit-box vertex coordinates
    protected double[] xVertices;
    protected double[] yVertices;

    protected String objID;
    public String getID() {return objID;}
    // could have more than 8 digits (more 0s)
    // 0-1 tanks
    // 2-103 walls
    // 104+ bullets and powerUps

    public void render(GraphicsContext gc)
    {
        gc.drawImage( image, positionX, positionY );
    }

    public void drawVertices(GraphicsContext gc) {
    	// for debugging
    	gc.setFill(Color.WHITE);
    	gc.fillOval(xVertices[0], yVertices[0], 2, 2);
    	gc.setFill(Color.TEAL);
    	for(int i = 1; i < 4; i++){ gc. fillOval(xVertices[i], yVertices[i], 2, 2); }
    }

    public boolean outOfBounds() {
    	return positionX < 0 || positionX+width > 1024
    			|| positionY < 0 || positionY+height > 768;
    }

    public double getX() {return positionX;}
    public double getY() {return positionY;}
    public void setX(double x) {positionX = x;}
    public void setY(double y) {positionY = y;}
    public double getXVertex(int i) {return xVertices[i];}
    public double getYVertex(int i) {return yVertices[i];}

    // checks if any of the sprite s' vertices are inside this sprite
    public boolean intersects(Sprite s)
    {
    	// uses the method of separation axis from here: http://elancev.name/oliver/2D%20polygon.htm
    	// finds out if any of sprite s' vertices are within this sprite

    	double xVectorA; // vector A
    	double yVectorA;
    	double xVectorB; // vector B
    	double yVectorB;
    	double ALength;
    	double scalarProjection; // the sum of the scalar projections of vector j on vector i
    	int toSide; // for each of sprite s' vertices to one side of the vector being checked, this variable is incremented
    	// 2 vectors to be checked for each sprite
    	// assumes there is always 4 vertices and the first and that non-adjacent vectors are parallel (i.e. always makes a rectangle)
    	for (int i = 1; i < 4; i+=2){
    		// treat vertex 0 as the origin, vector A = vertex i - vertex 0
    		toSide = 0;
    		xVectorA = xVertices[i] - xVertices[0];
    		yVectorA = yVertices[i] - yVertices[0];
    		ALength = Math.sqrt(xVectorA*xVectorA + yVectorA*yVectorA); // |A|
        	for (int j = 0; j < 4; j++){
    			// vector B = vertex j relative to vertex 0 of this
        		xVectorB = s.getXVertex(j) - xVertices[0];
        		yVectorB = s.getYVertex(j) - yVertices[0];
        		scalarProjection = ( xVectorA*xVectorB + yVectorA*yVectorB ) // A.B
	    							/ ALength; // |A|
            	if (ALength < scalarProjection)
            		toSide++;
            	if (scalarProjection < 0) // the scalar projection is within the vector
            		toSide--;
    		}
    		if (toSide == 4 || toSide == -4)
    			return false; // all vertex projections are to one side of vector A
    	}
    	return true;
    }
}










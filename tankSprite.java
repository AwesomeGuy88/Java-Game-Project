
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import java.util.ArrayList;

/*
 *  Notes on the current bounds:
 *  On my computer, the top bound is actually 2. Need to check on uni computers.
 *
 *  There is a delay in loading the home improvement grunt
 */

public class tankSprite extends Sprite
{
    private double velocityX = 0;
    private double velocityY = 0;
    private double speed = config.tankSpeed; // magnitude of velocity
    private int speedDir = 0; // 1 = moving forwards, 0 = staying still, -1 = moving backwards
    private int direction; // can be from 0 to 15, 0 meaning the tank is pointing up
    private int invun = config.tankInvun; // counter for respawn invulnerability

	private int  rotateIncrements = config.rotateIncrements; // 360/this = the angle that the tank rotates per increment

	private int power;
	private boolean powerActive = false;
	public boolean bubbleShield = false;

	private int timeLastShot = 0;
	private double fireRate = 1;

	private int timeLastRotated = 0;
	private int rotateDelay = config.rotateDelay;

	private  ArrayList<bulletSprite> bullets;

    public tankSprite(int x, int y, int tank, String objID)
    {
    	this.objID = objID;

    	// initialize the tank object
        positionX = x;
        positionY = y;
        if (tank == 5)
        	image = new Image("tank" + 50 + ".png");
        else
        	image = new Image("tank" + tank + ".png");

        width = image.getWidth(); // for collision detection
        height = image.getHeight();

        xVertices = new double[] {positionX + width, positionX + width, positionX, positionX};
        yVertices = new double[] {positionY, positionY + height, positionY + height, positionY};
        // 0 = top right, 1 = bottom right, 2 = bottom left, 3 = top left
    }


    @Override public void render(GraphicsContext gc)
    {
    	invun++;
    	gc.save(); // saves the current state on stack, including the current transform
    	double angle = direction*360/rotateIncrements;
        Rotate r = new Rotate(angle, positionX + image.getWidth()/2, positionY + image.getHeight()/2);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());

        // if the tank has just respawned, it will flash to show it is invunerable
    	if (invun < config.tankInvun) {
	    	if (((int)invun/3)%3 < 2)
		        gc.drawImage(image, positionX, positionY);
    	} else
	        gc.drawImage(image, positionX, positionY);

        gc.restore(); // un-does the rotation transform that was applied to the canvas

        // if the tank has a power up, it will have a coloured rectangle around it
        // colour of rectangle:
        switch(power) { // is break needed?
        	case 0: gc.setStroke(Color.rgb(invun%70,invun%70,255)); break; // bubble shield
        	case 1: gc.setStroke(Color.rgb(invun%70,255,invun%70)); break; // + movement
        	case 2: gc.setStroke(Color.rgb(200,invun%70,200)); break; // - movement
        	case 3: gc.setStroke(Color.rgb(255,invun%70,invun%70)); break; // + fire rate
        	case 4: gc.setStroke(Color.rgb(invun%70,200,200)); break; // - fire rate
        	default: gc.setStroke(Color.rgb(255,255,255)); break; // is break needed?
        }
    	if (powerActive)
    		gc.strokePolygon(xVertices, yVertices, 4);
    }

    public int getPower() {return power;}
    public void setPower(int power) {this.power = power;}
    public boolean getPowerActive() {return powerActive;}
    public void setPowerActive(boolean powerActive) {this.powerActive = powerActive;}

    public void shoot(int time, String objID) {
    	// shoot delay controls the rate of fire
    	if(time - timeLastShot > config.shootDelay / fireRate) {
    		bullets.add(new bulletSprite(positionX, positionY, direction, objID));
    		timeLastShot = time;
    	}
    }

    public void garbageDump(int time, String objID) {
    	// shoot delay controls the rate of fire
    	if(time - timeLastShot > config.shootDelay / fireRate) {
    		bullets.add(new bulletSprite(positionX, positionY, Math.abs((int)(Math.random() * 15)), objID));
    		timeLastShot = time;
    	}
    }

    public void loadBullets(ArrayList<bulletSprite> bullets) {
    	// shooting is handled by the tank sprite, but the bullets need to be loaded into the gameRun class
    	this.bullets = bullets;
    }
    public ArrayList<bulletSprite> getBullets() {return bullets;};
    public int getDirection() { return direction; }
    public void setDirection(int d) {
    	// will rotate towards to direction d
    	direction = d;
    	// update hit box
    	for (int i = 0; i < 4; i++) {
    		// position + (24,24) is the center of the image
    		xVertices[i] = positionX+24 + Math.sqrt(2)*24*Math.cos(Math.PI*2/rotateIncrements*(direction+i*rotateIncrements/4) - Math.PI*4/rotateIncrements);
    		yVertices[i] = positionY+24 + Math.sqrt(2)*24*Math.sin(Math.PI*2/rotateIncrements*(direction+i*rotateIncrements/4) - Math.PI*4/rotateIncrements);
    	}
    }
    public void incrementDirection (boolean cw, ArrayList<wallSprite> walls, int time) {
    	if(time - timeLastRotated > rotateDelay) {
	    	rotate(cw);
	    	// if out of bounds now, the tank will be moved back in
	        moveIntoBounds();
	        // TODO won't let the tank rotate into walls though
	    	for (wallSprite wall : walls){
	        	if (wall.intersects(this) && this.intersects(wall)) {
	        		int vertex = withinWall(wall);
	        		if (vertex == -1 || direction%4 == 0){
	        			rotate(!cw);
		            	return;
	        		} else {
	        			// push in direction of vertex
	        			rotate(!cw);
		            	return;
	        		}
	            	/*rotate(!cw);
	            	return;*/
	        	}
	        }
	    	timeLastRotated = time;
    	}
    }
    public void rotate(boolean cw) {
    	// true for clockwise increment
    	if (cw) {
    		if (direction == rotateIncrements - 1){
    			direction = 0;
    		} else {
    			direction++;
    		}
    	} else {
    		if (direction == 0){
    			direction = rotateIncrements - 1;
    		} else {
    			direction--;
    		}
    	}
    	// update hit box
    	for (int i = 0; i < 4; i++) {
    		// position + (24,24) is the center of the image
    		xVertices[i] = positionX+24 + Math.sqrt(2)*24*Math.cos(Math.PI*2/rotateIncrements*(direction+i*rotateIncrements/4) - Math.PI*4/rotateIncrements);
    		yVertices[i] = positionY+24 + Math.sqrt(2)*24*Math.sin(Math.PI*2/rotateIncrements*(direction+i*rotateIncrements/4) - Math.PI*4/rotateIncrements);
    	}
    }

///////////////////// UPDATE MOVEMENT AND POSITION

    public void setMovement(boolean moving, boolean forwards) {
    	if (!moving) {
    		speedDir = 0;
    	} else if (forwards) {
    		speedDir = 1;
    	} else {
    		speedDir = -1;
    	}
    }
    public void updateVelocity()
    {
    	// possible directions that the tank can point are restricted to multiples of pi/8
        velocityX = speed * speedDir * Math.sin(Math.PI * direction*2/rotateIncrements);
        velocityY = speed * speedDir * -1 * Math.cos(Math.PI * direction*2/rotateIncrements);
    }
    public void increaseSpeed() { speed *= 1.1; }

    public boolean updatePosition(ArrayList<wallSprite> walls) {
        positionX += velocityX;
        positionY += velocityY;
        for (int i = 0; i < 4; i++) {
        	xVertices[i] += velocityX;
        	yVertices[i] += velocityY;
        }

        // if the tank has just moved out of bounds or into a wall, we must push it back
        pushBackIntoBounds();
        return pushOutOfWalls(walls); // return true if there was a wall collision
    }
    public void updatePosition() {
        positionX += velocityX;
        positionY += velocityY;
        for (int i = 0; i < 4; i++) {
        	xVertices[i] += velocityX;
        	yVertices[i] += velocityY;
        }
    }

    public boolean tanksCollide(ArrayList<wallSprite> walls, tankSprite tank, GraphicsContext gc) {
        // respawn if the tanks touch each other
        if (intersects(tank) && tank.intersects(this)) {
        	this.respawn(walls, tank, gc);
        	tank.respawn(walls, this, gc);
        	return true;
        }
    	return false;
    }
    public boolean tanksCollide(ArrayList<wallSprite> walls, tankSprite[] tanks, GraphicsContext gc) {
        // respawn if the tanks touch each other
    	for(tankSprite tank: tanks){
	        if (intersects(tank) && tank.intersects(this)) {
	        	this.respawn(walls, tank, gc);
	        	tank.respawn(walls, this, gc);
	        	return true;
	        }
    	}
    	return false;
    }

///////////////////// BOUNDS COLLISSION DETECTION

	@Override public boolean outOfBounds() {
		double rotateExtended = 24; // extra length from the unrotated hitbox
		if (direction%2 == 1) {
			rotateExtended = Math.sqrt(2)*24*Math.cos(Math.PI/8);
		} else if(direction%4 == 2) {
			rotateExtended = Math.sqrt(2);
		}
    	return positionX + 24 - rotateExtended < 0 || positionX + 24 + rotateExtended > 1024
    			|| positionY + 24 - rotateExtended < 0 || positionY + 24 + rotateExtended > 768;
    }
	private void moveIntoBounds() { // for moving along the x/y axis into the bounds again
		double moveX = pushVectorIntoBounds(true);
        double moveY = pushVectorIntoBounds(false);
        positionX -= moveX;
        positionY -= moveY;
        for (int i = 0; i < 4; i++) {
        	xVertices[i] -= moveX;
        	yVertices[i] -= moveY;
        }
	}
	private void pushBackIntoBounds() { // for moving along the axis of movement
		double moveX = Math.abs(pushVectorIntoBounds(true));
        double moveY = Math.abs(pushVectorIntoBounds(false));
        if (moveX != 0) {
        	moveY = moveX * speedDir * -1 * Math.cos(Math.PI * direction*2/rotateIncrements);
        	moveX *= speedDir * Math.sin(Math.PI * direction*2/rotateIncrements);
        } else if (moveY != 0) {
        	moveX = moveY * speedDir * Math.sin(Math.PI * direction*2/rotateIncrements);
        	moveY *= speedDir * -1 * Math.cos(Math.PI * direction*2/rotateIncrements);
        }
        positionX -= moveX;
        positionY -= moveY;
        for (int i = 0; i < 4; i++) {
        	xVertices[i] -= moveX;
        	yVertices[i] -= moveY;
        }
	}
	public double pushVectorIntoBounds(boolean x) {
		double push = 0;
		for (int i = 0; i < 4; i++){
			if (x) {
	        	if (xVertices[i] < 0 && xVertices[i] < push) {
	    			push = xVertices[i];
	        	}
	        	if (xVertices[i] > 1024 && xVertices[i] > push)
	    			push = xVertices[i] - 1024;
			} else {
	        	if (yVertices[i] < 0 && yVertices[i] < push)
	    			push = yVertices[i];
	        	if (yVertices[i] > 768 && yVertices[i] > push)
	    			push = yVertices[i] - 768;
			}
		}
    	return push;
    }

///////////////////// WALL COLLISSION DETECTION

	private boolean pushOutOfWalls(ArrayList<wallSprite> walls) { // returns true if there was a colission
		double moveX, moveY;
		for (wallSprite wall : walls){
        	if (wall.intersects(this) && this.intersects(wall)) {
        		int vertex = withinWall(wall);
        		if (vertex == -1 || direction%4 == 0){
	        		moveX = wallPushVector1(wall) * speedDir * Math.sin(Math.PI * direction*2/rotateIncrements);
	        		moveY = wallPushVector1(wall) * speedDir * -1 * Math.cos(Math.PI * direction*2/rotateIncrements);
        		} else {
        			double[] push = wallPushVector2(wall, vertex);
        			moveX = -1*push[0];
	        		moveY = -1*push[1];
        		}
                positionX -= moveX;
                positionY -= moveY;
                for (int i = 0; i < 4; i++) {
                	xVertices[i] -= moveX;
                	yVertices[i] -= moveY ;
                }
                return true;
        	}
        }
		return false;
	}
	public int withinWall(wallSprite wall)
    {
    	int vertex = -1;

    	double xVectorA; // vector A
    	double yVectorA;
    	double xVectorB; // vector B
    	double yVectorB;
    	double vectorLength;
    	double scalarProjection;
    	boolean projectionOverlaps = false;

    	// find out which of the tank's 4 vertices is inside the wall
    	for (int j = 0; j < 4; j++){
			// vector B = vertex to be checked, relative to wall vertex 0
    		xVectorB = xVertices[j] - wall.getXVertex(0);
    		yVectorB = yVertices[j] - wall.getYVertex(0);
        	for (int i = 1; i < 4; i+=2){
        		// vector A = wall direction to check for intersections
        		xVectorA = wall.getXVertex(i) - wall.getXVertex(0);
        		yVectorA = wall.getYVertex(i) - wall.getYVertex(0);
        		vectorLength = Math.sqrt(xVectorA*xVectorA + yVectorA*yVectorA); // |A|
	    		scalarProjection = ( xVectorA*xVectorB + yVectorA*yVectorB ) // A.B
	    							/ vectorLength; // |A|
	    		projectionOverlaps = 0 < scalarProjection && scalarProjection < vectorLength; // the scalar projection is within the vector
				if (!projectionOverlaps)
					break;
			}
			if (projectionOverlaps) {
				vertex = j;
				return vertex;
			}
    	}
    	return vertex;
    }
    private double wallPushVector1(Sprite s) {
    	// Returns the distance to be pushed back in order to not be intersecting anymore (in the direction of movement)
    	// direction = true for x component
    	double push = 0;

    	double xVectorA; // vector A
    	double yVectorA;
    	double xVectorB; // vector B
    	double yVectorB;
    	double vectorLength;
    	double scalarProjection;
    	int i, j;
    	// 2 vectors to be checked for each sprite
    	// assumes there is always 4 vertices and the first and that non-adjacent vectors are parallel (i.e. always makes a rectangle)
    	if (speedDir == 1) { // moving forwards
    		i = 0;
    		j = 1;
    	} else if (speedDir == -1) { // moving backwards
    		i = 1;
    		j = 0;
    	} else {
    		return 0;
    	}
		// treat vertex i as origin
		xVectorA = xVertices[j] - xVertices[i];
		yVectorA = yVertices[j] - yVertices[i];
		vectorLength = Math.sqrt(xVectorA*xVectorA + yVectorA*yVectorA); // |A|
    	for (int k = 0; k < 4; k++){
			// vector B = vertex j - vertex i
    		xVectorB = s.getXVertex(k) - xVertices[i];
    		yVectorB = s.getYVertex(k) - yVertices[i];
    		scalarProjection = ( xVectorA*xVectorB + yVectorA*yVectorB ) // A.B
    							/ vectorLength; // |A|
        	if (scalarProjection > push) // the scalar projection is within the vector
    			push = scalarProjection;
		}
    	return push;
    }
    private double[] wallPushVector2(wallSprite wall, int vertex) {
    	// Returns the distance to be pushed back in order to not be intersecting anymore (in the direction of movement)
    	// direction = true for x component and false for y component
    	double push[] = {0,0};
    	double x0 = xVertices[vertex];
    	double y0 = yVertices[vertex];
    	int quadrant = (int)direction/4 + 2;
    	int v2;
    	if (vertex%2 == 0)
    		v2 = vertex+1;
    	else
    		v2 = vertex-1;
    	int v3 = (vertex+quadrant)%4;
    	int v4 = (vertex+quadrant+1)%4;
    	double x2 = xVertices[v2] - x0;
    	double y2 = yVertices[v2] - y0;
    	double x3 = wall.getXVertex(v3) - x0;
    	double y3 = wall.getYVertex(v3) - y0;
    	double x4 = wall.getXVertex(v4) - x0;
    	double y4 = wall.getYVertex(v4) - y0;

    	// see https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection#Given_two_points_on_each_line
    	double temp = (x3*y4-y3*x4)/(y2*(x3-x4)-x2*(y3-y4));
    	push[0] = x2*temp;
    	push[1] = y2*temp;
    	return push;
    }

///////////////////// SHOOTING AND RESPAWNING

    public boolean isShotBy(bulletSprite bullet) {
    	if (invun < config.tankInvun) {
    		return false;
    	} else {
    		return this.intersects(bullet);
    	}
    }
    public void respawn(ArrayList<wallSprite> walls, tankSprite tank, GraphicsContext gc) {
    	gc.setFill(Color.RED);
        gc.fillPolygon(xVertices, yVertices, 4);

        direction = 0;
        boolean validPos = false;
        while (!validPos) {
        	validPos = true;
            positionX = Math.random()*(1024-48);
            positionY = Math.random()*(768-48);
            xVertices = new double[] {positionX + width, positionX + width, positionX, positionX};
            yVertices = new double[] {positionY, positionY + height, positionY + height, positionY};
            if (outOfBounds() || (tank.intersects(this) && this.intersects(tank)))
            	validPos = false;
            else {
	        	for (wallSprite wall : walls) {
	            	if (wall.intersects(this) && this.intersects(wall)) { // rotation is 0 which is parallel to the walls
	            		validPos = false;
	            	}
	            }
            }
        }
        invun = 0;
    }
    public void respawn(ArrayList<wallSprite> walls, tankSprite[] tanks, GraphicsContext gc) {
    	gc.setFill(Color.RED);
        gc.fillPolygon(xVertices, yVertices, 4);

        direction = 0;
        boolean validPos = false;
        while (!validPos) {
        	validPos = true;
            positionX = Math.random()*(1024-48);
            positionY = Math.random()*(768-48);
            xVertices = new double[] {positionX + width, positionX + width, positionX, positionX};
            yVertices = new double[] {positionY, positionY + height, positionY + height, positionY};
            if (outOfBounds() || (tanks[0].intersects(this) && this.intersects(tanks[0]))
            		 		  || (tanks[1].intersects(this) && this.intersects(tanks[1]))
            				  || (tanks[2].intersects(this) && this.intersects(tanks[2])))
            	validPos = false;
            else {
	        	for (wallSprite wall : walls){
	            	if (wall.intersects(this) && this.intersects(wall)) { // rotation is 0 which is parallel to the walls
	            		validPos = false;
	            	}
	            }
            }
        }
        invun = 0;
    }

    public int getPowerUp() {
    	if (powerActive)
    		return power;
    	else
    		return -1;
    }

    //Gives the tank a random power up for 15 seconds
    public void powerUp() {
    	//Removes current power
    	if(powerActive = true) { powerDown(); }

    	powerActive = true;
    	power = (int) (Math.random() * 5); //Randomly selects a power

    	switch(power) {
	    	case 0: bubbleShield = true; break; // bubble shield
	    	case 1: speed *= 1.5; break; // + movement
	    	case 2: speed *= 0.5; break; // - movement
	    	case 3: fireRate = 3; break; // + fire rate
	    	case 4: fireRate = 0.4; break; // - fire rate
	    	default: break;
	    }
    }

    //Removes the effects of the power up
    public void powerDown() {
    	switch(power) {
	    	case 0: bubbleShield = false; break; // bubble shield
	    	case 1: speed = config.tankSpeed; break; // + movement
	    	case 2: speed = config.tankSpeed; break; // - movement
	    	case 3: fireRate = 1; break; // + fire rate
	    	case 4: fireRate = 1; break; // - fire rate
	    	default: break;
	    }
    	powerActive = false;
    }

    public boolean intersectingVertex(Sprite s)
    {
    	// uses the method of separation axis from here: http://elancev.name/oliver/2D%20polygon.htm

    	double xVectorA; // vector A
    	double yVectorA;
    	double xVectorB; // vector B
    	double yVectorB;
    	double vectorLength;
    	double scalarProjection;
    	boolean projectionOverlaps;
    	// 2 vectors to be checked for each sprite
    	// assumes there is always 4 vertices and the first and that non-adjacent vectors are parallel (i.e. always makes a rectangle)
    	for (int i = 1; i < 4; i+=2){
    		projectionOverlaps = false;
    		// treat vertex 0 as the origin, vector A = vertex i - vertex 0
    		xVectorA = xVertices[i] - xVertices[0];
    		yVectorA = yVertices[i] - yVertices[0];
    		vectorLength = Math.sqrt(xVectorA*xVectorA + yVectorA*yVectorA); // |A|
        	for (int j = 0; j < 4; j++){
    			// vector B = vertex j - vertex i
        		xVectorB = s.getXVertex(j) - xVertices[0];
        		yVectorB = s.getYVertex(j) - yVertices[0];
	    		scalarProjection = ( xVectorA*xVectorB + yVectorA*yVectorB ) // A.B
	    							/ vectorLength; // |A|
            	if (scalarProjection < vectorLength && 0 < scalarProjection) // the scalar projection is within the vector
	    			projectionOverlaps =  true;
    		}
    		if (!projectionOverlaps)
    			return false;
    	}
    	return true;
    }

    public void updateImage(int time) {
    	int num = (time/10)%2;
    	image = new Image("tank5" + num + ".png");
    }
}



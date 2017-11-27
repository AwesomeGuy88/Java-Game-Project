import java.util.ArrayList;
import java.util.HashSet;



//Contains all information stored and transferred between client and server
//Contains functions to decrypt data recieved from the server
//Contains functions to encrypt data sent to the server
public class SlaveData {

	////////////////////////////DATA//////////////////////////////////////////////
						//Data to receive
	//Tank Data master
	protected int positionX1 = 0;
	protected int positionY1 = 0;
	protected int direction1 = 0;
	protected int state1 = 0;
	protected int alive1 = 0;
	//Tank Data slave
	protected int positionX2 = 0;
	protected int positionY2 = 0;
	protected int direction2 = 0;
	protected int state2 = 0;
	protected int alive2 = 0;
	//Wall data
	protected ArrayList<wallSprite> walls = new ArrayList<wallSprite>();
	//Power up data
	protected ArrayList<powerUpSprite> powerUps = new ArrayList<powerUpSprite>();
	//Bullet data
	protected ArrayList<bulletSprite> bullets = new ArrayList<bulletSprite>();

	 					//Data to send
	 //Controls
	 protected HashSet<String> activeKeys;

	 //////////////////////////////DATA END/////////////////////////////////////////


	 //Function to send control signals to make sure data is being received and sent at appropriate intervals
	 public void control() {
		 //Control State 1:
		 //Receiving initial data and generating objects


		 //Control State 2:
		 //Receiving data updates and updating object data



	 }

	 //Function sends data to the server
	 private String sendData() {
		 String Data = null;


		 return Data;
	 }


	 //Function called by server to decrypt string information and generate objects
	 public int recieveData(String Data) {
		 //Initialise local variables
		 String[] decoded;

		 //Split the string into pieces of data
		 decoded = decode(Data);

		 //Depending on the object type, calls the appropriate function to update data


		 return 0; //Data decryption is succcessful
	 }

	 //Function to decode a string of data into its components
	 private String[] decode(String Data) {
		 //initialise local variables
		 String[] decoded = null;

		 //Algorithim to split the data into its various pieces

		 return decoded;
	 }


	 //Function which Retrieves data from the server
	 private String collectData(String APIparameters) {
		 String Data = "nothing";



		 return Data;
	 }

	 //Function which updates tank data, returns true if successful
	 private boolean updateTankData(String[] Data) {
		return false;

	 }
	 //Function which updates wall data, returns true if successful
	 private boolean updateWallData(String[] Data) {
		return false;

	 }
	 //Function which updates power up data, returns true if successful
	 private boolean updatePowerUpData(String[] Data) {
		return false;

	 }
	 //Function which updates bullet data, returns true if successful
	 private boolean updateBulletData(String[] Data) {
		return false;

	 }
	 //Function which encodes current control data
	 private String updateControlsData() {
		 String Data = null;

		 return Data;
	 }




}

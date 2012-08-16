package pac.utils;

import com.jme3.math.Vector3f;

public interface Constants {

	/** MODEL **/
	String SCENEMODELLOCATION = "assets/scenes/town.zip";
	String PLAYERMODELLOCATION = "Models/Oto/Oto.mesh.xml";
	
	
	/** PLAYER INIT **/
	Vector3f PLAYERINITTRANSLATIONVECTOR = new Vector3f(0.0f, 5.0f, 0.0f);
	float PLAYERSCALE = .2f;
	float PLAYERSTEPHEIGHT = 1.0f;
	float PLAYERJUMPSPEED = 5.0f;
	float PLAYERFALLSPEED = 5.0f;
	float PLAYERGRAVITY = 10.0f;
	
	
	/** CAMERA **/
	Vector3f CAMNODETRANSLATIONVECTOR = new Vector3f(.0f, 5.0f, -30.0f);
	
	
	/** ANIMATION **/
	String ANIMSTAND = "stand";
	String ANIMWALK = "Walk";
	float ANIMBLENDTIME = .10f;
	float ANIMSPEED = 1.5f;
	
	
	/** SPEED **/
	float SPEEDRATE = 12.0f;
	float SPEEDUPRATE = 1.0f;
	float WALKSLOW = 3.0f;
	float ROTSPEED = 4.0f;
	
	
	/** KEY MAPPING **/
	String LEFT = "Left";
	String RIGHT = "Right";
	String UP = "Up";
	String DOWN = "Down";
	String JUMP = "Jump";
	String SWITCHCAM = "SWITCHCAM";
	
	
	/** LIGHT **/
	float AMBIENTLIGHTMULT = 1.3f;
	Vector3f LIGHTDIRECTIONVECTOR = new Vector3f(2.8f, -2.8f, -2.8f);
	
}

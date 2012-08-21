package pac.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import pac.controller.*;
import pac.utils.Constants;
import pac.utils.MatrixTools;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Box;

public class Pac extends SimpleApplication implements  Constants{

	public static void main(String[] args) {
		Pac app = new Pac();
		app.start();
	}

	private Spatial scene, player;
	private RigidBodyControl sceneControl;
	private CharacterControl playerControl;
	
	private BulletAppState bulletAppState;
	private CameraNode camNode;

	private boolean left, right, up, down, jump, switchCam;
	
	private AnimChannel playerAnimChannel;
	private AnimControl playerAnimControl;
	
	private boolean switchCamControl = false;
	private float reverse = 1f;
	
	int numberOfDots=0, totalDots=0;
	private BitmapText HUDdots;
	

	public Pac(){
		 super((AppState)null);	 
	 }

	@Override
	public void simpleInitApp() {
		
		bulletAppState = new BulletAppState();
	    stateManager.attach(bulletAppState);
		
		setUpLight();
		setUpKeys();
		
		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
		
		
		// Load the scene from the zip file and adjust its size.
	    assetManager.registerLocator(SCENEMODELLOCATION, ZipLocator.class);
	    scene = assetManager.loadModel("main.scene");
	    
	    // Set up collision detection for the scene by creating a
	    // compound collision shape and a static RigidBodyControl with mass zero.
	    CollisionShape sceneShape =
	            CollisionShapeFactory.createMeshShape((Node) scene);
	    sceneControl = new RigidBodyControl(sceneShape, 0);
	    sceneControl.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_01);
	    scene.addControl(sceneControl);
	    
	    
	    // Load and place the model
	    player = assetManager.loadModel(PLAYERMODELLOCATION);
	    player.setLocalTranslation(PLAYERINITTRANSLATIONVECTOR);
	    player.setLocalScale(PLAYERSCALE);
	    
	    // Init player physics
	    float x = ( (BoundingBox)player.getWorldBound()).getXExtent();
	    float y = ( (BoundingBox)player.getWorldBound()).getYExtent();
	    CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(x, y/2, 1);
	    playerControl = new CharacterControl(capsuleShape, PLAYERSTEPHEIGHT);
	    playerControl.setJumpSpeed(PLAYERJUMPSPEED);
	    playerControl.setFallSpeed(PLAYERFALLSPEED);
	    playerControl.setGravity(PLAYERGRAVITY);
	    playerControl.setCollisionGroup(GhostControl.COLLISION_GROUP_02);
	    player.addControl(playerControl);
	    
	    
	    // Init Camera   
		camNode = new CameraNode("Camera Node", cam);
		//This mode means that camera copies the movements of the target:
		camNode.setControlDir(ControlDirection.SpatialToCamera);
		//Attach the camNode to the target:
		((Node) player).attachChild(camNode);
		//Move camNode, e.g. behind and above the target:
		camNode.setLocalTranslation(CAMNODETRANSLATIONVECTOR);
		//Rotate the camNode to look at the target:
		Vector3f lookat = playerControl.getPhysicsLocation();
		lookat.y += y/2;
		camNode.lookAt(lookat, Vector3f.UNIT_Y);
		/* ChaseCamera chaseCam = new ChaseCamera(cam, model, inputManager);
		chaseCam.setSmoothMotion(true);*/
		
		
		// Init Animations
		playerAnimControl = player.getControl(AnimControl.class);
		playerAnimControl.addListener( new animEventListener());
		playerAnimChannel = playerAnimControl.createChannel();
		playerAnimChannel.setAnim(ANIMSTAND);
		
		// Add player and scene to bullet
		bulletAppState.getPhysicsSpace().add(sceneControl);
		bulletAppState.getPhysicsSpace().add(playerControl);
		
		// Attach the models to the root node
		rootNode.attachChild(scene);
		rootNode.attachChild(player);
		
		/***********/
		
		// Add the pac-dots
		initPacDots();
		
		// Init GUI
		guiNode.detachAllChildren();
	    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
	    HUDdots = new BitmapText(guiFont, false);
	    HUDdots.setText(numberOfDots+" / "+totalDots); 
	    HUDdots.setColor(new ColorRGBA(1, 0, 0, 1));// fake crosshairs :)
	    HUDdots.setLocalTranslation( 
	      0,
	      settings.getHeight() - HUDdots.getLineHeight() / 2, 0);
	    guiNode.attachChild(HUDdots);	    	
		 
	}
	

	@Override
	  public void simpleUpdate(float tpf) {
	 
		updateCameraWithCollisions();
		
		rotateAndMovePLayer(tpf);

	 }
	 
	
	 

	/* 									FUNCTIONS											*/
	 /* *****************************************************************************************/
	 
	/********************************************************************************************
	 * 
	 * 										UPDATE
	 *
	**********************************************************************************************/
	 
	 /*
	 * 		UPDATE THE CAMERA, GETS CLOSER FROM THE PLAYER IF IS IN COLLISION WITH A WALL.
	 */
 	private void updateCameraWithCollisions() {
 		
 		float camMaxTrZ = CAMNODETRANSLATIONVECTOR.z;
		float camMinTrZ = CAMNODEMINTRANSLATIONVECTOR.z;

		/* 	switchCamControl is to make sure we enter in this condition only when
			the state of the keyboard button changes (pressed or released). We dont do it
		 	in the action controller because we need to have those transformations done 
		 	before the collisions are tested. */
		if (switchCam != switchCamControl ){
			
			reverse  = -reverse; // reverse is -1 if the camera is inverted, 1 otherwise. That permits to easily inverse things later.
			
			Vector3f tr = this.getCamNode().getLocalTranslation();
			this.getCamNode().setLocalTranslation(tr.x, tr.y, -tr.z); // reverse camera translation
			
			Quaternion rot = this.getCamNode().getLocalRotation();
  			this.getCamNode().setLocalRotation(new Quaternion(-rot.getZ(),rot.getW(), -rot.getX(), rot.getY())); // reverse camera rotation
  			
  			switchCamControl = switchCam;
		}
		
		// Create a ray, from the player position, looking in the camera direction inverted, limit is the distance between camera and player.
		// multiplied by the RAYFACTOR to make it a little longer.
		Ray ray = new Ray(player.getWorldTranslation(), cam.getDirection().negate());
		ray.setLimit(camNode.getLocalTranslation().negate().mult(PLAYERSCALE).z * RAYFACTOR * reverse);
		
		// Test collisions between the scene and the ray.
		CollisionResults results = new CollisionResults();
		scene.collideWith(ray, results);
		
		// Create temporary vector
		Vector3f camTr = new Vector3f();
		camTr.set(camNode.getLocalTranslation());

		
		// if there are results and the camera is not a its minimum, get the camera closer.
		if (results.size()>0 && camNode.getLocalTranslation().z*reverse < camMinTrZ)
			camTr.set(camNode.getLocalTranslation().add(0,0, reverse));
		
		// else if the camera is not at its maximum, get the camera further, only if the next position is not in collision.
		else if(camNode.getLocalTranslation().z*reverse > camMaxTrZ){
			ray.setLimit((ray.getLimit() + 0.2f)*RAYFACTOR);
			scene.collideWith(ray, results);
			if (results.size() == 0)
				camTr.set(camNode.getLocalTranslation().add(0,0, -reverse));
			
		}
	
		camNode.setLocalTranslation(camTr);
		
	}
 	 
 	/*
	 * 		ROTATE THE PLAYER OR MAKE HIM MOVE DEPENDING ON THE KEY PRESSED. UPDATE THE ANIMATION CHANNEL.
	 */
 	private void rotateAndMovePLayer(float tpf) {
		 Vector3f walkDirection = new Vector3f().set(0,0,0);
			Vector3f view = playerControl.getViewDirection().normalize();
			
			float angle = tpf*ROTSPEED*SPEEDUPRATE;
			float speed = tpf*SPEEDRATE*SPEEDUPRATE;
			
			if (right)
				angle = -angle;
			if (down)
				angle = -angle;
			
			Matrix3f rotMat = MatrixTools.getYRotationMatrix3f(angle);
			Vector3f rot = rotMat.mult(view);
				
			if (left)  
				playerControl.setViewDirection(new Vector3f(rot.x,rot.y,rot.z));
			
			if (right) 
				playerControl.setViewDirection(new Vector3f(rot.x,rot.y,rot.z));
			
			if (up || down){ 
				if (down)
					view = view.negateLocal();
				
				walkDirection.set(new Vector3f(view.x*speed,0.0f,view.z*speed));
				
			    if (!playerAnimChannel.getAnimationName().equals(ANIMWALK))
			    {
			          playerAnimChannel.setAnim(ANIMWALK, ANIMBLENDTIME);
			          playerAnimChannel.setSpeed(ANIMSPEED*SPEEDUPRATE);
			          playerAnimChannel.setLoopMode(LoopMode.Loop);
			    }
			}
			else
				playerAnimChannel.setAnim(ANIMSTAND, ANIMBLENDTIME);
				
			playerControl.setWalkDirection(walkDirection);
		
	}
 	
 	
 	/********************************************************************************************
	 * 
	 * 											INIT
	 *
	**********************************************************************************************/
 
 	
 	private void setUpLight() {
			
	    // We add light so we see the scene
	    AmbientLight al = new AmbientLight();
	    al.setColor(ColorRGBA.White.mult(AMBIENTLIGHTMULT));
	    rootNode.addLight(al);
	 
	    DirectionalLight dl = new DirectionalLight();
	    dl.setColor(ColorRGBA.White);
	    dl.setDirection(LIGHTDIRECTIONVECTOR.normalizeLocal());
	    rootNode.addLight(dl);
	}
	 
	
 	private void setUpKeys() {
		inputManager.addMapping(LEFT, new KeyTrigger(KeyInput.KEY_LEFT));
	    inputManager.addMapping(RIGHT, new KeyTrigger(KeyInput.KEY_RIGHT));
	    inputManager.addMapping(UP, new KeyTrigger(KeyInput.KEY_UP));
	    inputManager.addMapping(DOWN, new KeyTrigger(KeyInput.KEY_DOWN));
	    inputManager.addMapping(JUMP, new KeyTrigger(KeyInput.KEY_SPACE));
	    inputManager.addMapping(SWITCHCAM, new KeyTrigger(KeyInput.KEY_V));
	    
	    inputManager.addListener(new actionListener(this), LEFT, RIGHT, UP, DOWN, JUMP, SWITCHCAM);
			
	}
	 
 	private void initPacDots() {
 		
		Scanner sc = null;
		try {
			sc = new Scanner(new File("coordinates"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		

		float x=0, z=0;
		
		while(sc.hasNextFloat()){
			
			x= 0; z= 0;
			
			x = sc.nextFloat();
			if (sc.hasNextFloat())
				z = sc.nextFloat();
			
			Box b = new Box( 1, 1, 1); // create cube shape at the origin
		    Geometry geom = new Geometry("Box", b);  // create cube geometry from the shape
		    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
		    mat.setColor("Color", ColorRGBA.Red);   // set color of material to blue
		    geom.setMaterial(mat);   
		    geom.setLocalTranslation(x,2,z);// set the cube's material
		    rootNode.attachChild(geom);
		    
		    BoxCollisionShape geomShape = new BoxCollisionShape(new Vector3f(1,1,1));
		    myGhostControl boxControl = new myGhostControl(geomShape,this);
		    geom.addControl(boxControl);
		    bulletAppState.getPhysicsSpace().add(boxControl);
		    
		    totalDots++;
		}
        

		
	}
 	
 	/********************************************************************************************
	 *
	 * 									GETTERS & SETTERS
	 *
	**********************************************************************************************/
	
 	
 
	 public CharacterControl getPlayerControl() {
		return playerControl;
	}

	public void setPlayerControl(CharacterControl playerControl) {
		this.playerControl = playerControl;
	}

	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public boolean isRight() {
		return right;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	public boolean isUp() {
		return up;
	}

	public void setUp(boolean up) {
		this.up = up;
	}

	public boolean isDown() {
		return down;
	}

	public void setDown(boolean down) {
		this.down = down;
	}
	
	public boolean isJump() {
		return jump;
	}

	public void setJump(boolean jump) {
		this.jump = jump;
	}
	
	public boolean isSwitchCam() {
		return switchCam;
	}

	public void setSwitchCam(boolean switchCam) {
		this.switchCam = switchCam;
	}

	
	public CameraNode getCamNode() {
		return camNode;
	}

	public void setCamNode(CameraNode camNode) {
		this.camNode = camNode;
	}
	
	
	public Spatial getPlayer() {
		return player;
	}

	public void setPlayer(Spatial player) {
		this.player = player;
	}

	public BulletAppState getBulletAppState() {
		return bulletAppState;
	}

	public void setBulletAppState(BulletAppState bulletAppState) {
		this.bulletAppState = bulletAppState;
	}

	public int getNumberOfDots() {
		return numberOfDots;
	}

	public void setNumberOfDots(int numberOfDots) {
		this.numberOfDots = numberOfDots;
		HUDdots.setText(numberOfDots+" / "+totalDots);
	}

	public int getTotalDots() {
		return totalDots;
	}

	public void setTotalDots(int totalDots) {
		this.totalDots = totalDots;
	}
	
	public Node getRootNode(){
		return rootNode;
	}
}

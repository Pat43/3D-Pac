package pac.main;

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
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;

public class Pac extends SimpleApplication implements  Constants{
	
	

	public static void main(String[] args) {
		 Pac app = new Pac();
		    app.start();
	}

	private Spatial scene;
	private RigidBodyControl sceneControl;
	private Node player;

	private CharacterControl playerControl;
	
	private BulletAppState bulletAppState;
	private CameraNode camNode;

	private boolean left;
	private boolean right;
	private boolean up;
	private boolean down;
	private boolean jump;
	
	private AnimChannel playerAnimChannel;
	private AnimControl playerAnimControl;
	
	 
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
		
		
		// We load the scene from the zip file and adjust its size.
	    assetManager.registerLocator(SCENEMODELLOCATION, ZipLocator.class);
	    scene = assetManager.loadModel("main.scene");
	    
	    // We set up collision detection for the scene by creating a
	    // compound collision shape and a static RigidBodyControl with mass zero.
	    CollisionShape sceneShape =
	            CollisionShapeFactory.createMeshShape((Node) scene);
	    sceneControl = new RigidBodyControl(sceneShape, 0);
	    scene.addControl(sceneControl);
	    
	    
	    //Load the model
	    player = (Node) assetManager.loadModel(PLAYERMODELLOCATION);
	    player.setLocalTranslation(PLAYERINITTRANSLATIONVECTOR);
	    player.setLocalScale(PLAYERSCALE);
	    float x = ( (BoundingBox)player.getWorldBound()).getXExtent();
	    float y = ( (BoundingBox)player.getWorldBound()).getYExtent();
	   
	    CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(x, y/2, 1);
	    playerControl = new CharacterControl(capsuleShape, PLAYERSTEPHEIGHT);
	    playerControl.setJumpSpeed(PLAYERJUMPSPEED);
	    playerControl.setFallSpeed(PLAYERFALLSPEED);
	    playerControl.setGravity(PLAYERGRAVITY);
	    player.addControl(playerControl);
	    
		camNode = new CameraNode("Camera Node", cam);
		//This mode means that camera copies the movements of the target:
		camNode.setControlDir(ControlDirection.SpatialToCamera);
		//Attach the camNode to the target:
		player.attachChild(camNode);
		//Move camNode, e.g. behind and above the target:
		camNode.setLocalTranslation(CAMNODETRANSLATIONVECTOR);
		//Rotate the camNode to look at the target:
		Vector3f lookat = playerControl.getPhysicsLocation();
		lookat.y += y/2;
		camNode.lookAt(lookat, Vector3f.UNIT_Y);
		  
		/* ChaseCamera chaseCam = new ChaseCamera(cam, model, inputManager);
		chaseCam.setSmoothMotion(true);*/
		 
		playerAnimControl = player.getControl(AnimControl.class);
		playerAnimControl.addListener( new animEventListener());
		playerAnimChannel = playerAnimControl.createChannel();
		playerAnimChannel.setAnim(ANIMSTAND);
		
		bulletAppState.getPhysicsSpace().add(sceneControl);
		bulletAppState.getPhysicsSpace().add(playerControl);    
		
		
		rootNode.attachChild(scene);
		rootNode.attachChild(player);

	}
	
		 
	 @Override
	  public void simpleUpdate(float tpf) {
	 
		Vector3f walkDirection = new Vector3f();
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
	 
	 
 	/*
 	 * 
	 * 		INIT SET UPS
	 * 
	 */
	 
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
	 
	 	/*
	 	 * 
		 * 		GETTERS & SETTERS
		 * 
		 */
	 
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
		
		public CameraNode getCamNode() {
			return camNode;
		}

		public void setCamNode(CameraNode camNode) {
			this.camNode = camNode;
		}
		
		
		public Node getPlayer() {
			return player;
		}

		public void setPlayer(Node player) {
			this.player = player;
		}

}

package pac.controller;

import pac.main.Pac;
import pac.utils.Constants;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;


public class myGhostControl extends GhostControl implements  PhysicsCollisionListener, Constants {
	
	Pac context;	
	
	public myGhostControl(CollisionShape shape, Pac context){
		super(shape);
		this.context=context;
	    setCollisionGroup(GhostControl.COLLISION_GROUP_02);
	    setCollideWithGroups(GhostControl.COLLISION_GROUP_02);

		context.getBulletAppState().getPhysicsSpace().addCollisionListener(this);
	}


	@Override
	public void collision(PhysicsCollisionEvent event) {
		
		if ("Box".equals(event.getNodeA().getName()) || "Box".equals(event.getNodeB().getName())) {
			if( context.getRootNode().getChildIndex(event.getNodeB()) != -1 ){
				context.setNumberOfDots(context.getNumberOfDots()+1);	
				context.getRootNode().detachChild(event.getNodeB());
				context.getBulletAppState().getPhysicsSpace().remove(event.getObjectB());	
			}

		}
	}
}

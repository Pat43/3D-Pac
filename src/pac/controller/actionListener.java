package pac.controller;

import pac.main.Pac;
import pac.utils.Constants;

import com.jme3.input.controls.ActionListener;

public class actionListener implements ActionListener, Constants {

	Pac context;
	
	public actionListener(Pac context){
		this.context=context;
	}
	@Override
	public void onAction(String name, boolean keyPressed, float tpf) {
    	if (name.equals(JUMP) ) 
    		context.getPlayerControl().jump();
    	else if (name.equals(LEFT)) 
    		context.setLeft(keyPressed);
    	else if (name.equals(RIGHT)) 
    		context.setRight(keyPressed);
    	else if (name.equals(UP))
    		context.setUp(keyPressed);
    	else if (name.equals(DOWN))
    		context.setDown(keyPressed);
    	else if (name.equals(SWITCHCAM))
    		context.setSwitchCam (keyPressed);
    		
	}	

}

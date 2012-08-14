package pac.main;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;

public class Pac extends SimpleApplication {
	
	 public static void main(String[] args) {
		 Pac app = new Pac();
		    app.start();
	}
	 
	 public Pac(){
		 super((AppState)null);
		 
	 }

	@Override
	public void simpleInitApp() {
		// TODO Auto-generated method stub

	}

}

package pac.utils;

import com.jme3.math.Matrix3f;

public class MatrixTools {
	
	public static Matrix3f getYRotationMatrix3f(float angle){
		
		return new Matrix3f((float)Math.cos(angle), 0f, (float)Math.sin(angle), 0f, 1f, 0f, -(float)Math.sin(angle), 0f, (float)Math.cos(angle));
		
	}

}

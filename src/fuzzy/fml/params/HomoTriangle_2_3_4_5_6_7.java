package fuzzy.fml.params;

public class HomoTriangle_2_3_4_5_6_7 {
	static float[][] params = new float[][]
	{
		//2分割
		new float[] {0f, 0f, 1f},
		new float[] {0f, 1f, 1f},
		//3分割
		new float[] {0f, 0f, 0.5f},
		new float[] {0f, 0.5f, 1f},
		new float[] {0.5f, 1f, 1f},
		//4分割
		new float[] {0f, 0f, 1f/3f},
		new float[] {0f, 1f/3f, 2f/3f},
		new float[] {1f/3f, 2f/3f, 1f},
		new float[] {2f/3f, 1f, 1f},
		//5分割
		new float[] {0f, 0f, 0.25f},
		new float[] {0f, 0.25f, 0.5f},
		new float[] {0.25f, 0.5f, 0.75f},
		new float[] {0.5f, 0.75f, 1f},
		new float[] {0.75f, 1f, 1f},
		//6分割
		new float[] {0.0f, 0.0f, 0.2f},
		new float[] {0.0f, 0.2f, 0.4f},
		new float[] {0.2f, 0.4f, 0.6f},
		new float[] {0.4f, 0.6f, 0.8f},
		new float[] {0.6f, 0.8f, 1.0f},
		new float[] {0.8f, 1.0f, 1.0f},
		//7分割
		new float[] {0f, 0f, 1f/6f},
		new float[] {0f, 1f/6f, 2f/6f},
		new float[] {1f/6f, 2f/6f, 3f/6f},
		new float[] {2f/6f, 3f/6f, 4f/6f},
		new float[] {3f/6f, 4f/6f, 5f/6f},
		new float[] {4f/6f, 5f/6f, 1f},
		new float[] {5f/6f, 1f, 1f}
	};

	public static float[][] getParams(){
		return params;
	}
}

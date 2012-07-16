//Basic structure : enables several classes to be ignorant of the makeup of the Tetromino object being passed around

import java.awt.Color;

public class Tetromino
{	
	public boolean[] tetArray;
	public Color tetColor;
	public int tetType;
	public int tetRot;
	
	public Tetromino(int maxblocksize)
	{
		tetArray = new boolean[maxblocksize*maxblocksize];
	} 
}
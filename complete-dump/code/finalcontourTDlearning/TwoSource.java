//Donald Carr
//Tetris
//18/6/05
//Coded for Java 1.5

//melaxs reduced tetris block set
//5 blocks
//max dimension 2
// (x,y) == (0,0) in top left hand corner

import java.util.Random;
import java.awt.Color;

public class TwoSource implements TetrominoSet
{	//Constants
	private final int NUMTETROMINOS = 5;
	
	//5 blocks defined
	private final int SINGLEBLOCK = 0;
	private final int DOUBLEBLOCK = 1;
	private final int LBLOCK = 2;
	private final int SQUAREBLOCK = 3;
	private final int XORBLOCK = 4;
	
	//width of grid
	private final int MAXBLOCKSIZE = 2;
	
	//Constant for the tetris games : while using grid structure
	private final int MAXROTATIONS = 4;
	
	// Array used to store the range of tetrominos
	private Tetromino[][] flyWeightStructure;
		
	private Random 	randBlock;
	
	//number of tetrominos in flyweight structure
	private int creationCount;
		
	public TwoSource()
	{
		randBlock = new Random();
		flyWeightStructure = new Tetromino[MAXROTATIONS][NUMTETROMINOS];
		creationCount = 0;
	}
	
	public void resetCreationCount()
	{
		creationCount = 0;
	}
	
	public int getCreationCount()
	{
		return creationCount;
	}
	
	//The following methods return information about the passed tetromino
	
	public int getHeightTetromino(Tetromino tempTetromino)
	{	//Max height
		for(int hght = 0;hght < MAXBLOCKSIZE;hght++)
			for(int wdth = 0;wdth < MAXBLOCKSIZE;wdth++)
				if (tempTetromino.tetArray[hght*MAXBLOCKSIZE + wdth]) return (MAXBLOCKSIZE - hght);
		return 0;
	}
		
	public int getWidthTetromino(Tetromino tempTetromino)
	{	//Max width of object
		for(int wdth = (MAXBLOCKSIZE - 1) ;wdth >= 0;wdth--)
			for(int hght = 0;hght < MAXBLOCKSIZE;hght++)
				if (tempTetromino.tetArray[hght*MAXBLOCKSIZE + wdth]) return (wdth + 1);
									
		return 0;
	}
	
	public int getRightPosition(Tetromino tempTetromino,int certainHeight)
	{	//This method takes in an int between 0 and 1 with 0 being the top
		//and returns the rightmost occupied position at that height
		for(int wdth = (MAXBLOCKSIZE - 1) ;wdth >= 0;wdth--)
			if (tempTetromino.tetArray[certainHeight*MAXBLOCKSIZE + wdth]) return (wdth + 1);
									
		return 0;
	}
	
	public int getLeftPosition(Tetromino tempTetromino,int certainHeight)
	{	//This method takes in an int between 0 and 1 with 0 being the top
		//and returns the leftmost occupied position at that height
		for(int wdth = 0;wdth < MAXBLOCKSIZE;wdth++)
			if (tempTetromino.tetArray[certainHeight*MAXBLOCKSIZE + wdth]) return (wdth + 1);
									
		return 0;
	}
	
	public int getTopPosition(Tetromino tempTetromino,int certainWidth)
	{	//returns the topmost occupied position at that width
		for(int hght = 0;hght < MAXBLOCKSIZE;hght++)
			if (tempTetromino.tetArray[hght*MAXBLOCKSIZE + certainWidth]) return (hght + 1);
										
		return 0;
	}
	
	public int getBottomPosition(Tetromino tempTetromino,int certainWidth)
	{	//returns the bottom-most occupied position at that width
		
		if (tempTetromino == null) System.out.println("tetpointer is null");
		if (tempTetromino.tetArray == null) System.out.println("tetArray is null");
				
		for(int hght = (MAXBLOCKSIZE - 1) ;hght >= 0;hght--)
			if (tempTetromino.tetArray[hght*MAXBLOCKSIZE + certainWidth]) return (hght + 1);
									
		return 0;
	}
	
	public int getNoTetrominos()
	{	//returns height of each column
		return NUMTETROMINOS;
	}
	
	public Tetromino clockwise(Tetromino givenTetromino, boolean cw)
	{	//Responsible for the rotation of the tetrominos
		//This needs to be crafted for each piece set
		//Same method, different transform
		
		Tetromino tempTetromino;
		
		int tempRot = givenTetromino.tetRot;
		int currBlock = givenTetromino.tetType;
		
		if(cw)
		{	
			tempRot++;
			if ((MAXROTATIONS - 1)	< tempRot) tempRot = 0;
		}
		else
		{
			tempRot--;
			if (tempRot < 0) tempRot = MAXROTATIONS - 1;
		}
		
		if (flyWeightStructure[tempRot][currBlock] == null)
		{
			tempTetromino = new Tetromino(MAXBLOCKSIZE);
			
			creationCount++;
			
			tempTetromino.tetColor = givenTetromino.tetColor;
			tempTetromino.tetRot = tempRot;
			tempTetromino.tetType = currBlock;
			
			if(cw)
			{	
				tempTetromino.tetArray[1] = givenTetromino.tetArray[0];
				tempTetromino.tetArray[3] = givenTetromino.tetArray[1];
				tempTetromino.tetArray[0] = givenTetromino.tetArray[2];
				tempTetromino.tetArray[2] = givenTetromino.tetArray[3];
			}
			else
			{		
				tempTetromino.tetArray[0] = givenTetromino.tetArray[1];
				tempTetromino.tetArray[1] = givenTetromino.tetArray[3];
				tempTetromino.tetArray[2] = givenTetromino.tetArray[0];
				tempTetromino.tetArray[3] = givenTetromino.tetArray[2];
				
			}
			clearspace(tempTetromino);
			//System.out.println("Just created rotation : " + tempRot + " blockType : " + currBlock);
			//Pass pointer
			flyWeightStructure[tempRot][currBlock] = tempTetromino;
		}
		else tempTetromino = flyWeightStructure[tempRot][currBlock];
		
		return tempTetromino;	
	}
	
	public void clearspace(Tetromino givenTetromino)
	{	//clears space introduced on left and bottom of tetromino during rotation
		
		//Clear from left
		while(!(givenTetromino.tetArray[0] | givenTetromino.tetArray[2]))
		{	for(int temp = 0;temp < (MAXBLOCKSIZE*MAXBLOCKSIZE - 1);temp++)
				givenTetromino.tetArray[temp] = givenTetromino.tetArray[temp + 1];
			givenTetromino.tetArray[(MAXBLOCKSIZE*MAXBLOCKSIZE - 1)] = false;
		}
		//Clear from below, keeps pieces flush with container
		while(!(givenTetromino.tetArray[2] | givenTetromino.tetArray[3]))
		{	for(int temp = (MAXBLOCKSIZE*MAXBLOCKSIZE - 1);temp >= MAXBLOCKSIZE;temp--)
			{
				if ((temp > 3) || ( temp < 2)) System.out.println("Nearly went pear shaped with : " + temp);
				givenTetromino.tetArray[temp] = givenTetromino.tetArray[temp - MAXBLOCKSIZE];
				
			}
			for(int temp = 0;temp < MAXBLOCKSIZE;temp++)
				givenTetromino.tetArray[temp] = false;
		}
	}
	
	
	public int getMaxBlockSize()
	{
		return MAXBLOCKSIZE;
	}
		
	public Tetromino newBlock()
	{	//Tetromino selected from uniform distribution of tetrominos
	
		Tetromino tempTetromino;
	
		int currBlock = randBlock.nextInt(5);
		
		if (flyWeightStructure[0][currBlock] == null)
		{
			tempTetromino = new Tetromino(MAXBLOCKSIZE);
			creationCount++;
			createBlock(tempTetromino,currBlock);
		}
		else tempTetromino = flyWeightStructure[0][currBlock];
		 
		return tempTetromino;
	}
	
	public void createBlock(Tetromino givenTetromino,int currBlock)
	{	//All blocks described in terms of boolean array
		//All start out flush with lower left hand side of array
		
		for(int temp = 0;temp < MAXBLOCKSIZE*MAXBLOCKSIZE;temp++)
			givenTetromino.tetArray[temp] = false;
		
		givenTetromino.tetType = currBlock;
		givenTetromino.tetRot = 0;
		
			
		switch(currBlock)
		{	
			case(SINGLEBLOCK) :	givenTetromino.tetColor = Color.gray;
						givenTetromino.tetArray[2] = true;
						break;
			case(DOUBLEBLOCK) :	givenTetromino.tetColor = Color.green;
						givenTetromino.tetArray[2] = true;
						givenTetromino.tetArray[3] = true;
						break;
			case(LBLOCK) :		givenTetromino.tetColor = Color.magenta;
						givenTetromino.tetArray[1] = true;
						givenTetromino.tetArray[2] = true;
						givenTetromino.tetArray[3] = true;
						break;
			case(SQUAREBLOCK) :	givenTetromino.tetColor = Color.red;
						givenTetromino.tetArray[0] = true;
						givenTetromino.tetArray[1] = true;
						givenTetromino.tetArray[2] = true;
						givenTetromino.tetArray[3] = true;
						break;
			case(XORBLOCK) :	givenTetromino.tetColor = Color.yellow;
						givenTetromino.tetArray[1] = true;
						givenTetromino.tetArray[2] = true;
						break;
			default :		System.out.println("Generating random numbers out of bounds");
						break;
		}
		
		flyWeightStructure[0][currBlock] = givenTetromino;
		//System.out.println("Just created original tetromino : " + currBlock);		
	}
	
	
}

//Donald Carr
//Tetris
//18/6/05
//Coded for Java 1.5

//Classical tetris block set
//7 blocks
//max dimension 4
// (x,y) == (0,0) in top left hand corner

import java.util.Random;
import java.awt.Color;

public class TetrominoSource implements TetrominoSet
{	//Constants
	private final int NUMTETROMINOS = 7;
	
	private final int LONGBLOCK = 0;
	private final int LZIGZAG = 1;
	private final int RZIGZAG = 2;
	private final int SQUAREBLOCK = 3;
	private final int LBEND = 4;
	private final int RBEND = 5;
	private final int KEYBLOCK = 6;
		
	private final int MAXBLOCKSIZE = 4;
	private final int MAXROTATIONS = 4;
	
	private Tetromino[][] flyWeightStructure;
	
	//variables
	private Random 	randBlock;
	
	//number of tetrominos in flyweight structure
	private int creationCount;
		
	public TetrominoSource()
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
	{	//This method takes in an int between 0 and 3 with 0 being the top
		//and returns the rightmost occupied position at that height
		for(int wdth = (MAXBLOCKSIZE - 1) ;wdth >= 0;wdth--)
			if (tempTetromino.tetArray[certainHeight*MAXBLOCKSIZE + wdth]) return (wdth + 1);
									
		return 0;
	}
	
	public int getLeftPosition(Tetromino tempTetromino,int certainHeight)
	{	//This method takes in an int between 0 and 3 with 0 being the top
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
				
		for(int hght = (MAXBLOCKSIZE - 1) ;hght >= 0;hght--)
			if (tempTetromino.tetArray[hght*MAXBLOCKSIZE + certainWidth]) return (hght + 1);
									
		return 0;
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
			
			int jumpStart = 0;
			int jumpSpace;
			
			if(cw)
			{	
				jumpSpace = -5;
				
				for(int temp = 0;temp < MAXBLOCKSIZE*MAXBLOCKSIZE;temp++)
				{	//decrements by five so further simplifiable
					if (temp == 0) jumpStart = 12; 
					if (temp == 4) jumpStart = 9;
					if (temp == 8) jumpStart = 6;
					if (temp == 12) jumpStart = 3;
					tempTetromino.tetArray[temp] = givenTetromino.tetArray[temp + jumpStart];
					jumpStart += jumpSpace;
				}
			}
			else
			{
				jumpSpace = 3;
				
				for(int temp = 0;temp < MAXBLOCKSIZE*MAXBLOCKSIZE;temp++)
				{	//decrements by five so further simplifiable
					if (temp == 0) jumpStart = 3;
					if (temp == 4) jumpStart = -2;
					if (temp == 8) jumpStart = -7;
					if (temp == 12) jumpStart = -12;
					 
					tempTetromino.tetArray[temp] = givenTetromino.tetArray[temp + jumpStart];
					jumpStart += jumpSpace;
				}
			}
			
			clearspace(tempTetromino);
			//System.out.println("Just created rotation : " + tempRot + " blockType : " + currBlock);
			
			//Pass pointer
			flyWeightStructure[tempRot][currBlock] = tempTetromino;
		}
		else tempTetromino = flyWeightStructure[tempRot][currBlock];
		
		return tempTetromino;	
	}
	
	public int getNoTetrominos()
	{	//returns height of each column
		return NUMTETROMINOS;
	}
	
	public void clearspace(Tetromino givenTetromino)
	{	//clears space introduced on left and bottom of tetromino during rotation
		
		//Clear from left
		while(!(givenTetromino.tetArray[0] | givenTetromino.tetArray[4] | givenTetromino.tetArray[8] | givenTetromino.tetArray[12]))
		{	for(int temp = 0;temp < (MAXBLOCKSIZE*MAXBLOCKSIZE - 1);temp++)
				givenTetromino.tetArray[temp] = givenTetromino.tetArray[temp + 1];
			givenTetromino.tetArray[(MAXBLOCKSIZE*MAXBLOCKSIZE - 1)] = false;
		}
		//Clear from below, keeps pieces flush with container
		while(!(givenTetromino.tetArray[12] | givenTetromino.tetArray[13] | givenTetromino.tetArray[14] | givenTetromino.tetArray[15]))
		{	for(int temp = (MAXBLOCKSIZE*MAXBLOCKSIZE - 1);temp >= MAXBLOCKSIZE;temp--)
				givenTetromino.tetArray[temp] = givenTetromino.tetArray[temp - MAXBLOCKSIZE];
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
	
		int currBlock = randBlock.nextInt(7);
		
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
			case(LONGBLOCK) :	givenTetromino.tetColor = Color.gray;
						givenTetromino.tetArray[0] = true;
						givenTetromino.tetArray[4] = true;
						givenTetromino.tetArray[8] = true;
						givenTetromino.tetArray[12] = true;
						break;
			case(LZIGZAG) :		givenTetromino.tetColor = Color.green;
						givenTetromino.tetArray[5] = true;
						givenTetromino.tetArray[9] = true;
						givenTetromino.tetArray[8] = true;
						givenTetromino.tetArray[12] = true;
						break;
			case(RZIGZAG) :		givenTetromino.tetColor = Color.magenta;
						givenTetromino.tetArray[4] = true;
						givenTetromino.tetArray[8] = true;
						givenTetromino.tetArray[9] = true;
						givenTetromino.tetArray[13] = true;
						break;
			case(SQUAREBLOCK) :	givenTetromino.tetColor = Color.red;
						givenTetromino.tetArray[8] = true;
						givenTetromino.tetArray[9] = true;
						givenTetromino.tetArray[12] = true;
						givenTetromino.tetArray[13] = true;
						break;
			case(LBEND) :		givenTetromino.tetColor = Color.yellow;
						givenTetromino.tetArray[4] = true;
						givenTetromino.tetArray[8] = true;
						givenTetromino.tetArray[12] = true;
						givenTetromino.tetArray[13] = true;
						break;
			case(RBEND) :		givenTetromino.tetColor = Color.white;
						givenTetromino.tetArray[5] = true;
						givenTetromino.tetArray[9] = true;
						givenTetromino.tetArray[13] = true;
						givenTetromino.tetArray[12] = true;
						break;
			case(KEYBLOCK) :	givenTetromino.tetColor = Color.orange;
						givenTetromino.tetArray[4] = true;
						givenTetromino.tetArray[8] = true;
						givenTetromino.tetArray[9] = true;
						givenTetromino.tetArray[12] = true;
						break;
			default :		System.out.println("Generating random numbers out of bounds");
						break;
		}
		
		flyWeightStructure[0][currBlock] = givenTetromino;
		//System.out.println("Just created original tetromino : " + currBlock);		
	}
	
	
}

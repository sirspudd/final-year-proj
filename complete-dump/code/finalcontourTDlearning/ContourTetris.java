//Donald Carr
//Tetris
//18/6/05
//Coded for Java 1.5

//This is the rudimentary tetris game
//responsible for all well activity

public class ContourTetris implements TetrisGame
{	//Constants
	
	private final int LEFT = 0;
	private final int RIGHT = 1;
	private final int UP = 2;
	private final int DOWN = 3;
	
	//This dictates the breadth of the well throughout the game
	private final int WIDTH;
	
	//STIPULATE THE DIMENSIONS OF THE WELL HERE
	//IN ORDER FOR LEARNING TO OCCUR FULLWIDTH MUST EQUAL CONCEPTUALWIDTH
	//EXTENDING THE CONTOUR LEARNER INVOLVES SELECTING THE APPROPRIATE CONCEPTUAL WIDTH : SAME SIZE AS WHEN TRAINED
	//THE FULLWIDTH CAN THEN BE SET TO ANY WIDTH GREATER THEN THIS
	
	private final int FULLWIDTH = 5;
	private final int CONCEPTUALWIDTH = 5;
	
	//This dictates the height of the well throughout the game
	private final int HEIGHT = 20;
	
	private final int MAXBLOCKSIZE;
	
	//number of blocks in each game
	private final int TETROMINOLIMIT = 10000;
	private final boolean HEIGHTLIMITED = false;
	
	//Variables
	
	//NB : The agent class MUST be rebuilt when changing the different tetris blocksets
	private TetrominoSet	theSource;
	
	private boolean[]	blocks;
	private Tetromino	currentTetromino;
	private Tetromino	nextTetromino;
	
	private int		blockPosY;
	private int		blockPosX;
	private int		reward;
	//tallies the number of completed rows
	private int		score;
	
	private int		blockCount = 0;
	private boolean 	deceased;
	private boolean 	conceptual;
	
	private int 	previousCoveredHoles = 0;
	private int 	previousHeight = 0;
	
	public	ContourTetris()
	{	//real game
		
		WIDTH = FULLWIDTH;
		
		deceased = false;
		
		//For conceptual games where the state variables are insignificant
		conceptual = false;
		
		score = 0;
		
		//Choose blockset
		theSource = new TwoSource();
		
		//Tetromino limits dictated by blockset
		MAXBLOCKSIZE = theSource.getMaxBlockSize();
		
		//boolean array represents well contents
		blocks = new boolean[WIDTH*HEIGHT];
		
		for(int temp = 0; temp < WIDTH*HEIGHT;temp++)
		{
			blocks[temp] = false;
		}
		
		nextTetromino = theSource.newBlock();
		nextPiece();
	}
	
	public	ContourTetris(TetrominoSet tempSource,boolean full)
	{	//conceptual game
		
		//Two distinct conceptual wells are required in using reduced wells
		//to solve the whole problem
		
		if (full) WIDTH = FULLWIDTH;
		else WIDTH = CONCEPTUALWIDTH;
			
		deceased = false;
		conceptual = true;
		
		theSource = tempSource;
		MAXBLOCKSIZE = theSource.getMaxBlockSize();
	}
	
	public TetrominoSet getSource()
	{
		return theSource;
	}
	
	public boolean isConceptual()
	{
		return conceptual;
	}
	
	public int getWidth()
	{
		return WIDTH;
	}
	
	public int getHeight()
	{
		return HEIGHT;
	}
	
	/*
	Using boolean arrays to describe all interacting components :
	On Left : make certain that tetromino is not at left boundary of well else ignore
		  make certain no blocks in well against left side of tetromino else ignore
		  
	On Right : make certain that tetromino is not at right boundary of well else ignore
		  make certain no blocks in well against right side of tetromino else ignore
	
	On Down : make sure not at bottom else combine
		  make sure not against blocks in well else combine
		  make sure not at top when combining, else kill
		  
	*/	
	
	synchronized public boolean move(int temp)
	{	boolean combined = false;
		
		if(!deceased || conceptual)
		{
			boolean badMove = false;
						
			switch(temp)
			{
				case LEFT:	
						if (blockPosX < 0) System.out.println("I cant constrain it, things have already gone pear shaped on the left hand side : X Pos : " + blockPosX);
						if (blockPosX == 0) ;
						else
						{
							/*
								Run down left hand side of block
								check well position bounding existing leftmost components of side of block
								if all positions empty allow movement left
							*/
							for(int hght = 0; hght < MAXBLOCKSIZE; hght++)
								if ((hght + blockPosY - MAXBLOCKSIZE) >= 0)
									if (theSource.getLeftPosition(currentTetromino,hght) > 0)
											badMove = badMove | blocks[(hght + blockPosY - MAXBLOCKSIZE)*WIDTH + blockPosX + theSource.getLeftPosition(currentTetromino,hght) - 2];
							
									
							if (!badMove) blockPosX--;
						}
						break;
				case RIGHT:	
						if (blockPosX > WIDTH) System.out.println("I cant constrain it, things have already gone pear shaped on the right hand side : X Pos : " + blockPosX);
						if ((blockPosX + theSource.getWidthTetromino(currentTetromino)) == WIDTH) ;
						else 
						{	/*
								Run down right hand side of block
								check well position bounding exisiting rightmost components of side of block
								if all positions empty allow movement right
							*/
							
							for(int hght = 0; hght < MAXBLOCKSIZE; hght++)
								if ((hght + blockPosY - MAXBLOCKSIZE) >= 0)
									if (theSource.getRightPosition(currentTetromino,hght) > 0)
											badMove = badMove | blocks[(hght + blockPosY - MAXBLOCKSIZE)*WIDTH + blockPosX + theSource.getRightPosition(currentTetromino,hght)];
							
							if (!badMove) blockPosX++;
						}
						break;
				case DOWN:	
						if (blockPosY > HEIGHT)
						{
							System.out.println("I cant constrain it, things have already gone pear shaped along the y axis : Y Pos : " + blockPosY);//Just get thing to quit
							return true;
						}
						if (blockPosY == HEIGHT)
						{	
							combined = true;
							combine();
						}
						else 
						{	
							/*
								Run across bottom of block
								check well position bounding exisiting bottom-most components of bottom of block
								if all positions empty allow movement down
							*/
							
							for(int wdth = 0; wdth < MAXBLOCKSIZE; wdth++)
								if(theSource.getBottomPosition(currentTetromino,wdth) > 0)
									if((blockPosY + theSource.getBottomPosition(currentTetromino,wdth) - MAXBLOCKSIZE)>=0)
									 badMove = badMove | blocks[(blockPosY + theSource.getBottomPosition(currentTetromino,wdth) - MAXBLOCKSIZE)*WIDTH + blockPosX + wdth];
							
							if (!badMove) blockPosY++;
							else
							{
								combined = true;
								combine();
							}
							
						}
						break;
			}
				
			return combined;
		}
		else return false;
	}
	
	public int getReward()
	{	return reward;
	}
	
	public int getMaxBlockSize()
	{	
		return MAXBLOCKSIZE;
	}
		
	public Tetromino getCurrentTetromino()
	{
		return currentTetromino;
	}
	
	public Tetromino getNextTetromino()
	{
		return nextTetromino;
	}
	
	public int getXPos()
	{
		return blockPosX;
	}
	
	
	public int getYPos()
	{
		return blockPosY;
	}
	
	public int getScore()
	{
		return score;
	}
	
	public boolean isDead()
	{
		return deceased;
	}
	
	synchronized public boolean[] getBlocks()
	{	//returns the block structure
		return blocks;
	}
	
	public void restartTetris()
	{	//resets the game
		for(int temp = 0; temp < WIDTH*HEIGHT;temp++)
		{
			blocks[temp] = false;
		}
		nextPiece();
		deceased = false;
		score = 0;
	}
	
		
	synchronized public void nextPiece()
	{	//Generates next piece
	
		blockPosY = 0;
		blockPosX = 0;
		currentTetromino = nextTetromino;
		nextTetromino = theSource.newBlock();
	}
	
	
	synchronized public void rotate(boolean clockwise)
	{	//Checks validity of requested rotate and performs it
	
		if(!deceased || conceptual)
		{
			//Checking for intersection
			
			Tetromino tempTetromino;
			
			boolean badRotate = false;
			
			if(clockwise) tempTetromino = theSource.clockwise(currentTetromino,true);
			else	tempTetromino = theSource.clockwise(currentTetromino,false);
			
			//Checks rotation beyond right bounds
			
			if((theSource.getWidthTetromino(tempTetromino) + blockPosX) > WIDTH) badRotate = true;
			else	for(int hght = 0;hght < MAXBLOCKSIZE;hght++)
					for(int wdth = 0;wdth < MAXBLOCKSIZE;wdth++)
						if((blockPosY + hght - MAXBLOCKSIZE) >= 0)
							if(tempTetromino.tetArray[hght*MAXBLOCKSIZE + wdth])
								{
									badRotate = badRotate | blocks[(blockPosY + hght - MAXBLOCKSIZE)*WIDTH + blockPosX + wdth];
								}
			
			//No need to check left rotation		
			
			if(!badRotate)
				if(clockwise) currentTetromino = tempTetromino;
				else	currentTetromino = tempTetromino;
		}
	}
	
	
	synchronized public void combine()
	{	//Stores the number of covered holes in the original structure
		previousCoveredHoles = getNoCoveredHoles();
		previousHeight = getResultingHeight();
		
		//Cements the active Tetromino into the wall structure
	
			for(int hght = 0;hght < MAXBLOCKSIZE;hght++)
				if ((blockPosY + hght - MAXBLOCKSIZE)>=0)
				{
					for(int wdth = 0;wdth < MAXBLOCKSIZE;wdth++)
					{	
						if (currentTetromino.tetArray[hght*MAXBLOCKSIZE + wdth])
							{
								blocks[(blockPosY + hght - MAXBLOCKSIZE)*WIDTH + blockPosX + wdth] = true;
							}
					}
				}
		reduce();
		if (!conceptual) nextPiece();
	}
	
		
	synchronized public void reduce()
	{	//checks for completed lines, reduces well and awards points accordingly
	
		int numRowsCompleted = 0;
		
		for(int hght = 0;hght < HEIGHT;hght++)
		{
			boolean rowComplete = true;
			for(int wdth = 0;wdth < WIDTH;wdth++)
			{	
				rowComplete = rowComplete & blocks[hght*WIDTH + wdth];
			}
			if (rowComplete)
			{	//One scoring method can award points here for row completetion
				numRowsCompleted++;
				//tally of completed rows
				score++;
				
				//shifts down rows above completed row
				for(int hght2 = hght;hght2 > 0;hght2--)
				{
					for(int wdth2 = 0;wdth2 < WIDTH;wdth2++)
					{	
						blocks[hght2*WIDTH + wdth2] = blocks[(hght2-1)*WIDTH + wdth2];
					}
				}
				//inserts blank row at top
				for(int wdth2 = 0;wdth2 < WIDTH;wdth2++)
					{	
						blocks[wdth2] = false;
					}
			}
			
		}
		
		///////////////////////////////////////////////////////////////////////
		//REWARD FUNCTION DEFINED HERE
		///////////////////////////////////////////////////////////////////////
		
		//Update number of covered holes
		
		//gauging death
		
		reward = 0;
		
		if ((blockPosY == 0) || ((++blockCount == TETROMINOLIMIT) && HEIGHTLIMITED))
		{
			deceased = true;
			blockCount = 0;
		}
		else
		switch(numRowsCompleted)
		{	
			case 1 :reward = 100; 
				break;
			case 2 :reward = 200; 
				break;
			case 3 :reward = 300; 
				break;
			case 4 :reward = 400; 
				break;
			default: reward = 0;	
		}
		
		
		/*
		int changeHeight = getChangeHeight();
		
		int tempHoles = getNoNewCoveredHoles();
		
		reward += changeHeight*-100;
		if (tempHoles > 3) tempHoles = 3;
		reward += tempHoles*-40;
		*/
	}
	
	public int[] getHeightWell()
	{	//returns height of each column
		int height;
		int heights[] = new int[WIDTH];
		
		for(int wdth = 0; wdth < WIDTH;wdth++)
		{	height = 0;
			while((height < 20) && !blocks[height*WIDTH + wdth])
			{
				height++;
			}
			heights[wdth] = 20 - height;
		}
		return heights;
	}
	
	public int getNoTetrominos()
	{	//returns height of each column
		return theSource.getNoTetrominos();
	}
			
	public int getNoCoveredHoles()
	{	//returns number of covered holes
		
		int tempHoles = 0;
		boolean[] roofBlocks = new boolean[WIDTH];
		
		for(int count = 0; count < WIDTH; count++)
			roofBlocks[count] = false;
		
		for(int outer = 0; outer < HEIGHT; outer++)
			for(int inner = 0; inner < WIDTH ; inner++)
				if(blocks[outer*WIDTH + inner]) roofBlocks[inner] = true;
				else if (roofBlocks[inner]) tempHoles++;
		return tempHoles;
	}
	
	public int getNoNewCoveredHoles()
	{	//returns number of covered holes
    		return (getNoCoveredHoles() - previousCoveredHoles);
	}
    
	public int getChangeHeight()
	{	//returns number of covered holes
    		return (getResultingHeight() - previousHeight);
	}
	
	public boolean[] getCoveredHoles()
	{	//returns bool array showing columns with covered holes
		
		boolean[] tempHoles = new boolean[WIDTH];
		
		boolean[] roofBlocks = new boolean[WIDTH];
		
		for(int count = 0; count < WIDTH; count++)
			{
				tempHoles[count] = false;
				roofBlocks[count] = false;
			}
		
		for(int outer = 0; outer < HEIGHT; outer++)
			for(int inner = 0; inner < WIDTH ; inner++)
				if(blocks[outer*WIDTH + inner]) roofBlocks[inner] = true;
				else if (roofBlocks[inner]) tempHoles[inner] = true;
				
		return tempHoles;
	}
	
	
	
	public int getResultingHeight()
	{	//returns heightest point in well
		for(int outer = 0; outer < HEIGHT; outer++)
			for(int inner = 0; inner < WIDTH; inner++)
				if(blocks[outer*WIDTH + inner]) return (HEIGHT - outer);
		
		
		return 0;
	}
	
	
	public void printBlocks()
	{	//Debugging aid : displays block structure in text
		System.out.println("Printing Blocks");
		for(int outer = 0; outer < HEIGHT; outer++)
		{
			for(int inner = 0; inner < WIDTH; inner++)
				if(blocks[outer*WIDTH + inner]) System.out.print("1");
				else System.out.print("0");
			System.out.println();
		}
		System.out.println();
	}
	
	
	public void printTetromino()
	{	//Debugging aid : displays tetromino structure in text
	
		for(int outer = 0; outer < MAXBLOCKSIZE; outer++)
		{
			for(int inner = 0; inner < MAXBLOCKSIZE; inner++)
				if(blocks[outer*WIDTH + inner]) System.out.print("1");
				else System.out.print("0");
			System.out.println();
		}
		
	}
      	
      	public void setBlocks(boolean[] _blocks)
	{	//used in conceptual games
		blocks = _blocks;
	}
	
	public void setCurrentTetromino(Tetromino _currentTetromino)
	{	//used in conceptual games
		if (_currentTetromino == null) System.out.println("Pointer passed to virtual is null");
		currentTetromino = _currentTetromino;
	}
	
	public void resetYPos()
	{
		blockPosY = 0;
	}	
	
	public void resetXPos()
	{
		blockPosX = 0;
	}
	
}

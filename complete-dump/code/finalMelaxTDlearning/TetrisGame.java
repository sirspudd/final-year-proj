//Donald Carr
//Tetris
//18/6/05
//Coded for Java 1.5

//Interface for the tetromino structures

interface TetrisGame
{	
	TetrominoSet getSource();
	
	boolean move(int temp);
	
	Tetromino getCurrentTetromino();
	
	Tetromino getNextTetromino();
	
	int getWidth();
	
	int getHeight();
	
	int getXPos();
	
	int getYPos();
	
	int getScore();
	
	int getReward();
	
	int getMaxBlockSize();
	
	boolean isDead();
	
	boolean[] getBlocks();
	
	void restartTetris();
	
	void nextPiece();
	
	void printBlocks();
	
	void rotate(boolean clockwise);
	
	boolean isConceptual();
	
	int[] getHeightWell();
	
	int getNoTetrominos();
			
   	 int getNoNewCoveredHoles();
	
	boolean[] getCoveredHoles();
	
	int getResultingHeight();
	
	void setBlocks(boolean[] _blocks);
	
	void setCurrentTetromino(Tetromino _currentTetromino);
	
	void resetYPos();
	
	void resetXPos();
	
}
//Donald Carr
//TetrominoSet
//18/6/05
//Coded for Java 1.5

//Interface for the tetromino structures

interface TetrominoSet
{	
	int getHeightTetromino(Tetromino tempTetromino);

	int getWidthTetromino(Tetromino tempTetromino);
	
	int getRightPosition(Tetromino tempTetromino,int certainHeight);
	
	int getLeftPosition(Tetromino tempTetromino,int certainHeight);
	
	int getTopPosition(Tetromino tempTetromino,int certainWidth);
	
	int getBottomPosition(Tetromino tempTetromino,int certainWidth);
	
	Tetromino clockwise(Tetromino givenTetromino, boolean cw);
	
	int getNoTetrominos();
	
	int getMaxBlockSize();

	Tetromino newBlock();
}

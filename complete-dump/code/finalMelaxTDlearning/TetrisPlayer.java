
//Donald Carr
//TetrominoSet
//18/6/05
//Coded for Java 1.5

//Interface for the tetromino structures

interface TetrisPlayer extends Runnable
{	
	void toggleActive();
	
	void interruptTraining();
	
	void toggleDebug();
	
	void flushPlayer();
	
	void toggleObservablePace();
	
	void setObservablePace();
	
	void toggleExploring();
	
	void toggleLearning();
	
	void revive();
	
	void respond();
	
	void load(String fileName);
	
	void save(String fileName);
	
	void log(int score);
	
	void train(int numGames);
	
	void start();
	
	void printMind();
	
	void dumpValues();
}
//Donald Carr
//Tetris4
//18/6/05
//Coded for Java 1.5

//Results generating script
//Minus graphical overhead
//sans user interaction

public class WindowlessTetris
{	//Constants

	//Different variations require changes here and in the tetris games themselves.
	//1. Game type and player type set in TetrisWindow
	//2. Tetromino characteristics set in tetris games
	
	//Choose the appropriate game	
	private	static TetrisGame theGame;
	//Choose the appropriate player
	private static TetrisPlayer thePlayer;
	
	public static void main(String[] args)
	{
		theGame = new ContourTetris();
		thePlayer = new ContourRLGuru(theGame,null);
		thePlayer.start();
		thePlayer.toggleActive();
		//Using optomistic exploration
		thePlayer.toggleExploring();
		thePlayer.train(9999999);
	}
}	

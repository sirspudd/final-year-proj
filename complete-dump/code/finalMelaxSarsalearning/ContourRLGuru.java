
//Donald Carr
//15th June 2005

//Contour Based RL Player
//See Applying Reinforcement Learning to Tetris by Donald Carr

import java.util.Random;
import java.util.Vector;
import java.util.Date;
import java.io.*;

class ContourRLGuru extends Thread implements TetrisPlayer
{	//constants
	
	//LEARNER TYPE
	//EXCLUSIVE SELECTION OF LEARNER TYPE
	
	private final boolean EGREEDY = true;
	private final boolean SOFTMAX = !EGREEDY;
		
	private final byte LEFT = 0;
	private final byte RIGHT = 1;
	private final byte UP = 2;
	private final byte DOWN = 3;
	
	private final int WIDTH;
	private final int REDUCEDWIDTH;
	private final int HEIGHT;
	
	//Time in Milliseconds
	private final int AGENTMOVETIME = 50;
	
	//Grid based rotations
	private final int ORIENTATIONS = 4;
	
	//Number of distinct tetrominos passed from game
	private final int NUMTETROMINOS;
	
	//no of states due to contours
	private final int MELAXSPACE;
	//no of distinct states
	private final int STATEACTIONSPACE;
	
	//Offset between game well and agent state space
	private final int STARTPOS;
	
	//large constant well shy of 2^31 - 1 but big enough
	private final int MASSIVE = 10000000; 
	
	//Starting value assigned to each value in value function
	private final double STARTINGVALUE = 0;
	
	//Max permitted height diff between contours
	//... -4 -3 -2 -1 0 1 2 3 4 ....
	private final int MAXHEIGHTDIFF = 3;
	
	//Number of height terms (must be odd)
	private final int NOHEIGHTTERMS = 2*MAXHEIGHTDIFF + 1;
	
	//The number of reduced wells in the full well
	private final int REDUCEDFULL;
	
	//The resolution of the values in our value fuction	
	private final double FLOATERR = 0.00001;
	
	//used in sarsa algorithm
	private final double GAMMA = 0.8;
	private final double ALPHA = 0.2;
	
	//variables
	
	private TetrisGame theGame;
	
	//conceptual game : used only as agents testbed for placing blocks
	private TetrisGame virtualGame;
	
	//conceptual game : used only as agents testbed for full well response
	private TetrisGame virtualFullGame;
	
	//pointer enabling window to be alerted to actions on the agents part
	private TetrisWindow gameWindow;
	
	private boolean active;
	private boolean interruptTraining;
	private boolean training;
	private boolean deceased;
	private boolean observablePace;
	private boolean exploring;
	private boolean learning;
	private boolean debug;
	
	//Number of games the agent has played
	private int noOfGames = 0;
	
	//used in sarsa algorithm
	private double actionReward = 0;
	private int nextHashCode = 0;
		
	//used by SOFTMAX learning method
	//	0.01 results in rapid learning : little EGREEDY prehaps
	private double temperature = 1;
	//percentage to reduce temp by
	private double temperatureDecrease = 0.05; 
	//limit temp reduction
	private final double TEMPLIMIT = 0.1; 
	//move period between temp reduction
	private final int RESPONSELIMIT = 10000; 
	//	count since last reduce temp
	private int responseCount = 0; 
	
	//Log information
	private String typeGame;
	private String typePlayer;
 	
	//Require single pointer to original block structure
	private boolean[] realBlocks;
	
	boolean initialised = false;
		
	//Value "table" (array)
	private double[] V;
	
	private Random randNo;
	
	//////////////////////////////////////////////////////////////
	
	public ContourRLGuru(TetrisGame _theGame, TetrisWindow _gameWindow)
	{	
		this.setPriority(MIN_PRIORITY);
		
		typeGame = "ExtendableContour";
		
		randNo = new Random();
		
		theGame = _theGame;
		
		if(_gameWindow == null)
		{	//Used only for massive data collection
			System.out.println("Windowless tetris game : Must be training");
			System.out.println("This should only be used for data collection");
		}
		else
		{
			//Pass pointer to window in order to avoid polling
			gameWindow = _gameWindow;
		}
		
		//conceptual games instantiated here
		virtualGame = new ContourTetris( theGame.getSource() ,false);
		virtualFullGame = new ContourTetris( theGame.getSource() ,true);
		
		//Pointed to block structure, once off
		//block structure is maintained throughout life of agent
		realBlocks = theGame.getBlocks();
		
		NUMTETROMINOS = theGame.getNoTetrominos();
		
		HEIGHT = theGame.getHeight();
		WIDTH = theGame.getWidth();
		
		REDUCEDWIDTH = virtualGame.getWidth();
		
		//There are WIDTH - REDUCED WIDTH + 1 reduced wells to a full well
		REDUCEDFULL = WIDTH - REDUCEDWIDTH + 1;
		
		//My reduced contour representation : 
		//see Applying Reinforcement learning to Tetris by Donald Carr
		
		MELAXSPACE = (int) Math.pow(2,12);
		
		//The full size of the (s,a) table
		STATEACTIONSPACE = MELAXSPACE * NUMTETROMINOS * ORIENTATIONS * REDUCEDWIDTH; 
		STARTPOS = 2*REDUCEDWIDTH;
		
		V = new double[STATEACTIONSPACE]; 
		
		//initialise value function
		flushPlayer();
		
		//set initial boolean values
		active = false;
		interruptTraining = false;
		training = false;
		deceased = false;
		observablePace = true;
		exploring = true;
		learning = true;
		debug = false;
		
	}
	
	public void toggleActive()
	{
		active = !active;
	}
	
	public void interruptTraining()
	{
		interruptTraining = true;
	}
	
	public void toggleDebug()
	{
		debug = !debug;
	}
	
	public void flushPlayer()
	{
		for(int count = 0;count < STATEACTIONSPACE;count++)
			V[count] = randNo.nextDouble();
			//V[count] = STARTINGVALUE;
	}
	
	public void toggleObservablePace()
	{
		observablePace = !observablePace;
	}
	
	public void setObservablePace()
	{
		observablePace = true;
	}
	
	public boolean getObservablePace()
	{
		return observablePace;
	}
	
	public void toggleExploring()
	{
		exploring = !exploring;
	}
	
	public void toggleLearning()
	{
		learning = !learning;
	}
	
	synchronized public void revive()
	{	//revives agent : mind maintained
		noOfGames++;
		log(theGame.getScore());
		
		theGame.restartTetris();
		
		//Important reintialisations
		initialised = false;
		nextHashCode = 0;
		
		deceased = false;
		
		if (!training) gameWindow.reviveWindow();
	}
	
	synchronized public void respond()
	{	
		//This should never occur
		if (deceased) System.out.println("Race condition : Deceased in respond method");
		
		typePlayer = "MelaxSarsaPlayer";
		
		//all possibles transitions from current state and tetromino
		Vector posValues = new Vector();
		
		PosMovRot transition;
		
		int currentHashCode;
		
		int chosen;
		
		////////////////////////////////////////////////////////////
		
		//(s,a) hash code or (s') depending on conceptual model
		currentHashCode = nextHashCode;
		
		//find  a'
		findTransitions(posValues);
		
		if (WIDTH != REDUCEDWIDTH) posValues = multiToSingle(posValues);
		
		//choose a'		
		if (EGREEDY) chosen = greedy(posValues); 
		if (SOFTMAX) chosen = softMax(posValues);
		
		transition = (PosMovRot) posValues.get(chosen);
		
		//(s',a') hash code
		//System.out.println("Real Game");
		nextHashCode = makeMove(theGame,transition,false);
		
		//The reward associated with this transition is taken to be the reward attained
		//in taking the next action in the destination state
				
		if (debug) System.out.println("Rewarded : " + transition.reward);
		
		if(!initialised) initialised = true;
		else
			if(learning && (WIDTH == REDUCEDWIDTH))
			{	//actual learning can only occur in wells where the virtual and real width agree
				learn(currentHashCode,nextHashCode,actionReward);
			}
		
		//This reward is carried forth to reward a' in the next iteration	
		actionReward = transition.reward;	
	}
	
	public void learn(int currentHashCode, int nextHashCode,double reward)
	{	
		double nextValue = V[nextHashCode];
		
		//defined as such in Sutton & Barto
		if (theGame.isDead()) nextValue = 0;
		
		double delta = reward + GAMMA*nextValue - V[currentHashCode];
		
		V[currentHashCode] = V[currentHashCode] + ALPHA*delta;
		
	}
	
	synchronized public void findTransitions(Vector posValues)
	{	//This method searchs for all possible transtions
		//prevents wide blocks being repeatedly dropped at end position
		
		PosMovRot transition;
		
		//Clear the possible transition vector
		posValues.clear();
		
		//Using td
		//Drop tetromino in every position in cognitive model of game
		//to discover predicted value
				
		if (debug)
		{
			System.out.println();
			System.out.println("Tetromino : " + theGame.getCurrentTetromino().tetType);
			System.out.println();
		}
		
		for(int wells = 0; wells < REDUCEDFULL; wells++)
		{	//First loop runs across the complete set of wells
		
			if (debug) System.out.println("wells : " + wells);

			for(int rotations = 0; rotations < ORIENTATIONS;rotations++)
			{	//Second loop cycles through different orientations
			
				if (debug) System.out.println("rotations : " + rotations);
				
				for(int moves = 0;moves < REDUCEDWIDTH; moves++)
					{	//Third loop runs across the complete width of the well
						if (debug) System.out.println("moves : " + moves);
						
						//Initial values requested to test transition
						transition = new PosMovRot(0,0,wells,moves,rotations);
						
						//Last parameter used by td method
						fakeMove(virtualGame,transition,false);
						
						if (transition.mov == moves) posValues.addElement(transition);
						
						adjustTransition(virtualFullGame,transition);
						
						
					}
					if (debug) System.out.println();
				}
				if (debug) System.out.println();
		}
		if (debug) System.out.println();
		
		if(debug)
		{
			System.out.println("Spectrum of Values");
			
			for(int count = 0;count < posValues.size();count++)
			{
				System.out.print("\t" + ((PosMovRot) posValues.get(count)).value);
			}
			System.out.println();
		}
	}
	
	synchronized public void adjustTransition(TetrisGame testGame,PosMovRot transition)
	{	
		//A conceptual model of the full game must be passed
		
		//this method adjusts the transition object for the full well
		
		PosMovRot tempTransition;
		
		transition.mov = transition.mov + transition.well;
		transition.well = 0;
		
		tempTransition = new PosMovRot(0,0,0,transition.mov,transition.rot);
		
		//Last parameter is comparrison boolean
		//System.out.println("Virtual Full Game");
		fakeMove(testGame,tempTransition,false);
		
		transition.reward = tempTransition.reward;
		//transition.value = transition.value + tempTransition.reward;
	}
	
	synchronized public int fakeMove(TetrisGame testGame,PosMovRot transition,boolean comp)
	{	final int TEMPWIDTH = testGame.getWidth();
		
		//temporary blocks array for conceptual Game instantiated 
		boolean[] tempBlocks = new boolean[TEMPWIDTH*HEIGHT];
	
		for(int hght = 0;hght < HEIGHT;hght++)
			for(int wdth = 0;wdth < TEMPWIDTH;wdth++)
				if (realBlocks[hght*WIDTH + wdth + transition.well]) tempBlocks[hght*TEMPWIDTH + wdth] = true;
				else tempBlocks[hght*TEMPWIDTH + wdth] = false;
	
		testGame.setBlocks(tempBlocks);
		testGame.setCurrentTetromino(theGame.getCurrentTetromino());
		
		//testGame.printBlocks();
		
		return makeMove(testGame,transition, comp);
	}
	
	synchronized public int makeMove(TetrisGame testGame,PosMovRot transition,boolean comp)
	{	int hash = 0;
		
		testGame.resetYPos();
		/*
		if (debug)
		{
			System.out.println("Rotations : " + transition.rot);
			System.out.println("Moves : " + transition.mov);		
			System.out.println("Wells : " + transition.well);		
		}*/
		
		for(int count = 0; count < testGame.getWidth(); count++)
			testGame.move(LEFT);
		
		for(int outer = 0; outer < transition.rot;outer++)
			testGame.rotate(true);
		
		for(int outer = 0; outer < transition.mov;outer++)
			testGame.move(RIGHT);
		
		place(testGame);
		
		//Only of consequence in conceptual game
		if (testGame.isConceptual()) transition.mov = testGame.getXPos();
		
		testGame.resetXPos();
		
		if(testGame.getWidth() == REDUCEDWIDTH)
		{	//VIP, learning only occurs in wells with WIDTH == REDUCEDWIDTH
			
			//This is the mirror hash Value of the contour 
			//Mirror symmetry is not adopted with the sarsa player  
			hash = rawHash(testGame.getBlocks());
			
			//This hash corrects for the actions in sarsa
			//And enables the expansion of the td method;
			hash = adjustHash(hash,virtualGame.getCurrentTetromino().tetType,transition.mov,transition.rot);
			transition.value = V[hash];
			if (comp) hash = rawHash(testGame.getBlocks());
		}			
		//else System.out.println("No hash Code");
		
		transition.reward = testGame.getReward();
		
		return hash;
	}
	
	
	
	synchronized public int greedy(Vector posValues)
	{		//This is the EGREEDY approach
			
			//System.out.println("The length : " + posValues.size());
			
			//All transformations have been gathered
			//When finding biggest so far, clear vector and add count pos to vector
			//If biggest is found again add position to vector again
			//This enables the game to choose randomly between states with the highest value, rather then favouring 		
			//the first/last encountered
			
			int chosen;
			double biggest = -MASSIVE;
			Vector maxValues = new Vector();
			
			for(int count = 0;count < posValues.size();count++)
			{
				if ( ((PosMovRot) posValues.get(count)).value > biggest)
				{
					biggest = ((PosMovRot) posValues.get(count)).value;
					//chosen = count;
					maxValues.clear();
					maxValues.addElement(new Integer(count));
				}
				else if (( ((PosMovRot) posValues.get(count)).value > (biggest - FLOATERR)) && ( ((PosMovRot) posValues.get(count)).value < (biggest + FLOATERR)))
						maxValues.addElement(new Integer(count));
			}
			
			
			/////////////////////////////////////////////////////////////////////////////////////////
			
			//States that have equal values have equiprobability of being selected
			
			chosen = ((Integer) maxValues.get(randNo.nextInt(maxValues.size()))).intValue();
	
			/////////////////////////////////////////////////////////////////////////////////////////
			
			//This is the epsilon part of epsilon-GREEDY methods
			
			//If exploring take arb path occasionally
						
			if (exploring)
				if (randNo.nextInt(16) == 4) chosen = randNo.nextInt(posValues.size());
			
			return chosen;
			
	}
	
	public int softMax(Vector posValues)
	{		//This is the SOFTMAX method of intelligently exploring the state space.
			//SOFTMAX : Limiting this to numbers between 0 and 1
			
			double tempNum = MASSIVE;
			double decide;
			int chosen = 0;
			
			/////////////////////////////////////
			
			if (responseCount++ == RESPONSELIMIT) 
			{
				if (temperature > TEMPLIMIT) temperature = temperature - temperatureDecrease*temperature;
				else temperature = TEMPLIMIT;
				responseCount = 0;
			}
		
			///////////////////////////////////////
			
			//get smallest value
			for(int count = 0;count < posValues.size();count++)
				if (((PosMovRot) posValues.get(count)).value < tempNum) tempNum = ((PosMovRot) posValues.get(count)).value;
			
			//subtract this from all values : therefore all values greater then zero
			
			for(int count = 0;count < posValues.size();count++)
				((PosMovRot) posValues.get(count)).value = ((PosMovRot) posValues.get(count)).value - tempNum;
			
			//Should now have positive numbers 
			
			tempNum = -MASSIVE;
			
			//find biggest
					
			for(int count = 0;count < posValues.size();count++)
				if (((PosMovRot) posValues.get(count)).value > tempNum) tempNum = ((PosMovRot) posValues.get(count)).value;
			
			//normalise all values using largest number
			
			for(int count = 0;count < posValues.size();count++)
				((PosMovRot) posValues.get(count)).value = (((PosMovRot) posValues.get(count)).value)/tempNum;
			
			//Should now have numbers between 0 and 1
			
			//temperature determines the agents policy
			//as temperature -> 0 the agents policy becomes EGREEDY
			//as temperature tends -> oo the agents policy becomes random
			
			
			//temeperature value comes into play here
			
			for(int count = 0;count < posValues.size();count++)
				((PosMovRot) posValues.get(count)).value = (((PosMovRot) posValues.get(count)).value)/temperature;
			
			for(int count = 0;count < posValues.size();count++)
				((PosMovRot) posValues.get(count)).value = Math.exp(((PosMovRot) posValues.get(count)).value);
			
			//Should have the gibbs distribution
			
			double totalSpread = 0;
			
			for(int count = 0;count < posValues.size();count++)
				totalSpread += ((PosMovRot) posValues.get(count)).value;
			
			for(int count = 0;count < posValues.size();count++)
				((PosMovRot) posValues.get(count)).value = (((PosMovRot) posValues.get(count)).value)/totalSpread;
			
			//Should now have numbers that sum to 1
					
			double previousNum = 0;
			double currentNum = 0;
			
			decide = randNo.nextDouble();
			
			for(int count = 0;count < posValues.size();count++)
			{
				currentNum = previousNum + ((PosMovRot) posValues.get(count)).value;
				if((decide >= previousNum) && (decide < currentNum)) chosen = count;
				previousNum = currentNum;
			}
		
		
			if(debug)
			{
				System.out.println("Soft max Values : ");
				
				for(int count = 0;count < posValues.size();count++)
				{
					System.out.print("\t" + ((PosMovRot) posValues.get(count)).value);
				}
				System.out.println();
			}
			
			return chosen;
	}
	
	public int arbMove(Vector posValues)
	{
		//Arb choice amongst available actions
		//Uncomment to contrast rand vs re-inforced player
		
		return randNo.nextInt(posValues.size());
	}
	
	public Vector multiToSingle(Vector posValues)
	{
		//Update values to handle multi well spanning
		//When dealing with parallel strips the same transition is considered in different wells
		//There is overlap in calculating the values of these transition
		
		//there are three options
		
		//1. average values
		//2. select the biggest
		//3. sum the values
		
		
		//When dealing with conceptual wells and real wells which are the same size
		//the following method lobs out exactly what it takes in
		
		//The averaging method limits the updating validity of the method.
		
		Vector spreadVector = new Vector();
		
		if(debug)
		{
			System.out.println("Values before multiwell adjustment");
			
			for(int count = 0;count < posValues.size();count++)
			{
				System.out.print("\t" + ((PosMovRot) posValues.get(count)).value);
			}
			System.out.println();
		}
		
		while(!posValues.isEmpty())
		{	int found = 1;
			
			int tempRot = ((PosMovRot) posValues.get(0)).rot;
			int tempMov = ((PosMovRot) posValues.get(0)).mov;
			int tempWell = ((PosMovRot) posValues.get(0)).well;
			double accumValue = ((PosMovRot) posValues.get(0)).value;
						
			posValues.removeElementAt(0);
			
			for(int count = 0; count < spreadVector.size();count++)
				if (((PosMovRot) spreadVector.get(count)).mov  == tempMov)
					if (((PosMovRot) spreadVector.get(count)).rot == tempRot)
						{
							//System.out.println("Averaging values across multiwell structure");
							
							//Averaging values across multiwell structure
							//spreadVector.set(count,new PosMovRot((accumValue + found*((PosMovRot) spreadVector.get(count)).value)/++found,0,0, tempMov,tempRot));
							
							//Only keeping the biggest
							spreadVector.set(count,new PosMovRot((accumValue > ((PosMovRot) spreadVector.get(count)).value) ? accumValue : ((PosMovRot) spreadVector.get(count)).value,0,0, tempMov,tempRot));	
							found++;
						}
			
			if(found <= 1) spreadVector.add(new PosMovRot(accumValue,0,tempWell,tempMov,tempRot));
		}
		
		if(debug)
		{
			System.out.println("Values post multi well adjustment");
			
			for(int count = 0;count < spreadVector.size();count++)
			{
				System.out.print("\t" + ((PosMovRot) spreadVector.get(count)).value);
			}
			System.out.println();
		}
		
		//return the new vector to posValues.
		return spreadVector;
	}
	
	public void dumpValues()
	{	//NB THIS ONLY WORKS FOR A CONCEPTUAL WELL OF WIDTH 4
		
		//Bare in mind that the valeus are spread between -MAXHEIGHTDIFF and MAXHEIGHTDIFF
		//NOHEIGHTTERMS diff values
		
		PrintWriter oStream;
		
		if (REDUCEDWIDTH != 4) System.out.println("Sorry, the values can't be logged unless the well is width 4");
		else
		try
		{
			oStream = new PrintWriter(new FileWriter("debugMemory.log"));
		
			for(int count = 0; count < NOHEIGHTTERMS; count++)
			{	
				for(int outer = 0; outer < NOHEIGHTTERMS; outer++)
				{	
					for(int inner = 0; inner < NOHEIGHTTERMS; inner++)
					{	//This is the term that limits it to WIDTH 4
						int tempInt = (int) (count * Math.pow(NOHEIGHTTERMS,0) + outer * Math.pow(NOHEIGHTTERMS,1) + inner * Math.pow(NOHEIGHTTERMS,2));
						oStream.println("" + (count-MAXHEIGHTDIFF) + "\t" + (outer-MAXHEIGHTDIFF) + "\t" + (inner-MAXHEIGHTDIFF) + " : " + V[tempInt]);
						
						
					}
					
				}
			}
			oStream.close();
		}
		catch(IOException err)
		{
			System.err.println("Missing Class");
		}
		
		
	}
	
	public void siesta()
	{	//delay
		try
		{   Thread.sleep(AGENTMOVETIME);
		} 
		catch (InterruptedException e)
		{
		}
	}
	
	synchronized public void place(TetrisGame passedGame)
	{	if (passedGame.isConceptual()) while(!passedGame.move(DOWN));
		else
		{
			
			//The if else clause speeds up block placement
			//as the fleshed out while statement introduced severe delays
				
			if (training || !observablePace) while(!passedGame.move(DOWN));
			else while(!passedGame.move(DOWN))
				{	//This stage freezes under linux when clicking on training with a piece mid flight
					//Works perfectly under windows on 2 machines : same jdk & jvm 1.5.04
					//This problem was circumvented by making siesta() and train() non-synchronized
					gameWindow.postMove();
					siesta();
				}
			
			if (!training) 
			{	//Single screen update following piece placement
				//For observablePace player
				
				gameWindow.setCombined();		
				gameWindow.postMove(); 
			}
			
			deceased = passedGame.isDead();
		}
	}
	
	public void printMind()
	{	//prints out values in value table
		for(int count = 0; count < STATEACTIONSPACE; count++)
			if (V[count] != STARTINGVALUE) System.out.print(" " + V[count]);
		System.out.println();
	}

	public void load(String fileName)
	{	//loads values into value function
		ObjectInputStream oStream;
		System.out.println("reading" + fileName);
		try
		{
			oStream = new ObjectInputStream(new FileInputStream(fileName));
		
			V = (double[]) oStream.readObject();
			oStream.close();
		}
		catch(IOException err)
		{
			System.err.println("IO Exception thrown");
		}
		catch(ClassNotFoundException err)
		{
			System.err.println("Missing Class");
		}
		
	}
	
	public void save(String fileName)
	{	//saves values currently in value function
		ObjectOutputStream oStream;
		System.out.println("writing " + fileName);
		
		try
		{
			oStream = new ObjectOutputStream(new FileOutputStream(fileName));
		
			oStream.writeObject(V);
			oStream.close();
		}
		catch(IOException err)
		{
			System.err.println("Missing Class");
		}
	}
	
	synchronized public void log(int score)
	{	//logs game details
		PrintWriter oStream;
		//System.out.println("writing " + fileName);
		
		try
		{
			oStream = new PrintWriter(new FileWriter((typeGame + typePlayer + "scores.log"),true));
		
		if (noOfGames == 1)
		{
			Date tempDate = new Date();
			oStream.println();
			oStream.println(tempDate.toString());
			oStream.println();
			oStream.println(typeGame);
			oStream.println(typePlayer);
			oStream.println("ALPHA\t" + ALPHA);
			oStream.println("GAMMA\t" + GAMMA);
			oStream.print("Game\t\t" );
			oStream.println("Height" );
			oStream.println();
		}
		oStream.print("" + noOfGames + "\t\t" );
		oStream.println(score);
		oStream.close();
		}
		catch(IOException err)
		{
			System.err.println("Missing Class");
		}
	}
	
	public int rawHash(boolean[] tempBlocks)
	{	//Go through all positions in 2*6 array and add number for each occupied region
		int increment = 1;
		int returnValue = 0;
		
		for(int outer = STARTPOS;outer < (HEIGHT*WIDTH);outer++)
			{
				if (tempBlocks[outer]) returnValue += increment;
				increment*=2;
			}
		return returnValue;
	}
	
	public int mirrorHash(boolean[] tempBlocks)
	{	//Go through all positions in 2*6 array and add number for each occupied region
		int increment = 2048;
		int backValue = 0;
		int forValue = 0;
		
		for(int outer = STARTPOS + WIDTH;outer < (HEIGHT*WIDTH);outer++)
			{
				if (tempBlocks[outer]) forValue += increment;
				increment/=2;
			}
		for(int outer = STARTPOS;outer < STARTPOS + WIDTH;outer++)
			{
				if (tempBlocks[outer]) forValue += increment;
				increment/=2;
			}
		
		increment = 1;
		
		for(int outer = STARTPOS;outer < (HEIGHT*WIDTH);outer++)
			{
				if (tempBlocks[outer]) backValue += increment;
				increment*=2;
			}
		
		return (forValue < backValue) ? forValue : backValue;
	}
	
	synchronized public int adjustHash(int hash,int tetType,int pos, int rot)
	{	
		//System.out.println("Hash : " + hash);
		//System.out.println("Tetromino : " + tetType);
		//System.out.println("Pos : " + pos);
		//System.out.println("Rot : " + rot);
		//System.out.println("Final hash : " + (((hash + MELAXSPACE*tetType) + MELAXSPACE*NUMTETROMINOS*pos) + MELAXSPACE*NUMTETROMINOS*REDUCEDWIDTH*rot));
		//System.out.println();
		
		return (((hash + MELAXSPACE*tetType) + MELAXSPACE*NUMTETROMINOS*pos) + MELAXSPACE*NUMTETROMINOS*REDUCEDWIDTH*rot);
	}
	
	public int[] getHeightWell(boolean[] tempBlocks)
	{	//returns differences in heights between well columns
		
		int height;
		int tempInt;
		int heights[] = new int[REDUCEDWIDTH];
		
		for(int wdth = 0; wdth < REDUCEDWIDTH;wdth++)
		{	height = 0;
			while((height < HEIGHT) && !tempBlocks[height*REDUCEDWIDTH + wdth])
			{
				height++;
			}
			heights[wdth] = HEIGHT - height;
		}
		
		//Calculate differences in height
		
		for(int wdth = 0; wdth < REDUCEDWIDTH-1;wdth++)
		{	
			tempInt = heights[wdth] - heights[wdth+1];
			// There are only REDUCEDWIDTH-1 differences in height
			if(Math.abs(tempInt) > MAXHEIGHTDIFF) tempInt = tempInt/Math.abs(tempInt)*MAXHEIGHTDIFF;
			//Shift every value up by 3, just to facilitate easy hashing
			heights[wdth] = tempInt + MAXHEIGHTDIFF;
		}
		
		return heights;
	}
	
	public int getAverageHeight(int[] tempHeights)
	{	int totalHeight = 0;
		
		for(int wdth = 0; wdth < WIDTH;wdth++)
		{	
			totalHeight += tempHeights[wdth];
		}
		return Math.round(totalHeight/WIDTH);
	}
	
	public void train(int numGames)
	{	//Disables visuals
		//gets agent training
		
		int count = 0;
		
		//Stats
		
		//System.out.println("Checking dead");
		
		if (deceased)
		{
			revive();
		}
		
		//System.out.println("Starting training");
		
		
		//Cant enter a loop with debug information being created
		debug = false;
		active = false;
		training = true;
		observablePace = false;
		interruptTraining = false;
		
		//theGame.restartTetris();
		
		while((count < numGames) && !interruptTraining)
		{	respond();
			
			if (deceased)
			{	
				revive();
				count++;
			}
		}
		
		training = false;
		interruptTraining = false;
		observablePace = true;
		active = true;
	}
	
	public void run()
		{		
			    for (;;)
		        {
		        	if(active && !deceased) 
		        	{	
		        		respond();
		        		//if (observablePace) siesta();
		        	}
	        	}
		}
	
	public class PosMovRot
	{	//Sub class encapsulating the moves, rotations, well position and value of a transition
		public double value;
		public double reward;
		public int mov;
		public int rot;
		public int well;
		
		
		public PosMovRot(double _value,double _reward,int _well,int _mov,int _rot)
		{
			value = _value;
			reward = _reward;
			well = _well;
			mov = _mov;
			rot = _rot;
		}
	}
	
}             	

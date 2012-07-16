//Donald Carr
//Tetris4
//18/6/05
//Coded for Java 1.5

//General Comments
//This is the human interface of the program
//Both human interface and RL-player link up to the tetris game similarly
//We are granted access to interface via gui, ai player granted it via pointer to gui

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.text.*;
import java.awt.Color;
import java.awt.Font;
import java.io.*;

public class TetrisWindow extends JFrame
{	//Constants
	
	//Size of each cube making up wall and tetrominos
	//private final int	SIZE	=	36;  //1024*768
	private final int	SIZE	=	26;  //800*600
	
	//Number of squares to a side for tetrominos		
	private final int 	MAXBLOCKSIZE;
	
	//Main panels
	private JPanel		mainPanel;
	private JPanel		inputPanel;
	private JPanel		infoPanel;
	//Specialised panel classes
	private	TetrisPanel	gamePanel;
	private	TetrominoPanel	currentPanel;
	private	TetrominoPanel	nextPanel;
		
	private JButton breakButton;
	private JButton playerButton;
	private JButton restartButton;
	private JButton trainButton;
	private JButton exploreButton;
	private JButton observablePaceButton;
	private JButton loadButton;
	private JButton saveButton;
	private JButton dumpMind;
	private JButton debugAgentButton;
	private JButton toggleLearningButton;
	
	private JTextField numTrain;
	
	private JLabel exploringTitle;
	private JLabel scoreTitle;
	private JLabel scoreText;
	private JLabel nextTitle;
	private JLabel currentTitle;
	
	private final JFileChooser fc = new JFileChooser();
        
	private boolean	player = true;
	private boolean	isDead = false;
	private boolean	combined = false;
	private boolean	exploring = true;
	private boolean	learning = true;
	
	int score;
	
	//Different variations require changes here and in the tetris games themselves.
	//1. Game type and player type set in TetrisWindow
	//2. Tetromino characteristics set in tetris games
	
	//Choose the appropriate game	
	private	TetrisGame theGame;
	//Choose the appropriate player
	private TetrisPlayer thePlayer;
	
	private final int LEFT = 0;
	private final int RIGHT = 1;
	private final int UP = 2;
	private final int DOWN = 3;
	
	private	TetrisWindow()
	{	
		
		super();
		theGame = new ContourTetris();
		thePlayer = new ContourRLGuru(theGame,this);
		thePlayer.start();
		MAXBLOCKSIZE = theGame.getMaxBlockSize();
		//initialise components
		initialise();
	}
	
	public void updateScore()
	{	
		score = theGame.getScore();
		scoreText.setText(Integer.toString(score));
	}
	
	public void reviveWindow()
	{	//Resets the display for new game
		isDead = false;
	    	updateScore();
	   	currentPanel.repaint();
	    	nextPanel.repaint();
	   	gamePanel.repaint();
	   	if (player) gamePanel.requestFocus();
	}
	
	public void setHuman()
	{	//Sets the display for human interaction 
		
		playerButton.setText("Agent");
	        trainButton.setEnabled(false);
	        numTrain.setEditable(false);
	        
	        gamePanel.addListeners();
	}
	
	public void setAgent()
	{	//Sets the display for human interaction 
		
		playerButton.setText("Human");
	        trainButton.setEnabled(true);
	        numTrain.setEditable(true);
	        
	        gamePanel.removeListeners();
	}
	
	public void setCombined()
	{	
		combined = true;
	}
	
	public void preTrainDisableButton()
	{	//Disables buttons when training
		
		playerButton.setEnabled(false);
		restartButton.setEnabled(false);
		trainButton.setEnabled(false);
		numTrain.setEnabled(false);
		numTrain.setEditable(false);
		loadButton.setEnabled(false);
		saveButton.setEnabled(false);
		exploreButton.setEnabled(false);
		observablePaceButton.setEnabled(false);
		dumpMind.setEnabled(false);
		debugAgentButton.setEnabled(false);
		toggleLearningButton.setEnabled(false);
	}
	
	public void postTrainEnableButton()
	{	//Enables buttons following training
		
		playerButton.setEnabled(true);
		restartButton.setEnabled(true);
		trainButton.setEnabled(true);
		numTrain.setEnabled(true);
		numTrain.setEditable(true);
		loadButton.setEnabled(true);
		saveButton.setEnabled(true);
		exploreButton.setEnabled(true);
		observablePaceButton.setEnabled(true);
		dumpMind.setEnabled(true);
		debugAgentButton.setEnabled(true);
		toggleLearningButton.setEnabled(true);
	}
	
	public void postMove()
	{	//Display update following every move
		
		//Updates display to reflect death/score change
		if (combined)
		{	if (theGame.isDead()) isDead = true;
		       	updateScore();
		      	nextPanel.repaint();
			currentPanel.repaint();
		}
		gamePanel.repaint();
		combined = false;
	}
	
	private void initialise()
	{	//Creates components
		
		breakButton = new JButton("Interrupt training");
		playerButton = new JButton("Agent");
		restartButton = new JButton("Restart");
		trainButton = new JButton("Train");
		exploreButton = new JButton("Explore/Exploit");
		loadButton = new JButton("Load");
		saveButton = new JButton("Save");
		dumpMind = new JButton("Dump Mind");
		observablePaceButton = new JButton("Observable pace");
		debugAgentButton = new JButton("Debug agent");
		toggleLearningButton = new JButton("Cease learning");
		
		numTrain = new JTextField("0");
		
		exploringTitle = new JLabel("exploring");
	        scoreTitle = new JLabel("Score");
	        scoreText = new JLabel("0");
	        currentTitle = new JLabel("Current Block");
	        nextTitle = new JLabel("Next Block");
	        
	        currentPanel = new TetrominoPanel(true);
		nextPanel = new TetrominoPanel(false);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new FlowLayout());
		
		inputPanel = new JPanel();
		
		infoPanel = new JPanel();
		
		gamePanel = new TetrisPanel();
		
	        exploringTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
	        scoreTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
	        scoreText.setAlignmentX(Component.CENTER_ALIGNMENT);
	        currentTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
	        nextTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
	        currentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
	        nextPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
	        	
	        inputPanel.setLayout(new java.awt.GridLayout(20, 1));
	        	
	        inputPanel.add(breakButton);
		inputPanel.add(observablePaceButton);
		inputPanel.add(playerButton);
		inputPanel.add(trainButton);
		inputPanel.add(numTrain);
		inputPanel.add(restartButton);
		inputPanel.add(loadButton);
		inputPanel.add(saveButton);
		inputPanel.add(exploreButton);
		inputPanel.add(toggleLearningButton);
		inputPanel.add(dumpMind);
		inputPanel.add(debugAgentButton);
		
		infoPanel.setLayout(new BoxLayout(infoPanel,BoxLayout.PAGE_AXIS));
        	
		infoPanel.add(exploringTitle);
		infoPanel.add(scoreTitle);
		infoPanel.add(scoreText);
		
		infoPanel.add(currentTitle);
		infoPanel.add(currentPanel);
		infoPanel.add(nextTitle);
		infoPanel.add(nextPanel);
		
		
		//Set Window listener
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		
		trainButton.addActionListener(new ActionListener()
	        {	
	        	public void actionPerformed(ActionEvent evt)
		        {	//Set the agent training for x games
		        	
		         	String num = numTrain.getText();
		                try
		                {
			                    NumberFormat nf = NumberFormat.getInstance();
			                    nf.setParseIntegerOnly(true);
			                    final int x = (nf.parse(num)).intValue();
			                    if (x > 0)
			                    {	//This method uses the Sun SwingWorker class to
			                    	//detach and execute the Agents train method
			                    	//leaving the interface responsive
			                    		
				               		final SwingWorker worker = new SwingWorker()
				             	  	{
								        public Object construct()
								        {	
								        	preTrainDisableButton();
							                    	System.out.println("Training for " + x + " games");
							                    	thePlayer.train(x);
									        return null;
								        }
								        
								        public void finished()
								        {
								        	postTrainEnableButton();
					                    		}
				             	  	};
						    	worker.start(); 
			                    }
		                }	
		                catch (ParseException pe)
		                {
		                	System.out.println("Error parsing Integer!!");
		                }
		        }
	        });
	        
        exploreButton.addActionListener(new ActionListener()
	        {   //Toggles the agents desire to explore
	            public void actionPerformed(ActionEvent evt)
	            {	
	            	exploring = !exploring;
	            	thePlayer.toggleExploring();
	            	if (exploring) exploringTitle.setText("Exploring");
	            	else exploringTitle.setText("Exploiting");
	            }
	        });
	        
        toggleLearningButton.addActionListener(new ActionListener()
	        {   //Toggles the agents desire to learn
	            public void actionPerformed(ActionEvent evt)
	            {	
	            	learning = !learning;
	            	thePlayer.toggleLearning();
	            	if (learning) toggleLearningButton.setText("Cease learning");
	            	else toggleLearningButton.setText("Learn");
	            }
	        });
	        
        breakButton.addActionListener(new ActionListener()
	        {	//Ceases training
	            	public void actionPerformed(ActionEvent evt)
	            	{	
	            		thePlayer.interruptTraining();
	            	}
	        });
	        
        debugAgentButton.addActionListener(new ActionListener()
	        {   //Request verbose output on actions
	            public void actionPerformed(ActionEvent evt)
	            {	
	            	thePlayer.toggleDebug();
	            }
	        });
	        
        dumpMind.addActionListener(new ActionListener()
	        {   //Drop value function into text file and display it on screen 
	            public void actionPerformed(ActionEvent evt)
	            {
	            	thePlayer.printMind();
	            	thePlayer.dumpValues();
	            }
	        });
	        
        observablePaceButton.addActionListener(new ActionListener()
	        {
	            public void actionPerformed(ActionEvent evt)
	            {	//Slows/Speeds the player down/up
	            	thePlayer.toggleObservablePace();
	            }
	        });
	        
        playerButton.addActionListener(new ActionListener()
	        {   //Switch between inputs
	            public void actionPerformed(ActionEvent evt)
	            {	
	            	 thePlayer.toggleActive();
	                 player = !player;
	                 if(player)
	                 {
	                 	setHuman();
	                 }
	                 else
	                 {
	                 	setAgent();
	                 }
	            }
	            
	        });
	        
        restartButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {	
            	//Player must be revived first as he grabs score off game
                thePlayer.revive();
                reviveWindow();
            }
        });
        
        loadButton.addActionListener(new ActionListener()
        {   //Load the player's value function
            public void actionPerformed(ActionEvent evt)
            {
            	int returnVal = fc.showOpenDialog(TetrisWindow.this);

	            if (returnVal == JFileChooser.APPROVE_OPTION)
	            {
	                File infile = fc.getSelectedFile();
	                thePlayer.load(infile.toString());
	            }
            }
        });
        
        saveButton.addActionListener(new ActionListener()
        {   //Save the player's value function
            
            public void actionPerformed(ActionEvent evt)
            {	
            	int returnVal = fc.showSaveDialog(TetrisWindow.this);

	            if (returnVal == JFileChooser.APPROVE_OPTION)
	            {
	                File outfile = fc.getSelectedFile();
	                thePlayer.save(outfile.toString());
	            }
            }
        });
        
        mainPanel.add(inputPanel);
        mainPanel.add(gamePanel);
        mainPanel.add(infoPanel);
	
        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);
        
        setHuman();
        pack();
        gamePanel.initialise();
		
	}
	
	private	class TetrisPanel extends JPanel
	{	//The main tetris game panel : the visible tetris game
		
		//Constants
		
		private final int	WIDTH;
		private final int	HEIGHT;
		
		//variables
		
		private int	blockPosY	=	0;
		private int	blockPosX	=	5;
		
		//The following are both pointers to the corresponding objects in the tetris game
		
		private Tetromino currentTetromino;
		private boolean[] blocks;
		
		private GameMouse theMouse = new GameMouse();
		private GameKeys theKeys = new GameKeys();
		
		private TetrisPanel()
		{	
			super();
			WIDTH = theGame.getWidth();
			HEIGHT = theGame.getHeight();
			
			initialise();
			
			//returns pointer
	          	currentTetromino = theGame.getCurrentTetromino();
	            	blocks = theGame.getBlocks();
		}
		
		private void initialise()
		{
			setOpaque(true);
			
			//game dictates dimensions
						
			Dimension size = new Dimension(WIDTH*SIZE, HEIGHT*SIZE);
		        setPreferredSize(size);
		        setBorder(new javax.swing.border.LineBorder(Color.WHITE));
        	}
         	
         	//Adds keyboard & mouse listeners to game
         	
	     	protected void addListeners()
		{		//Yield control to user
				requestFocus();
				addMouseListener(theMouse);
	        		addKeyListener(theKeys);
	        }
	     	
	     	//Removes keyboard & mouse listeners from game
	     	//This is done in order to deactivate concurrent threads
	     	//with access to the tetris game
	     	 
	     	protected void removeListeners()
		{	//Remove control from user
				
			removeMouseListener(theMouse);
	       		removeKeyListener(theKeys);
	       	}
     	
     		public void drawTetromino(Graphics tetrisScreen)
		{	//Draw active tetromino (piece)
			 
			//This pointer needs to be refreshed since new objects are being handed back
			currentTetromino = theGame.getCurrentTetromino();
			
			blockPosX = theGame.getXPos();
			blockPosY = theGame.getYPos();
		
			for(int hght = 0;hght < MAXBLOCKSIZE;hght++)
				if ((blockPosY + hght - MAXBLOCKSIZE)>=0)
				{
					for(int wdth = 0;wdth < MAXBLOCKSIZE;wdth++)
					{	
						if (currentTetromino.tetArray[hght*MAXBLOCKSIZE + wdth])
						{	tetrisScreen.setColor(currentTetromino.tetColor);
							tetrisScreen.fillRect((blockPosX + wdth)*SIZE,(blockPosY + hght - MAXBLOCKSIZE)*SIZE , SIZE, SIZE);
							tetrisScreen.setColor(Color.black);
	         					tetrisScreen.drawRect((blockPosX + wdth)*SIZE,(blockPosY + hght - MAXBLOCKSIZE)*SIZE , SIZE, SIZE);
						}
					}
				}
			
		}
		
		//Draws existing Tetromino structure
				
		public void draw(Graphics tetrisScreen)
		{	//The blocks object is created once so there is no need to refresh the pointer
			
			for(int hght = 0;hght < HEIGHT;hght++)
				for(int wdth = 0;wdth < WIDTH;wdth++)
				{	
					if (blocks[hght*WIDTH + wdth])
					{	tetrisScreen.setColor(Color.blue);
		        			tetrisScreen.fillRect(wdth*SIZE,hght*SIZE , SIZE, SIZE);
						tetrisScreen.setColor(Color.black);
		         			tetrisScreen.drawRect(wdth*SIZE,hght*SIZE , SIZE, SIZE);
					}
					else
					{
						tetrisScreen.setColor(Color.black);
		         			tetrisScreen.fillRect(wdth*SIZE,hght*SIZE , SIZE, SIZE);
					}
				}
		}
		
		public void paintComponent(Graphics page)
	        {		//updates Tetris Window
	         	super.paintComponent(page);
	         	
	         	if (isDead) drawDead(page);
	         	else
	         	{
	         		draw(page);
	         		drawTetromino(page);
	         	}
	         		
	        }
         	
	     	private void drawDead(Graphics tetrisScreen)
	     	{	//Draws deceased message
	     		
	     		tetrisScreen.setColor(Color.black);
			tetrisScreen.fillRect(0,0 , WIDTH*SIZE, HEIGHT*SIZE);
				
			tetrisScreen.setColor(Color.red);
			tetrisScreen.setFont(new Font("SansSerif",0,WIDTH*5));
			tetrisScreen.drawString("Deceased",WIDTH,HEIGHT*5);
	     	}
	}
	
	private	class TetrominoPanel extends JPanel
	{	//Draws current/next tetromino in side block
		
		//Constants
		
		private final int	MAXBLOCKSIZE	=	theGame.getMaxBlockSize();
		private final boolean current;
		
		//variables
		
		private Tetromino tetromino;
		
		private TetrominoPanel(boolean current)
		{
			super();
			initialise();
			this.current = current;
		}
		
		private void initialise()
		{
			setOpaque(true);
			Dimension size = new Dimension(MAXBLOCKSIZE*SIZE, MAXBLOCKSIZE*SIZE);
		        setPreferredSize(size);
		        setBorder(new javax.swing.border.LineBorder(Color.WHITE));
		}
		
		
		public void paintComponent(Graphics page)
	        {	//updates Tetris Window
			super.paintComponent(page);
	         	drawTetromino(page);
	        }
		
		public void drawTetromino(Graphics tetrisScreen)
		{	//This pointer needs to be constantly updated, as new tetrominos are created
			//ie cant rely on static presence of associated tetromino
			
			if (current) tetromino = theGame.getCurrentTetromino();
			else tetromino = theGame.getNextTetromino();
						
			tetrisScreen.setColor(Color.black);
			tetrisScreen.fillRect(0,0 , MAXBLOCKSIZE*SIZE, MAXBLOCKSIZE*SIZE);
			
			for(int hght = 0;hght < MAXBLOCKSIZE;hght++)
				for(int wdth = 0;wdth < MAXBLOCKSIZE;wdth++)
					{	
						if (tetromino.tetArray[hght*MAXBLOCKSIZE + wdth])
						{	
							tetrisScreen.setColor(tetromino.tetColor);
							tetrisScreen.fillRect(wdth*SIZE,hght*SIZE , SIZE, SIZE);
							tetrisScreen.setColor(Color.black);
	         					tetrisScreen.drawRect(wdth*SIZE,hght*SIZE , SIZE, SIZE);
						}
					}
		}
	}
	
	public class GameMouse implements MouseListener
	{
		public void mouseClicked(MouseEvent e)
		{
			gamePanel.requestFocus();
		}
		
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e){}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
	}
	
	private class GameKeys implements KeyListener
	{	//This listener is removed when the ai player is handed control
		//the boolean condition below was !isDead && player
		//Completely removed listener in order to minimise
		//simultaneous threads
		
	        public void keyPressed (KeyEvent event)
		{	if(!isDead)	
			{           
				    switch (event.getKeyCode())
			        {        
			                  case KeyEvent.VK_LEFT:
			                     theGame.move(LEFT);
			                     break;
			                  case KeyEvent.VK_RIGHT:
			                     theGame.move(RIGHT); 
			                     break;
			                  case KeyEvent.VK_DOWN:
			                     combined = theGame.move(DOWN);
			                     break;
			                  case KeyEvent.VK_SHIFT:
			                  //Clockwise rotation
			                     theGame.rotate(true);
			                     break;
			                  case KeyEvent.VK_CONTROL:
			                     theGame.rotate(false);      
			                     break;
			                
			        }
			        postMove();
			}
		        
		        			
		}
		
		public void keyTyped (KeyEvent event) {}
		public void keyReleased (KeyEvent event) {}
	       	
	} 
      	
   //Thread safe main method
	
	public static void main(String[] args)
	{
	        javax.swing.SwingUtilities.invokeLater
	        (new Runnable()
		        {
		            public void run()
		            {
		            	new TetrisWindow().setVisible(true);
		            }
		        }
	        );
	}
}

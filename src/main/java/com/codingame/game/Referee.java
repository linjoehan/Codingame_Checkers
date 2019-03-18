package com.codingame.game;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.MultiplayerGameManager;
//import com.codingame.gameengine.module.entities.Circle;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Sprite;
import com.google.inject.Inject;
import java.util.ArrayList;

public class Referee extends AbstractReferee {
    // Uncomment the line below and comment the line under it to create a Solo Game
    // @Inject private SoloGameManager<Player> gameManager;
    @Inject private MultiplayerGameManager<Player> gameManager;
    @Inject private GraphicEntityModule graphicEntityModule;
    
    private char[][] board = new char[8][8];
    private int gameturn;
    
    //maximum number of turns allowed (excluding inserted frames)
    private int GAMETURN_LIMIT = 300;
    
    //private int PIECE_RADIUS = 35;
    private int BOARD_X = 582 - 45;
    private int BOARD_Y = 189 - 45;
    private int BOARD_DX = 102;
    private int BOARD_DY = 102;
    
    private Sprite[] boardpiece = new Sprite[24];
    private Sprite[] boardmask = new Sprite[24];
    private Sprite[] player_avatar = new Sprite[2];
    
    private String move_buffer;
    private int move_buffer_index;
    
    private ArrayList<String> getMoves()
    {
    	ArrayList<String> final_move_list = new ArrayList<String>();
    	ArrayList<String> j_move = new ArrayList<String>();
    	ArrayList<Integer> j_move_status = new ArrayList<Integer>();
        
        //find all pieces with potential jumps
        for(int row = 0;row<8;row++)
        {
            for(int col = 0;col<8;col++)
            {
            	boolean p1turn = (gameturn%2 == 0);
                
                if(
                (board[row][col] == 'b' && p1turn) ||
                (board[row][col] == 'B' && p1turn) ||
                (board[row][col] == 'r' && !p1turn) ||
                (board[row][col] == 'R' && !p1turn)
                )
                {
                    String square = "";
                    square = "" + (char)(col + 'a');
                    square = square + (char)(8 - row + '0');
                    
                    j_move.add(square);
                    j_move_status.add(0);
                }
            }
        }
        
        //add to potential jumps that are not complete
        boolean all_done = false;
        while(!all_done)
        {
        	all_done = true;
        	for(int i = 0;i<j_move.size();i++)
        	{
        		if(j_move_status.get(i) == 0)
        		{
        			for(int d = 0;d<4;d++)
        			{
        				String newmove = add_jump(j_move.get(i),d);
        				if(newmove.length() != j_move.get(i).length())
        				{
        					j_move_status.set(i, 2); //not a legal move anymore
        					j_move.add(newmove);
        					j_move_status.add(0);
        				}
        			}
        			if(j_move_status.get(i) == 0)
        			{
        				j_move_status.set(i, 1);
        			}
        		}
        	}
        	
        	for(int i = 0;i<j_move.size();i++)
        	{
        		if(j_move_status.get(i) == 0)
        		{
        			all_done = false;
        		}
        	}
        }
        
        //add legal jump moves to final list
        for(int i = 0; i<j_move.size();i++)
        {
        	if(j_move.get(i).length() > 2 && j_move_status.get(i) == 1)
        	{
        		final_move_list.add(j_move.get(i));
        	}
        }
        
        //add normal moves if there are no jumps
        if(final_move_list.size() == 0)
        {
        	for(int row = 0;row<8;row++)
        	{
        		for(int col = 0;col<8;col++)
        		{
        			boolean p1turn = (gameturn%2 == 0);
        			
        			if(
        	        (board[row][col] == 'b' && p1turn) ||
        	        (board[row][col] == 'B' && p1turn) ||
        	        (board[row][col] == 'r' && !p1turn) ||
        	        (board[row][col] == 'R' && !p1turn)
        	        )
        			{
        				//up right move
        				if(row > 0 && col < 7 && board[row][col] != 'r')
        				{
        					if(board[row-1][col+1] == '.')
        					{
        						String move_string = "";
        						move_string = move_string + (char)(col+'a');
        						move_string = move_string + (char)(8 - row + '0');
        						move_string = move_string + (char)((col+1) + 'a');
        						move_string = move_string + (char)(8 - (row-1) + '0');
        						final_move_list.add(move_string);
        					}
        				}
        				
        				//up left move
        				if(row > 0 && col > 0 && board[row][col] != 'r')
        				{
        					if(board[row-1][col-1] == '.')
        					{
        						String move_string = "";
        						move_string = move_string + (char)(col+'a');
        						move_string = move_string + (char)(8 - row + '0');
        						move_string = move_string + (char)((col-1) + 'a');
        						move_string = move_string + (char)(8 - (row-1) + '0');
        						final_move_list.add(move_string);
        					}
        				}
        				
        				//down right move
        				if(row < 7 && col < 7 && board[row][col] != 'b')
        				{
        					if(board[row+1][col+1] == '.')
        					{
        						String move_string = "";
        						move_string = move_string + (char)(col+'a');
        						move_string = move_string + (char)(8 - row + '0');
        						move_string = move_string + (char)((col+1) + 'a');
        						move_string = move_string + (char)(8 - (row+1) + '0');
        						final_move_list.add(move_string);
        					}
        				}
        				
        				//down left move
        				if(row < 7 && col > 0 && board[row][col] != 'b')
        				{
        					if(board[row+1][col-1] == '.')
        					{
        						String move_string = "";
        						move_string = move_string + (char)(col+'a');
        						move_string = move_string + (char)(8 - row + '0');
        						move_string = move_string + (char)((col-1) + 'a');
        						move_string = move_string + (char)(8 - (row+1) + '0');
        						final_move_list.add(move_string);
        					}
        				}
        			}
        		}
        	}
        }
        
        return final_move_list;
    }
    
    private String add_jump(String inputmove,int dir)
    {
    	String move = inputmove;
    	int first_row = 8 - move.charAt(1) + '0';
    	int first_col = move.charAt(0) - 'a';
    	
    	char piece = board[first_row][first_col];
    	
    	//board to mark previous jumps
    	boolean cap[][] = new boolean[8][8];
    	for(int row = 0;row<8;row++)
    	{
    		for(int col = 0;col<8;col++)
    		{
    			cap[row][col] = false;
    		}
    	}
    	
    	//mark completed captures
    	int current_row = first_row;
    	int current_col = first_col;
    	int string_pos = 2;
    	while(move.length() > string_pos)
    	{
    		int next_row = 8 - move.charAt(string_pos + 1) + '0';
    		int next_col = move.charAt(string_pos) - 'a';
    		cap[(current_row + next_row) / 2][(current_col + next_col) / 2] = true;
    		current_row = next_row;
            current_col = next_col;
            string_pos += 2;
    	}
    	
    	//NOTE: CLEAN THIS UP
    	//add in next capture if possible
    	if(dir == 0 && current_row > 1 && current_col < 6 && piece != 'r') //up right capture
    	{
    		//white captures black
    		if( (piece == 'r' || piece == 'R') &&
    		    (board[current_row - 1][current_col+1] == 'b' || board[current_row - 1][current_col+1] == 'B') &&
    		    (board[current_row-2][current_col+2] == '.' || (current_row-2 == first_row && current_col+2 == first_col)) &&
    		    (cap[current_row-1][current_col+1] == false)
    		)
    		{
    			move = move + (char)((current_col + 2)+'a');
                move = move + (char)(8 - (current_row - 2) + '0');
    		}
    		
    		//black captures white
    		if( (piece == 'b' || piece == 'B') &&
        	    (board[current_row - 1][current_col+1] == 'r' || board[current_row - 1][current_col+1] == 'R') &&
        	    (board[current_row-2][current_col+2] == '.' || (current_row-2 == first_row && current_col+2 == first_col)) &&
        	    (cap[current_row-1][current_col+1] == false)
        	)
        	{
    			move = move + (char)((current_col + 2)+'a');
    			move = move + (char)(8 - (current_row - 2) + '0');
        	}
    	}
    	
    	if(dir == 1 && current_row > 1 && current_col > 1 && piece != 'r') //up left capture
    	{
    		//white captures black
    		if( (piece == 'r' || piece == 'R') &&
    		    (board[current_row - 1][current_col-1] == 'b' || board[current_row - 1][current_col-1] == 'B') &&
    		    (board[current_row-2][current_col-2] == '.' || (current_row-2 == first_row && current_col-2 == first_col)) &&
    		    (cap[current_row-1][current_col-1] == false)
    		)
    		{
    			move = move + (char)((current_col - 2)+'a');
                move = move + (char)(8 - (current_row - 2) + '0');
    		}
    		
    		//black captures white
    		if( (piece == 'b' || piece == 'B') &&
        	    (board[current_row - 1][current_col-1] == 'r' || board[current_row - 1][current_col-1] == 'R') &&
        	    (board[current_row-2][current_col-2] == '.' || (current_row-2 == first_row && current_col-2 == first_col)) &&
        	    (cap[current_row-1][current_col-1] == false)
        	)
        	{
    			move = move + (char)((current_col - 2)+'a');
    			move = move + (char)(8 - (current_row - 2) + '0');
        	}
    	}
    	
    	if(dir == 2 && current_row < 6 && current_col > 1 && piece != 'b') //down left capture
    	{
    		//white captures black
    		if( (piece == 'r' || piece == 'R') &&
    		    (board[current_row + 1][current_col-1] == 'b' || board[current_row + 1][current_col-1] == 'B') &&
    		    (board[current_row+2][current_col-2] == '.' || (current_row+2 == first_row && current_col-2 == first_col)) &&
    		    (cap[current_row+1][current_col-1] == false)
    		)
    		{
    			move = move + (char)((current_col - 2)+'a');
                move = move + (char)(8 - (current_row + 2) + '0');
    		}
    		
    		//black captures white
    		if( (piece == 'b' || piece == 'B') &&
        	    (board[current_row + 1][current_col-1] == 'r' || board[current_row + 1][current_col-1] == 'R') &&
        	    (board[current_row+2][current_col-2] == '.' || (current_row+2 == first_row && current_col-2 == first_col)) &&
        	    (cap[current_row+1][current_col-1] == false)
        	)
        	{
    			move = move + (char)((current_col - 2)+'a');
    			move = move + (char)(8 - (current_row + 2) + '0');
        	}
    	}
    	
    	if(dir == 3 && current_row < 6 && current_col < 6 && piece != 'b') //down right capture
    	{
    		//white captures black
    		if( (piece == 'r' || piece == 'R') &&
    		    (board[current_row + 1][current_col+1] == 'b' || board[current_row + 1][current_col+1] == 'B') &&
    		    (board[current_row+2][current_col+2] == '.' || (current_row+2 == first_row && current_col+2 == first_col)) &&
    		    (cap[current_row+1][current_col+1] == false)
    		)
    		{
    			move = move + (char)((current_col + 2)+'a');
                move = move + (char)(8 - (current_row + 2) + '0');
    		}
    		
    		//black captures white
    		if( (piece == 'b' || piece == 'B') &&
        	    (board[current_row + 1][current_col+1] == 'r' || board[current_row + 1][current_col+1] == 'R') &&
        	    (board[current_row+2][current_col+2] == '.' || (current_row+2 == first_row && current_col+2 == first_col)) &&
        	    (cap[current_row+1][current_col+1] == false)
        	)
        	{
    			move = move + (char)((current_col + 2)+'a');
    			move = move + (char)(8 - (current_row + 2) + '0');
        	}
    	}
    	
    	return move;
    }
    
    private void sendPlayerInputs(Player player) 
    {
        for(int row = 0;row<8;row++)
        {
            String str = "";
            for(int col = 0;col<8;col++)
            {
                str = str + board[row][col];
            }
            player.sendInputLine(str);
        }
        
        if(gameturn%2==0)
        {
        	player.sendInputLine("b");
        }
        else
        {
        	player.sendInputLine("w");
        }
        
        ArrayList<String> possible_moves = getMoves();
        int N = possible_moves.size();
        
        player.sendInputLine(String.valueOf(N));
        
        for(int i = 0;i<N;i++)
        {
        	player.sendInputLine(possible_moves.get(i));
        }
    }
    
    private void make_move(String move) //makes move on board without checking
    {
    	//convert string into coordinates
    	int r = 8 - (move.charAt(1) - '0');
    	int c = move.charAt(0) - 'a';
    	//pick up piece
    	char piece = board[r][c];
    	board[r][c] = '.';
    	
    	//find index of piece
    	int piece_index = -1;
    	for(int i = 0;i<24;i++)
    	{
    		if(boardpiece[i].getX() == BOARD_X + c*BOARD_DX && boardpiece[i].getY() == BOARD_Y + r*BOARD_DY)
    		{
    			piece_index = i;
    		}
    	}
    	
    	int string_pos = 2;
    	while(move.length() > string_pos)
    	{
    		//translate next coordinate
    		int next_r = 8 - (move.charAt(string_pos + 1) - '0');
    		int next_c = move.charAt(string_pos) - 'a';
    		
    		//move boardpiece
    		boardmask[piece_index]
    				.setX(BOARD_X + next_c*BOARD_DX)
    				.setY(BOARD_Y + next_r*BOARD_DY);
    		boardpiece[piece_index]
    				.setX(BOARD_X + next_c*BOARD_DX)
    				.setY(BOARD_Y + next_r*BOARD_DY);
    		
    		//remove piece from the middle of the squares (If jumping or just the any of the original if not)
    		board[(r+next_r)/2][(c+next_c)/2] = '.';
    		
    		//check if piece must be removed
    		if( (r+next_r)/2 != r && (r+next_r)/2 != next_r)
    		{
    			int remove_piece_index = -1;
    			for(int i = 0;i<24;i++)
    			{
    				if(boardpiece[i].getX() == BOARD_X + ((c+next_c)/2)*BOARD_DX && boardpiece[i].getY() == BOARD_Y + ((r+next_r)/2)*BOARD_DY)
    	    		{
    					remove_piece_index = i;
    	    		}
    			}
    			
    			//remove piece
    			int offx = 1500 + (int)(Math.random()*350);
    			int offy = 150 + (int)(Math.random()*700);
    			
    			boardpiece[remove_piece_index]
    					.setX(offx)
        				.setY(offy);
    			boardmask[remove_piece_index]
    					.setX(offx)
        				.setY(offy);
    		}
    		
            //update new r
            r = next_r;
            c = next_c;
            
            //change to king if I can
    		if(piece == 'b' && next_r == 0)
            {
                piece = 'B';
                boardpiece[piece_index]
                		.setImage("black_king.png");
            }
            if(piece == 'r' && next_r == 7)
            {
                piece = 'R';
                boardpiece[piece_index]
                		.setImage("red_king.png");
            }
            
            string_pos = string_pos + 2;
    	}
    	
    	//put down piece on board
        board[r][c] = piece;
    }
    
    @Override
    public void init() 
    {
    	gameManager.setMaxTurns(GAMETURN_LIMIT + 100);
    			
    	move_buffer = "";
    	move_buffer_index = 0;
    	
    	//background
        graphicEntityModule.createSprite().setImage("background.jpg").setAnchor(0);
        
        //player avatar 0
        int player_num = 0;
        int avatarx = 100;
        int avatary = 720;
        
        graphicEntityModule.createSprite()
        		.setImage(gameManager.getPlayer(player_num).getAvatarToken())
        		.setX(avatarx+20)
        		.setY(avatary)
        		.setBaseWidth(200)
        		.setBaseHeight(200)
        		.setAnchor(0);
        graphicEntityModule.createText(gameManager.getPlayer(player_num).getNicknameToken())
                .setFontSize(50)
                .setStrokeThickness(2)
                .setX(avatarx+92)
                .setY(avatary+220)
                .setAnchor(0);
        
        graphicEntityModule.createSprite()
				.setImage("black_piece.png")
				.setMask(graphicEntityModule.createSprite()
						.setImage("mask.png")
						.setX(avatarx-20)
						.setY(avatary+210)
						)
				.setX(avatarx-20)
				.setY(avatary+210)
				.setAnchor(0);
        
        //player avatar 1
        player_num = 1;
        avatarx = 100;
        avatary = 50;
        
        graphicEntityModule.createSprite()
			.setImage(gameManager.getPlayer(player_num).getAvatarToken())
			.setX(avatarx+20)
			.setY(avatary)
			.setBaseWidth(200)
			.setBaseHeight(200)
			.setAnchor(0);
        graphicEntityModule.createText(gameManager.getPlayer(player_num).getNicknameToken())
        	.setFontSize(50)
        	.setStrokeThickness(2)
        	.setX(avatarx+92)
        	.setY(avatary+220)
        	.setAnchor(0);

        graphicEntityModule.createSprite()
			.setImage("red_piece.png")
			.setMask(graphicEntityModule.createSprite()
					.setImage("mask.png")
					.setX(avatarx-20)
					.setY(avatary+210)
					)
			.setX(avatarx-20)
			.setY(avatary+210)
			.setAnchor(0);
        
      //fill board with starting locations
        int next_piece_index = 0;
        
        for(int row = 0;row<8;row++)
        {
            for(int col = 0;col<8;col++)
            {
                if( (row+col) % 2 == 1 && row <= 2)
                {
                    board[row][col] = 'r';
                    boardmask[next_piece_index] = graphicEntityModule.createSprite()
                    		.setImage("mask.png")
                    		.setX(BOARD_X + col*BOARD_DX)
                    		.setY(BOARD_Y + row*BOARD_DY);
                    boardpiece[next_piece_index] = graphicEntityModule.createSprite()
                    		.setImage("red_piece.png")
                    		.setMask(boardmask[next_piece_index])
                    		.setX(BOARD_X + col*BOARD_DX)
                    		.setY(BOARD_Y + row*BOARD_DY);
                    next_piece_index++;
                    		
                }
                else if((row+col) % 2 == 1 && row >= 5)
                {
                    board[row][col] = 'b';
                    boardmask[next_piece_index] = graphicEntityModule.createSprite()
                    		.setImage("mask.png")
                    		.setX(BOARD_X + col*BOARD_DX)
                    		.setY(BOARD_Y + row*BOARD_DY);
                    boardpiece[next_piece_index] = graphicEntityModule.createSprite()
                    		.setImage("black_piece.png")
                    		.setMask(boardmask[next_piece_index])
                    		.setX(BOARD_X + col*BOARD_DX)
                    		.setY(BOARD_Y + row*BOARD_DY);
                    next_piece_index++;
                    
                }
                else
                {
                    board[row][col] = '.';
                }
            }
        }
        
        
        
        gameturn = 0;
        
        
        
    }

    @Override
    public void gameTurn(int turn)
    {
        // Code your game logic.
        // See README.md if you want some code to bootstrap your project.
        
    	//insert frame for multijump
    	if(move_buffer.length() >= move_buffer_index + 4)
    	{
    		
    		make_move(move_buffer.substring(move_buffer_index,move_buffer_index+4));
    		move_buffer_index = move_buffer_index + 2;
    		
    		if(move_buffer.length() == move_buffer_index + 2) //only if move is complete
            {
        		//check winner(move list is empty then plyer loses)
                ArrayList<String> next_turn_moves = getMoves();
                if(next_turn_moves.size() == 0)
                {
                	Player next_player = gameManager.getPlayer(gameturn % 2);
                	next_player.deactivate("No moves left for next player");
                	gameManager.endGame();
                }
                
                //check draw condition
                if(gameturn == GAMETURN_LIMIT)
                {
                	gameManager.endGame();
                }
            }
    		
    		//skip player from requiring outputs for frame
    		Player player = gameManager.getPlayer((gameturn-1) % 2);
    		player.setExpectedOutputLines(0);
    		
    		player.execute();
    		player.setExpectedOutputLines(1);
    	}
    	else
    	{
	    	Player player = gameManager.getPlayer(gameturn % 2);
	        //send player inputs
	        sendPlayerInputs(player);
	        player.execute();
	        
	        
	        try 
	        {
	        	//get input from player
	        	String player_move = player.getOutputs().get(0);
	        	
	        	//check that move is legal
	        	ArrayList<String> possible_moves = getMoves();
	        	boolean is_legal_move = false;
	        	for(int i = 0;i<possible_moves.size();i++)
	        	{
	        		if(player_move.equals(possible_moves.get(i)))
	        		{
	        			is_legal_move = true;
	        		}
	        	}
	        	
	        	if(is_legal_move)
	        	{
	        		move_buffer = player_move;
	        		move_buffer_index = 0;
	        		
	        		//apply move to board
	        		make_move(move_buffer.substring(move_buffer_index,move_buffer_index+4));
	        		move_buffer_index = move_buffer_index + 2;
	        		
	        		//increment gameturn
	                gameturn++;
	                
	                if(move_buffer.length() == move_buffer_index + 2) //only if move is complete
	                {
		        		//check winner(move list is empty then player loses)
		                ArrayList<String> next_turn_moves = getMoves();
		                if(next_turn_moves.size() == 0)
		                {
		                	Player next_player = gameManager.getPlayer(gameturn % 2);
		                	next_player.deactivate("No moves left for next player");
		                	gameManager.endGame();
		                }
		                
		                //check draw condition
		                if(gameturn == GAMETURN_LIMIT)
		                {
		                	gameManager.endGame();
		                }
	                }
	                
	        	}
	        	else
	        	{
	        		player.deactivate("Move:" + player_move + " is not a legal move");
	        		gameManager.endGame();
	        	}
	        	
	        }
	        catch (TimeoutException e)
	        {
	        	player.deactivate("Time limit exceeded!");
	        	gameManager.endGame();
	        }
    	}
    }
    
    @Override
    public void onEnd() 
    {
        for (Player p : gameManager.getPlayers()) {
            p.setScore(p.isActive() ? 1 : 0);
        }
    }
}

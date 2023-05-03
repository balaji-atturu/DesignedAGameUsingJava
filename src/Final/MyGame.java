package Final;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
//Main Assignment
class Bricks {
	
	public int arr [][];
	public int brickWidth;
	public int brickHeight;
	
	private Map<String, BufferedImage> images ;
	// this creates the brick of size 3x7
	public Bricks(int row, int col) {
		images=new HashMap<>();
		try {
			images.put("brick2.png", ImageIO.read(new File("Resources/darkbrown.png")));
			images.put("brick6.png", ImageIO.read(new File("Resources/Brick6.png")));
			images.put("brick7.png", ImageIO.read(new File("Resources/Brick7.png")));
		} catch (Exception e) {
			
			System.out.println(e);
		}
	
		arr = new int [row][col];
		for (int i = 0; i < arr.length; i++) { 
			for (int j=0; j< arr[0].length;j++) {
				   arr[i][j] = 3;
			}
		}
		
		
	}
	
	
	public void draw(Graphics2D g) {
		brickWidth = 30;
		brickHeight = 10;
		for (int i = 0; i < arr.length; i++) {
			for (int j=0; j<arr[0].length;j++) {
				if(arr[i][j] > 0) {
					int x = j * brickWidth+110;
	                int y = i * brickHeight+20;
	                if(arr[i][j]==1) {
	                	g.drawImage(images.get("brick7.png"), x, y, 30, 10, null);}
	                else if(arr[i][j]==2) {
	                
	                	g.drawImage(images.get("brick6.png"), x, y, 30, 10, null);
	                }
	                else if(arr[i][j]==3) {
	                	g.drawImage(images.get("brick2.png"), x, y, 30, 10, null);
	                }
	                
	                g.setColor(Color.white);
	                g.drawRect(x, y, brickWidth, brickHeight);
				}
			}
			
		}
	}
	
	
	public void setBrickValue(int value, int row, int col) {
		arr[row][col] = value;
	}

}

class GamePlay extends JPanel implements KeyListener,ActionListener  {
	private boolean play = true;
	private int score = 0;
	
	private int totalBricks=20*14*3;
	
	private Timer timer;
	private int delay =8;
	
	private int playerX = 310;
	private int playerY=550;
	private int ballposX = 120;
	private int ballposY = 350;
	private int ballXdir = -1;
	private int ballYdir = -2;

	
	private Bricks map;
		
	private int birdX=0;
	private int birdY=400;
	private int birdCount=1;
	
	private Map<String, BufferedImage> pics;
	
	//Database
	private Connection con;
	
	public GamePlay() {
		addKeyListener(this);
		map = new Bricks(20, 14);
		
		setFocusable(true);
	    pics=new HashMap<>();
	    try {
			pics.put("bar.png", ImageIO.read(new File("Resources/bloodbar.png")));
			pics.put("background4.png", ImageIO.read(new File("Resources/background4.png")));
			pics.put("bird1", ImageIO.read(new File("Resources/Frame-1.png")));
			pics.put("bird2", ImageIO.read(new File("Resources/frame-2.png")));
			pics.put("bird3", ImageIO.read(new File("Resources/frame-3.png")));
			pics.put("bird4", ImageIO.read(new File("Resources/frame-4.png")));
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		setFocusTraversalKeysEnabled(false);
		
		//
		try{
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/calendar","root","Balaji@3456");
			}
			catch(Exception e) {
				System.out.println(e.getMessage());
			}
		
		//
		
		timer = new Timer(delay, this);
		timer.start();
	}

	public void paint(Graphics g) {
		
		
	    g.drawImage(pics.get("background4.png"), 0, 0, 700, 600, null);
		map.draw((Graphics2D)g);
		
		g.setColor(Color.black);
		g.fillRect(0, 0, 3, 600); //Borders
		g.fillRect(0, 0, 692, 3);
		g.fillRect(691, 0, 3, 600);
		g.drawImage(pics.get("bar.png"), playerX, playerY, 120, 20, null);
		
		g.setColor(Color.RED);  
		g.fillOval(ballposX, ballposY, 15, 15); //ball
		
		g.setColor(Color.white);
		g.setFont(new Font("MV Boli", Font.BOLD, 25));
		g.drawString("Score: " + score, 530, 30);
		//Flappy bird
		if(birdCount==1)
		    g.drawImage(pics.get("bird1"), birdX, birdY, 50, 50, null);
		else if(birdCount==2)
			g.drawImage(pics.get("bird2"), birdX, birdY, 50, 50, null);
		else if(birdCount==3)
			g.drawImage(pics.get("bird3"), birdX, birdY, 50, 50, null);
		else
			g.drawImage(pics.get("bird4"), birdX, birdY, 50, 50, null);
		
		//
		
		
		if (totalBricks <= 0) {
			
			play = false;
			g.setColor(new Color(0XFF6464));
			g.setFont(new Font("MV Boli", Font.BOLD, 30));
			g.drawString("You Won, Score: " + score, 190, 300);
			try {
				PreparedStatement pstmt=con.prepareStatement("UPDATE mygame SET score=? WHERE id=?");
				pstmt.setInt(1, score);
				pstmt.setInt(2, 1);
				pstmt.executeUpdate();
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
			g.setFont(new Font("MV Boli", Font.BOLD, 20));
			g.drawString("Press Enter to Restart.", 230, 350);
		}
		
		if(ballposY > 570) {
			play = false;
			ballXdir = 0;
			ballYdir = 0;
			
			g.setColor(Color.BLACK);
			
			//
			int dupScore=0;
			try {
				PreparedStatement pstmt=con.prepareStatement("select * from mygame where id=?");
				pstmt.setInt(1, 1);
				ResultSet rs=pstmt.executeQuery();
				
				while(rs.next())
				{
					dupScore=rs.getInt(2);
				}
				if(score>dupScore)
				     { 
					           pstmt=con.prepareStatement("UPDATE mygame SET score=? WHERE id=?");
				               pstmt.setInt(1, score);
				               pstmt.setInt(2, 1);
				               pstmt.executeUpdate();
				      }
			} catch (Exception e) {
				
				System.out.println(e);
			}
			//
			if(score<dupScore) {
				g.setFont(new Font("MV Boli", Font.BOLD, 20));
				g.drawString(" Game Over, Score: " + score, 230, 300);
				g.drawString("High Score : "+dupScore, 230, 350);
				g.setFont(new Font("MV Boli", Font.BOLD, 20));
				g.drawString("Press Enter to Restart", 230, 400);
				
			}
			else {
				g.setFont(new Font("MV Boli", Font.BOLD, 20));
				g.drawString(" Yay ! , HighScore: " + score, 230, 350);
				g.drawString("Press Enter to Restart", 230, 400);
			}
			
				
		} 
		g.dispose();
	}

		
	@Override
	public void keyTyped(KeyEvent key) {
		System.out.println(key);
	}
	
	@Override
	public void keyPressed(KeyEvent key) {
		if(key.getKeyCode() == KeyEvent.VK_RIGHT) { 
			if(playerX >= 600) {
				playerX = 600;
			} else {
				
				if(play == true)
				playerX += 50;
					
			}
		}
		if(key.getKeyCode() == KeyEvent.VK_LEFT) { 
			if(playerX < 10) {
				playerX = 10;
			} else {
				if(play == true)
				playerX -= 50;
					
			}
		}
		
		if(key.getKeyCode() == KeyEvent.VK_ENTER) { 
			if(!play) {
				play = true;
				ballposX = 120;
				ballposY = 350;
				ballXdir = -1;
				ballYdir = -2;
				birdX=0;
				score = 0;
				totalBricks = 20*14*3;
				map = new Bricks(20,14);
				
				repaint();
			}
		}
		
	}	
		

	@Override
	public void keyReleased(KeyEvent arg0) {
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		timer.start();
		
		if(play) {
			
			if(new Rectangle(ballposX, ballposY, 15, 15).intersects(new Rectangle(playerX, playerY, 120, 20))) {
				ballYdir = - ballYdir;
			}
			
			for( int i = 0; i<map.arr.length; i++) { 
				for(int j = 0; j<map.arr[0].length; j++) { 
					if(map.arr[i][j] > 0) {
						int brickX = j*map.brickWidth + 110;
						int brickY = i*map.brickHeight + 20;
						int brickWidth= map.brickWidth;
						int brickHeight = map.brickHeight;
						
						Rectangle rect = new Rectangle(brickX, brickY, brickWidth, brickHeight);
						Rectangle ballRect = new Rectangle(ballposX, ballposY, 15,15);
						Rectangle brickRect = rect;
						
						if(ballRect.intersects(brickRect) ) {
							int num=map.arr[i][j];
							map.setBrickValue(num-1, i, j);
							totalBricks--;
							score+=5;
							
							if(ballposX + 14 <= brickRect.x || ballposX +1 >= brickRect.x + brickRect.width) 
								ballXdir = -ballXdir;
							 else {
								ballYdir = -ballYdir;
							}
						}
						
					}
					
				}
			   }
			
			if(score >= 250 && score % 250 == 0)
			{
				playerY-=15;
				score+=10;
				//Bird
				birdX=0;
				birdY-=30;
				
			}
			ballposX += ballXdir;
			ballposY += ballYdir;
			if(ballposX < 0) { 
				ballXdir = -ballXdir;
			}
			if(ballposY < 0) { 
				ballYdir = -ballYdir;
			}
			if(ballposX > 670) { 
				ballXdir = -ballXdir;  
			
			}
			
			//BirdPos
			  birdX+=1;
			  birdCount+=1;
			  if(birdCount>4)
				  birdCount=1;
		}
			
	repaint();

	}
	
}
public class MyGame {

	public static void main(String[] args) {
		JFrame obj = new JFrame();
		GamePlay gamePlay = new GamePlay();
		obj.setBounds(10, 10, 710, 640);
		obj.setTitle("Brick Breaker");
		obj.setResizable(true);
		obj.setVisible(true);
		obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		obj.add(gamePlay);
		
	}

}

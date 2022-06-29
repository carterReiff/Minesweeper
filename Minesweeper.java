
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class minesweeper 
{
	private static final int MINE = 9;
	
	private static double DIFFICULTY = 1.5;
	
	private JFrame frame;
	private JButton reset;
	private Container grid;
	private JComboBox difficulty;
	private String[] diff = {"Easy", "Medium", "Hard"};
	
	private final ActionListener listener = actionEvent ->
	{
		Object caller = actionEvent.getSource();
		if(caller == reset)
			setMines();
		else if(caller == difficulty)
		{
			switch(difficulty.getSelectedIndex())
			{
				case 0 -> {
					DIFFICULTY = 1.5;
					size = 10;
				}
				case 1 -> {
					DIFFICULTY = 2.0;
					size = 15;
					frame.setSize(750,750);
				}
				case 2 -> {
					DIFFICULTY = 2.5;
					size = 20;
					frame.setSize(1000,1000);
				}
				default -> {
					DIFFICULTY = 1.5;
					size = 10;
				}
			}
			redrawBoard();
		}
		else step((Cell) caller);
	};
	
	private int size;
	private static Cell[] neighbors = new Cell[8];
	
	private Cell[][] board;
	private final Random r = new Random();
	
	private class Cell extends JButton implements MouseListener
	{
		private final int row;
		private final int col;
		int val;
		boolean pressed;
		
		Cell(final int row, final int col, final ActionListener listener)
		{
			this.row = row;
			this.col = col;
			
			addMouseListener(this);
			
			addActionListener(listener);
			setText("");
		}
		
		boolean isMine()
		{
			return val == MINE;
		}
		
		void reset()
		{
			val = 0;
			setBackground(Color.WHITE);
			setEnabled(true);
			setText("");
		}
		
		void reveal()
		{
			if(isMine())
				setText("X");
			else setText(String.valueOf(val));
			setEnabled(false);
		}
		
		void updateValue()
		{
			getNeighbors(neighbors);
			for(Cell neighbor : neighbors)
			{
				if(neighbor == null)
					continue;
				if(neighbor.isMine())
					val++;
			}
		}
		
		void getNeighbors(final Cell[] cell)
		{
			for(int i = 0; i < neighbors.length; i++)
				neighbors[i] = null;
			
			int entry = 0;
			
			for(int relRow = -1; relRow <= 1; relRow++)
			{
				for(int relCol = -1; relCol <= 1; relCol++)
				{
					if(relRow == 0 && relCol == 0)
						continue;
					
					int currRow = row + relRow;
					int currCol = col + relCol;
					
					if(currRow < 0 || currRow >= size || currCol < 0 || currCol >= size)
						continue;
					
					cell[entry++] = board[currRow][currCol];
				}
			}
		}
		
		Cell[] getAdjacent()
		{
			Cell[] adjacents = new Cell[4];

			if(row-1 >= 0)
				adjacents[0] = board[row-1][col];
			if(col+1 < size)
				adjacents[1] = board[row][col+1];
			if(row+1 < size)
				adjacents[2] = board[row+1][col];
			if(col-1 >= 0)
				adjacents[3] = board[row][col-1];

			return adjacents;
		}
		
		@Override
		public int hashCode()
		{
			return Objects.hash(row,col);
		}
		
		@Override
		public boolean equals(Object object)
		{
			if(getClass() != object.getClass())
				return false;
			Cell obj = (Cell) object;
			return (row == obj.row && col == obj.col);
		}
		
		@Override
		public void mouseClicked(MouseEvent e)
		{
		}
		
		@Override
		public void mouseReleased(MouseEvent e) 
		{
			getModel().setArmed(false);
			getModel().setPressed(false);
			if(pressed)
			{
				if(SwingUtilities.isRightMouseButton(e))
				{
					if(getBackground() == Color.red)
						setBackground(Color.white);
					else 
					{
						setBackground(Color.red);
						setOpaque(true);
					}
				}
			}
			pressed = false;
		}

		@Override
		public void mousePressed(MouseEvent e) 
		{
			getModel().setArmed(true);
			getModel().setPressed(true);
			pressed = true;
		}

		@Override
		public void mouseExited(MouseEvent e) 
		{
			pressed = false;
		}

		@Override
		public void mouseEntered(MouseEvent e) 
		{
			pressed = true;
		}
		
	}
	
	private minesweeper()
	{
		size = 10;
		board = new Cell[size][size];
		
		frame = new JFrame("Minesweeper");
		frame.setSize(500, 500);
		frame.setLayout(new BorderLayout());
		
		setButtonPanel();
		initializeBoard();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	private void initializeBoard()
	{
		grid = new Container();
		grid.setLayout(new GridLayout(size,size));
		for(int r = 0; r < size; r++)
			for(int c = 0; c < size; c++)
			{
				board[r][c] = new Cell(r,c,listener);
				grid.add(board[r][c]);
			}
		
		setMines();
		frame.add(grid, BorderLayout.CENTER);
	}
	
	private void redrawBoard()
	{
		frame.remove(grid);
		board = new Cell[size][size];
		initializeBoard();
		grid.revalidate();
		grid.repaint();
	}
	
	private void setButtonPanel()
	{
		JPanel buttons = new JPanel();
		reset = new JButton("Reset");
		difficulty = new JComboBox(diff);
		reset.addActionListener(listener);
		difficulty.addActionListener(listener);
		buttons.add(reset);
		buttons.add(difficulty);
		frame.add(buttons, BorderLayout.SOUTH);
	}
	
	private void setMines()
	{
		reset();
		int numMines = (int) DIFFICULTY * size;
		
		Set<Integer> cells = new HashSet<>(size*size);
		for(int r = 0; r < size; r++)
			for(int c = 0; c < size; c++)
				cells.add(r*size + c);
		
		for(int i = 0; i < numMines; i++)
		{
			int mineCell = r.nextInt(cells.size());
			int row = mineCell / size;
			int col = mineCell % size;
			
			board[row][col].val = MINE;
			cells.remove(mineCell);
		}
		
		for(int r = 0; r < size; r++)
			for(int c = 0; c < size; c++)
				if(!board[r][c].isMine())
					board[r][c].updateValue();
	}
	
	private void reset()
	{
		for(int r = 0; r < size; r++)
			for(int c = 0; c < size; c++)
				board[r][c].reset();
	}
	
	private void step(Cell cell)
	{
		if(!cell.isMine())
			clearCell(cell);
		else loser();
		
		winner();
	}
	
	private void clearCell(Cell cell)
	{
		cell.reveal();
		if(cell.val != 0)
			return;
		Cell adjacents[] = cell.getAdjacent();
		for(Cell adjacent : adjacents)
		{
			if(adjacent == null)
				continue;
			if(adjacent.val == 0 && adjacent.isEnabled())
				clearCell(adjacent);
			else if(!adjacent.isMine())
				adjacent.reveal();
		}
	}
	
	private void loser()
	{
		for(int r = 0; r < size; r++)
			for(int c = 0; c < size; c++)
				if(!board[r][c].isEnabled())
				{
					board[r][c].reveal();
					board[r][c].setForeground(Color.red);
				}
		
		JOptionPane.showMessageDialog(frame, "You stepped on a mine.", "BOOM", JOptionPane.ERROR_MESSAGE);
		setMines();
	}
	
	private void winner()
	{
		boolean win = true;
		for(Cell[] row : board)
			for(Cell cell : row)
				if(!cell.isMine() && cell.isEnabled())
					win = false;
		
		if(win)
		{
			JOptionPane.showMessageDialog(frame, "You win!", "GG", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private static void run()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignore) {}
		
		new minesweeper();
	}
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> minesweeper.run());
	}
}

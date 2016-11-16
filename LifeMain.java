/**
 * Conway's Game of Life
 * 
 * @author Alex Thoennes
 * 
 * Rules:
 * 1) Any live cell with 2 or 3 live neighbors stays alive. (stable)
 * 2) Any live cell with more than 3 or less than 2 live neighbors dies. (over population/ under population)
 * 3) Any dead cell with exactly 3 live neighbors comes alive. (re-population)
 * 
 * This version utilizes the dead border approach.
 * 
 */
public class LifeMain
{
	public static void main(String[] args)
	{
		// create new game of life  with a 32x32 board
		GameOfLife life = new GameOfLife();
		life.geneticAlgorithm(10);
	}
}
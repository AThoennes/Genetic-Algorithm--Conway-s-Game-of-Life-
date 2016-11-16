import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Alex Thoennes
 */
public class GameOfLife 
{
	// these are the two game boards the program will use to create each other
	// I look at one board and use those cells to create the next board
	int [][] board1 = new int [32][32];
	int [][] board2 = new int [32][32];


	// variables used to store the best conditions
	int [][] bestBox = new int [8][8];
	int [][] bestFinal = new int [32][32];
	int [][] bestFinal2 = new int [32][32];
	int [][] bestStart = new int [32][32];
	int [][] bestStart2 = new int [32][32];
	int [][] box;


	// booleans to tell which board you are on
	boolean b1;
	boolean b2;


	// number of desired genertions
	int generations;

	// best solution so far
	float best;


	// number of live cells at the beginning and the end
	float startLiveCells;
	float endLiveCells;

	// list of the population
	ArrayList<int [][]> population = new ArrayList<int [][]>();


	/**
	 * Default constructor for the game of life
	 */
	public GameOfLife()
	{
		// fill the boards with 0's
		fillBoard(board1);
		fillBoard(board2);
	}


	/**
	 * creates a random pattern on the matrix
	 * that is passed into it
	 * 
	 * @param box
	 */
	private void randomStart(int [][] box)
	{
		Random rand = new Random();


		for (int i = 0; i < box.length; i ++)
		{
			for (int j = 0; j < box.length; j ++)
			{
				// make the spot 0 or 1
				box[i][j] = rand.nextInt(2);
			}
		}
	}


	/**
	 * fill the passed in array with 0's
	 * 
	 * @param array
	 */
	private void fillBoard(int [][] array)
	{
		for (int i = 0; i < array.length; i ++)
		{
			for (int j = 0; j < array.length; j ++)
			{
				array [i][j] = 0;
			}
		}
	}


	/**
	 * Put the random start into the larger array. Also
	 * used if we find multiple fitnesses
	 * 
	 * @param array
	 */
	private void setUp(int [][] array)
	{
		// fill the current board with 0's
		fillBoard(array);
		box = new int [8][8];

		randomStart(box);

		// used in putting the 8x8 middle box
		// into the 32x32
		int spot = (array.length / 2) - 4;


		for (int i = spot; i < spot + 8; i ++)
		{
			for (int j = spot; j < spot + 8; j ++)
			{
				array[i][j] = box[i-spot][j-spot];
			}
		}
	}


	/**
	 * This is where the main process of determining whether 
	 * a cell lives or not is done.
	 * 
	 * @param iterations
	 * @param array
	 */
	private void run(int iterations, int [][] array)
	{
		// want to start on board 1
		b1 = true;
		b2 = false;

		// make board1 = passed in array
		// you do not want to modify the
		// passed in array here
		combineArrays(array, currentBoard());

		// number of live cells to start
		startLiveCells = countLiveCells(currentBoard());


		// perform the game until the number of desired generations has been reached
		while (generations < iterations)
		{
			// for every cell
			for (int i = 0; i < currentBoard().length; i ++)
			{
				for (int j = 0; j < currentBoard().length; j ++)
				{
					// reinforces the idea of a dead border (no cells can live on 
					// the border, 
					// the other option is to wrap the matrix like a map in a civ 
					// game)
					if (atBorder(currentBoard(), i, j))
					{
						// nextBoard() and currentBoard() are dependent on 
						// the booleans b1 and b2
						nextBoard()[i][j] = 0;
					}
					else
					{
						// if you've found a live cell
						if (alive(currentBoard(), i, j))
						{
							// check to see if the cell has 2 or 3 live 
							// neighbors
							if (checkNeighbors(currentBoard(), i, j) == 2 || checkNeighbors(currentBoard(), i, j) == 3)
							{
								// stay alive if it does
								nextBoard()[i][j] = 1;
							}
							else
							{
								// otherwise it dies
								nextBoard()[i][j] = 0;
							}
						}
						// if you found a dead cell
						else if (!alive(currentBoard(), i, j))
						{
							// count the number of live neighbors
							if (checkNeighbors(currentBoard(), i, j) == 3)
							{
								// if the number is exactly 3, then the 
								// current cell
								// comes to life
								nextBoard()[i][j] = 1;
							}
							else
							{
								nextBoard()[i][j] = 0;
							}
						}
					}
				}
			}


			generations ++;


			if (b1)
			{
				// switch to board two and move on to
				// the next generation
				b1 = false;
				b2 = true;
			}
			else if (b2)
			{
				// otherwise switch to board 1 and 
				// move on to the next generation
				b1 = true;
				b2 = false;
			}
		}


		// number of live cells at the end of the game
		endLiveCells = countLiveCells(nextBoard());
	}


	/**
	 * perform a cross over with the two parents
	 * and then return their child
	 * 
	 * @param parent1
	 * @param parent2
	 * @return
	 */
	private int [][] crossOver(int [][] parent1, int [][] parent2)
	{
		// initialize the child to a 32x32
		int [][] child = new int [32][32];


		// the only area that a cross over would be productive
		// is the 8x8 box
		int spot = (parent1.length / 2) - 4;


		for (int row = spot; row < spot + 8; row ++)
		{
			for (int column = spot; column < spot + 8; column ++)
			{
				// use XOR
				if (parent1[row][column] == parent2[row][column])
				{
					child[row][column] = 0;
				}
				else if (parent1[row][column] != parent2[row][column])
				{
					child[row][column] = 1;
				}
			}
		}


		return child;
	}


	/**
	 * This method mutates the array that is passed in 
	 * 1% of the time
	 * 
	 * @param original
	 * @return
	 */
	private int [][] mutate(int [][] original)
	{
		// initialize the mutated array
		int [][] mutatedArray = new int [32][32];


		// create new random object
		Random rand = new Random();


		int spot = (original.length / 2) - 4;


		// iterate over the 8x8 array
		for (int row = spot; row < spot + 8; row ++)
		{
			for (int column = spot; column < spot + 8; column ++)
			{
				// true 1% of the time
				boolean val = rand.nextInt(101) < 1;


				// if its that 1% then mutate
				if (val && !atBorder(original, row, column))
				{
					// mutate means to invert 1 to 0
					// and 0 to 1
					if (original[row][column] == 0)
					{
						mutatedArray[row][column] = 1;
					}
					else if (original[row][column] == 1)
					{
						mutatedArray[row][column] = 0;
					}
				}
				else
				{
					mutatedArray[row][column] = original[row][column];
				}
			}
		}


		return mutatedArray;
	}
	
	private void saveBox(int [][] array)
	{
		int spot = (array.length / 2) - 4;
		
		for (int row = spot; row < spot + 8; row ++)
		{
			for (int column = spot; column < spot + 8; column ++)
			{
				bestBox[row-spot][column-spot] = array[row][column];
			}
		}
	}

	/**
	 * This is what is called from the main class.
	 * This performs the genetic algorithm.
	 * 
	 * @param desiredPop
	 */
	public void geneticAlgorithm(int desiredPop)
	{
		// start at iteration 0
		int iteration = 0;


		// initial best value
		best = Float.NEGATIVE_INFINITY;


		// this is the *unsorted* array of fitness values
		// I leave this unsorted because the indices in
		// the population list directly line up with 
		// their fitness values in this array
		float [] fitnessVals = new float [desiredPop];


		// these two lines are only used for getting the 
		// current system time and then add it to a set 
		// number of hours
		long t = System.currentTimeMillis();
		long end = t + TimeUnit.HOURS.toMillis(8);


		// used to only reference things in the population
		int index = 0;


		// construct the initial population
		while (index < desiredPop)
		{
			population.add(new int [32][32]);
			setUp(population.get(index));
			index ++;
		}


		// runs for the set amount of time
		while (System.currentTimeMillis() < end)
		{
			// retrieve each fitness value
			// population[1]'s fitness is at fitnessVals[1]
			for (int i = 0; i < population.size(); i ++) 
			{
				run(1000, population.get(i));
				float num = endLiveCells / ((float)2.0 * startLiveCells);
				fitnessVals[i] = num;
			}

			// if we have a duplicate fitness then replace it
			for (int i = 0; i < fitnessVals.length; i ++)
			{
				for (int j = 0; j < fitnessVals.length; j ++)
				{
					if (fitnessVals[j] == fitnessVals[i])
					{
						population.set(j, new int [32][32]);
						setUp(population.get(j));
						run(1000, population.get(j));
						float num = endLiveCells / ((float)2.0 * startLiveCells);
						fitnessVals[j] = num;
					}
				}
			}

			// this makes it easier to find the largest fitness value
			// and then walk through the unordered fitness array and 
			// compare the current fitness to the desired fitness until
			// we find the one we want
			float [] fitsOrdered = orderFitnesses(fitnessVals);

			fitness(fitsOrdered[0], iteration, fitsOrdered, fitnessVals);

			// replace 7, 9, 10 with a cross over of 1 and 2, 3 and 4, 1 and 4
			int [][] crossed = crossOver(population.get(findDesiredFit(fitnessVals, 1)), 
			population.get(findDesiredFit(fitnessVals, 2)));
			population.set(findDesiredFit(fitnessVals, 7), crossed);


			crossed = crossOver(population.get(findDesiredFit(fitnessVals, 3)), 
			population.get(findDesiredFit(fitnessVals, 4)));
			population.set(findDesiredFit(fitnessVals,9), crossed);


			crossed = crossOver(population.get(findDesiredFit(fitnessVals, 1)), 
			population.get(findDesiredFit(fitnessVals, 4)));
			population.set(findDesiredFit(fitnessVals,10), crossed);

			// then mutate 6, 8, 5
			int [][] mutated = mutate(population.get(findDesiredFit(fitnessVals, 1)));
			population.set(findDesiredFit(fitnessVals, 6), mutated);


			mutated = mutate(population.get(findDesiredFit(fitnessVals, 2)));
			population.set(findDesiredFit(fitnessVals, 8), mutated);


			mutated = mutate(population.get(findDesiredFit(fitnessVals, 3)));
			population.set(findDesiredFit(fitnessVals, 5), mutated);


			iteration ++;
		}

		// display the best results
		System.out.println("\nBest Start Board: ");
		displayBoard(bestStart);


		System.out.println("\nBest Final Board: ");
		displayBoard(bestFinal);

		System.out.println("\nBest 8x8 in compact: ");
		displayCompactBoard(bestBox);

		System.out.println("\nBest Fitness: " + best);
	}


	/**
	 * used in saving the arrays
	 * 
	 * @param a1
	 * @param a2
	 */
	private void combineArrays(int [][] a1, int [][] a2)
	{
		for (int i = 0; i < a1.length; i ++)
		{
			for (int j = 0; j < a1.length; j ++)
			{
				a2[i][j] = a1[i][j];
			}
		}
	}

	/**
	 * Used in determining if the found fitness is better than
	 * the current best
	 * 
	 * @param sol
	 * @param iteration
	 * @param vals
	 */
	private void fitness(float sol, int iteration, float [] vals, float [] f)
	{
		if (sol > best && sol != Float.POSITIVE_INFINITY)
		{
			best = sol;
			System.out.println("Iteration: " + iteration + "     Best: " + best);
			bestStart = population.get(findDesiredFit(f, 1));
			run(1000, bestStart);
			bestFinal = nextBoard();
			saveBox(bestStart);
		}
		else if (sol == Float.POSITIVE_INFINITY)
		{
			for (int i = 0; i < vals.length; i ++)
			{
				if (vals[i] > best && vals[i] != Float.POSITIVE_INFINITY)
				{
					best = vals[i];
					System.out.println("Iteration: " + iteration + "     Best: " + best);
					bestStart = population.get(findDesiredFit(f, 1));
					run(1000, bestStart);
					bestFinal = nextBoard();
					saveBox(bestStart);
					break;
				}
			}
		}
	}


	/**
	 * Looks at the two boolean values and determines 
	 * which board is the current board
	 * 
	 * @return
	 */
	private int [][] currentBoard()
	{
		if (b1)
		{
			// on board1
			return board1;
		}
		else
		{
			// on board2
			return board2;
		}
	}


	/**
	 * Returns the board that you want to modify based
	 * off the cells in your current board
	 * 
	 * @return
	 */
	private int [][] nextBoard()
	{
		// if you're on board1
		// you want to modify board2
		if (b1)
		{
			return board2;
		}
		else
		{
			// otherwise modify board1
			return board1;
		}
	}


	// order the array from least to greatest then reverse it
	// so most fit is at position 0 in the array
	private float [] orderFitnesses(float [] array)
	{
		float [] unsortedArray = array;
		float [] orderedArray = new float [unsortedArray.length];
		Arrays.sort(unsortedArray);
		int a = 0;


		for (int i = unsortedArray.length - 1; i > -1; i --)
		{
			orderedArray[a] = unsortedArray[i];
			a++;
		}


		return orderedArray;
	}


	// runs through the unsorted float array and looks for the
	// nth largest fitness
	private int findDesiredFit(float [] array, int nth)
	{
		float [] k = orderFitnesses(array);
		int index = 0;


		float desired = k[nth - 1];


		for (int i = 0; i < array.length; i ++)
		{
			if (array[i] == desired)
			{
				index = i;
			}
		}
		return index;
	}


	/**
	 *  This method looks at the cells around your current cell
	 *  to determine how many live neighbors it has
	 * 
	 * @param array
	 * @param i
	 * @param j
	 * @return numAlive
	 */
	private int checkNeighbors(int [][] array, int i , int j)
	{
		// number of live neighboring cells
		int numAlive = 0;


		// use ternary and set the min/maxRow and min/maxCol
		// to the proper values only if they are not in the dead border
		int minRow = (i != 0) ? i - 1 : 0;
		int maxRow = (i != array.length - 1) ? i + 1: 0;


		int minCol = (j != 0) ? j - 1 : 0;
		int maxCol = (j != array.length - 1) ? j + 1: 0;


		// iterates over the neighboring cells and counts how many are alive
		for (int row = minRow; row <= maxRow; row++) 
		{
			for (int column = minCol; column <= maxCol; column++) 
			{
				if (array[row][column] == 1 && !(row == i && column == j)) 
				{
					numAlive++;
				}
			}
		}


		// finally return the number of live cells
		return numAlive;
	}


	/**
	 * Used to check if the current cell is at the border
	 * 
	 * @param array
	 * @param i
	 * @param j
	 * @return
	 */
	private boolean atBorder(int [][] array, int i, int j)
	{
		// check if the cell is at the border
		if (i == array.length - 1 || i == 0 || j == 0 || j == array.length - 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}


	/**
	 * Check if the current cell is alive or dead
	 * 
	 * @param array
	 * @param i
	 * @param j
	 * @return
	 */
	private boolean alive(int [][] array, int i, int j)
	{
		// current cell 
		if (array[i][j] == 0)
		{
			return false;
		}
		else 
		{
			return true;
		}
	}


	/**
	 * counts and returns the number of 
	 * live cells in the board
	 * 
	 * @param array
	 * @return
	 */
	private int countLiveCells(int [][] array)
	{
		int numAlive = 0;


		for (int i = 0; i < array.length; i ++)
		{
			for (int j = 0; j < array.length; j ++)
			{
				if (array[i][j] == 1)
				{
					numAlive ++;
				}
			}	
		}


		return numAlive;
	}


	/**
	 * This method looks at the binary matrix and for every 4
	 * elements it reads in, it is then converted to hexadecimal
	 * and printed to provide a more compact form of output
	 * 
	 * @param array
	 */
	private void displayCompactBoard(int [][] array)
	{
		// num will contain the four elements
		String num = "";


		// for every space in board
		for (int row = 0; row < array.length; row ++)
		{
			for (int column = 0; column < array.length; column ++)
			{
				// add the next element
				num = num + array[row][column];


				// if your length is 4
				if (num.length() == 4)
				{
					// convert it to hexadecimal
					int binary = Integer.parseInt(String.valueOf(num), 2);
					String hex = Integer.toHexString(binary);


					// print out the hex value
					System.out.print(hex.toUpperCase());


					// then reset num
					num = "";
				}
			}
			System.out.println();
		}
	}


	private void displayBoard(int [][] array)
	{
		for (int i = 0; i < array.length; i ++)
		{
			for (int j = 0; j < array.length; j ++)
			{
				if (array[i][j] == 0)
				{
					System.out.print(".");
				}
				else
				{
					System.out.print("*");
				}
			}
			System.out.println();
		}
	}
}

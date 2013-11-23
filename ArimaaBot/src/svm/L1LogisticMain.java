package svm;

import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.SolverType;
import utilities.GameData;
import utilities.HypothesisTest;
import utilities.helper_classes.Utilities;

public class L1LogisticMain {
	
	private static final int MAX_NUM_X_POINTS = 15;
	private static final int START_X_POINT = 1;
	private static final int X_SCALING = 10;
	private static final double TRAIN_FRACTION = 0.7;

	public static void main(String[] args) {
		final long totalStartTime = System.currentTimeMillis();
		
		// please redirect console output to file:
		// right click NBMain.java -> Run as -> Run Configurations... -> select "Common" tab
		// 						   -> check "File" under "Standard Input and Output"
		//                         -> enter destination file name :D
		
		for (int x = START_X_POINT; x <= MAX_NUM_X_POINTS; x++) {
			int numGames = x * X_SCALING;
			System.out.println("-------------------------------");
			System.out.println("------START OF ROUND (" + (numGames) + ")------");
			System.out.println("-------------------------------\n");
			final long startTime = System.currentTimeMillis();
			
			System.out.println("Training and testing on " + numGames + " games...");
			
			GameData myGameData = new GameData(numGames, TRAIN_FRACTION);
			System.out.println("Finished fetching game data");
			
			SVMTrain trainingModel = new SVMTrain();
			System.out.println("Created the SVM model");
	
			Model l1Model = trainingModel.train(myGameData, new Parameter(SolverType.L1R_LR, 1.0, .001));
			System.out.println("Just finished training!");
			
			System.out.println("About to evaluate model: creating a hypothesis...");
			SVMHypothesis myHypothesis = new SVMHypothesis( l1Model );
			
			System.out.println("\nTesting hypothesis on TEST set...");
			myGameData.setMode(GameData.Mode.TEST);
			HypothesisTest.test(myHypothesis, myGameData);
			
			System.out.println("\nTesting hypothesis on TRAIN set...");
			myGameData.setMode(GameData.Mode.TRAIN);
			HypothesisTest.test(myHypothesis, myGameData);
			
			final long endTime = System.currentTimeMillis();
			System.out.println("Round execution time: " + Utilities.msToString(endTime - startTime));
			System.out.println("\n------------------------");
			System.out.println("------END OF ROUND------");
			System.out.println("------------------------\n\n");
		}
		
		final long totalEndTime = System.currentTimeMillis();
		System.out.println("\n\nTotal execution time (from process running to termination): " 
					+ Utilities.msToString(totalEndTime - totalStartTime));
		
	}

}

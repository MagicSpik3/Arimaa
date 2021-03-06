package game_phase;

import utilities.helper_classes.Utilities;
import ai_util.Util;
import arimaa3.*;

public class FeatureExtractor implements Constants {

	/* General constants */ 
	public static final int NUM_RANKS = 8;
	public static final int NUM_TRAPS = 4; 
	
	/* Feature count constants */
	public static final int NUM_PIECES_FEATURES = 2;
	public static final int NUM_PIECE_RANK_FEATURES = 12*NUM_RANKS;
	public static final int NUM_TRAP_DOMINANCE_FEATURES = NUM_TRAPS * 2;
	public static final int NUM_FEATURES = NUM_PIECES_FEATURES + NUM_PIECE_RANK_FEATURES + 2*NUM_TRAPS;
	
	/* Maximum total value for all features in each feature class, used for normalizing feature ranges */
	
	// Calculation for MAX_TRAP_DOMINANCE_SCORE: 
	// 9 + 8 + 7 + 7 + 5/2 + 5/2 + 4/2 + 4/2 + 3/2 + 3/2 + 3/2 + 3/2 + 3/2 + 3 * 3/3
	public static final double MAX_TRAP_DOMINANCE_SCORE = 50.5 * NUM_TRAP_DOMINANCE_FEATURES;
	public static final double MAX_RANKS = NUM_RANKS * NUM_PIECE_RANK_FEATURES;
	public static final double MAX_NUM_PIECES = 16.0 * NUM_PIECES_FEATURES;
	
	
	/* Constants for reduced feature set alternative */
	private static final int NUM_REDUCED_FEATURES = 2;
	
	private static final long RANKS_FOUR_THRU_EIGHT = RANK_4 | RANK_5 | RANK_6 | RANK_7 | RANK_8;
	private static final long RANKS_ONE_THRU_FIVE = RANK_1 | RANK_2 | RANK_3 | RANK_4 | RANK_5;


	//PL_WHITE and PL_BLACK are constants that refer to the index into the features array
	
	/**
	 * Extracts features for the resulting board after playing a possible legal move.
	 * current_board is the resulting board after playing current_move on prev game state.
	 */
	public static double[] extractFeatures(GameState state) {
		double[] features = new double[NUM_FEATURES];
		extractNumPiecesPerPlayer(state, features);
		extractRank(state, features);
		extractTrapDominance(state, features);
		return features;
	}
	
	/**
	 * Extract features to be used in GamePhaseHeuristicDiscriminator
	 */
	public static double[] extractReducedFeatures(GameState state){
		double[] features = new double[NUM_REDUCED_FEATURES];
		features[0] = extractMinNumPieces(state);
		features[1] = extractMaxNumDisplaced(state);
		return features;
	}
	
	/**
	 * @return the smaller of the number of gold or silver pieces on the board
	 */
	private static int extractMinNumPieces(GameState state){
		return Math.min(Util.PopCnt(state.colour_bb[PL_WHITE]), Util.PopCnt(state.colour_bb[PL_BLACK]));
	}
	
	/**
	 * @return the higher value of 'numDisplaced' for gold and silver, where 'numDisplaced' is the 
	 * number of non-elephant pieces advanced beyond one's own trap squares. 
	 */
	private static int extractMaxNumDisplaced(GameState state){
		long[] piece_bb = state.piece_bb;
		int whiteScore = 0;
		int blackScore = 0;
		
		// Count all pieces except for elephants
		for (int pieceType = PT_WHITE_RABBIT; pieceType < PT_WHITE_ELEPHANT; pieceType++){
			
			if (pieceType % 2 == PL_WHITE) // White pieces
				whiteScore += Util.PopCnt(piece_bb[pieceType] & RANKS_FOUR_THRU_EIGHT);
			
			else // Black pieces
				blackScore += Util.PopCnt(piece_bb[pieceType] & RANKS_ONE_THRU_FIVE);
		}
		
		return Math.max(whiteScore, blackScore);
	}
	
	private static int extractImminentGoalFeature(GameState state){
		//TODO: implement
		
		return 0;
	}
	
	private static void extractNumPiecesPerPlayer(GameState state, double[] features){
		features[PL_WHITE] = Util.PopCnt( state.colour_bb[PL_WHITE] ) / MAX_NUM_PIECES;
		features[PL_BLACK] = Util.PopCnt( state.colour_bb[PL_BLACK] ) / MAX_NUM_PIECES;
	}
	
	private static void extractRank(GameState state, double[] features){
		for(int piece_type = PT_WHITE_RABBIT; piece_type <= PT_BLACK_ELEPHANT; piece_type++){
			for(int rank = 0; rank < NUM_RANKS; rank++){
				long curr_rank = RANK_1 << (rank*NUM_RANKS); //the current rank
				features[NUM_PIECES_FEATURES + piece_type*NUM_RANKS +rank] = 
						Util.PopCnt( state.piece_bb[piece_type] & curr_rank) / MAX_RANKS;
			}
		}
	}
	
	private static int getDistance(int cell, int trapID){
		int rank = cell/8;
		int col = cell % 8;
		
		if (trapID == 0){
			return Math.abs( rank - 2 ) + Math.abs( col - 2 );
		}
		else if (trapID == 1){
			return Math.abs(rank - 5) + Math.abs(col - 2);
		}
		else if (trapID == 2){
			return Math.abs(rank - 2) + Math.abs(col - 5);
		}
		else {
			return Math.abs(rank - 5) + Math.abs(col - 5);
		}
		
	}
	
	private static void extractTrapDominance(GameState state, double[] features){
		int[] weights = { 9, 8, 7, 5, 4, 1, 2, 3 };
		byte[] pieceRanks = Utilities.calculatePieceTypes(state);
		for(int i = 0; i < TRAP.length; i++){
			for(int j = 0; j < state.piece_bb.length; j++){
				double score = 0;

				for (int cell = 0; cell < 64; cell++){
					if ((state.piece_bb[j] & (1L << cell)) > 0){
						int distance = getDistance(cell, i);
						
						if (distance > 4){
							continue; //dont consider pieces that are greater than 4 away
						} else if (distance == 0){
							distance = 2; //assume that distance of a piece on trap is equivalent to 2 hops away
						}
						
						score += (1.0/distance)*weights[pieceRanks[j]];
						
					}
				}
				
				int trapFeatureID= (j % 2)*TRAP.length + i; //assigns a unique index for each (player, trap square) tuple
				features[NUM_PIECES_FEATURES + NUM_PIECE_RANK_FEATURES + trapFeatureID] += (score / MAX_TRAP_DOMINANCE_SCORE);
			}
		}
		

	}


	
}
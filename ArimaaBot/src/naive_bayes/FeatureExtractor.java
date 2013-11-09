package naive_bayes;
import java.util.BitSet;

import ai_util.LogFile;
import arimaa3.*;

public class FeatureExtractor{
	
	private static final int NUM_FEATURES = 1040;
	private static final int NUM_SRC_MVMT = 512;
	private static final int NUM_LOCATIONS = 32;
	private static final int NUM_PIECE_TYPES = 8;
	
	/* Game state resulting from a move that we are extracting feature vectors for */
	private GameState curr;
	
	/* Original game state that is one move behind curr (i.e. before trying all possible moves from prev)*/
	private GameState prev;
	
	private GameState prev_prev;
	
	/* "Expert" move actually played by person/bot in training data from prev game state */
	private ArimaaMove expert_move;
	
	/* Piece category --> type mapping in curr GameState. E.g. if piece_types[3] = 4, this means that "black cat" 
	 * is of piece type 4 for the current GameState. */
	private byte piece_types[];
	
	private BitSet featureVector;
	
	/* Move that changed prev to curr */
	private ArimaaMove current_move;
	
	public FeatureExtractor(GameState prev, ArimaaMove expert_move) {
		this.expert_move = expert_move;
		this.prev = prev;
		prev_prev = null;
		curr = null;
		featureVector = null;
		current_move = null;
		piece_types = new byte[12];
	}
	
	public BitSet extractFeatures(GameState current_board, ArimaaMove current_move){
		featureVector = new BitSet(NUM_FEATURES);
		curr = current_board;
		this.current_move = current_move;
		calculatePieceTypes();
		// feature extraction subroutine calls here
		
		generateMovementFeatures();
		return featureVector;
	}
	

	private void generateMovementFeatures() {
		
		long[] move_bb = current_move.piece_bb;
		for(int i = 0; i < 12; i++) {
			long source = curr.piece_bb[i] & move_bb[i];
			long dest = source ^ move_bb[i];
			updateBitSetMovementFeatures(source, dest, i);
		}
	}

	private void updateBitSetMovementFeatures(long source, long dest, int piece_id) {
		//TODO captures
		int player = (piece_id%2==0) ? 0 : 1; //white if even
		//higher rows are at the most significant bits
		for(int i = 0; i < Long.SIZE; i++) {
			if((source & (1L << i)) > 0) {
				setSrcMovementFeature(player, piece_types[piece_id], getLocation(i));
			}
			if((dest & (1L << i)) > 0) {
				setDestMovementFeature(player, piece_types[piece_id], getLocation(i));
			}
		}
	}
	
	private int getLocation(int index) {
		int row = index >> 3;
		index = index & 0x07; //reduces all rows to the same indices as first row
		index = (index > 3) ? 7 - index : index;
		return index + row << 2;
	}

	private void setSrcMovementFeature(int player, int piece_type, int location) {
		int index = player*NUM_PIECE_TYPES*NUM_LOCATIONS + piece_type*NUM_LOCATIONS + location;
		featureVector.set(index);
	}
	
	private void setDestMovementFeature(int player, int piece_type, int location) {
		int index = NUM_SRC_MVMT + player*NUM_PIECE_TYPES*(NUM_LOCATIONS+1) + piece_type*(NUM_LOCATIONS+1) + location;
		featureVector.set(index);
	}

	public void incrementMove(GameState new_board, ArimaaMove expert_move){
		this.expert_move = expert_move;
		prev_prev = prev;
		prev = new_board;
	}
	
	// Calculates the piece type (e.g. 3) for each piece category (e.g. black dog).
	private void calculatePieceTypes(){
		// compute_secondary_bitboards has to have been called, so
		// that stronger_enemy_bb is up-to-date.
		
		for (int i = 0; i < 2; i++){ // calculate for rabbits 
			byte numStronger = countOneBits(curr.stronger_enemy_bb[i]);
			if (numStronger < 5)
				piece_types[i] = 7;
			else if (numStronger < 7)
				piece_types[i] = 6;
			else
				piece_types[i] = 5;
		}
		
		for (int i = 2; i < 12; i++){ // calculate for non-rabbits
			byte numStronger = countOneBits(curr.stronger_enemy_bb[i]);
			switch (numStronger) {
				case 0: piece_types[i] = 0; break;
				case 1: piece_types[i] = 1; break;
				case 2: piece_types[i] = 2; break;
				case 3: case 4: piece_types[i] = 3; break;
				default: piece_types[i] = 4; break;
			}
		}
	}
	
	private static byte countOneBits(long n) {
		// Algorithm adapted from http://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetKernighan
		byte c; // c accumulates the total bits set in v
		for (c = 0; n != 0; c++)
			n &= n - 1; // clear the least significant bit set
		return c;
	}
	
	
	private static String tests[] = {
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|         D   r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		
		"2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   d X     X     |%135|   d R M c       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
	};


	// This has been tested and works as expected. 
	private static void testPieceTypes(){
		
		FeatureExtractor fe = new FeatureExtractor(null, null);
//	    String white ="1w Ee2 Me1 Hg2 Hb2 Df2 Dd1 Cc2 Cd2 Ra2 Rh2 Ra1 Rb1 Rc1 Rf1 Rg1 Rh1";
//	    String black = "1b ha7 db7 cc7 md7 ee7 cf7 dg7 hh7 ra8 rb8 rc8 rd8 re8 rf8 rg8 rh8";
//
//	    GameState gs = new GameState(white,black);
//	    System.out.println(gs.toBoardString());
//	    fe.extractFeatures(gs);	
	    
	    for (String text : tests) {
		      GameState position = new GameState(text);
		      System.out.println(position.toBoardString());
//		      fe.extractFeatures(position);
	    }
	}
	
	public static void main(String[] args){
		// testPieceTypes();

	}
	
}
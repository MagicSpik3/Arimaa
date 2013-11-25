package feature_extractor;

import java.util.BitSet;
import arimaa3.GameState;

public class TrapExtractor extends AbstractExtractor {

	private GameState prev, curr;
	
	/** Extracts (change in) Trap Status information, using prev and curr 
	 * @param prev The previous game state (leading to curr)
	 * @param curr The current game state to be analyzed */
	public TrapExtractor(GameState prev, GameState curr) {
		this.prev = prev;
		this.curr = curr;
	}
	
	
	/** The portion of the BitSet that will be updated is TRAP_STATUS_END - TRAP_STATUS_START
	 *  + 1 (== 512) bits. These bits will be split up as follows:
	 *  <br> -There will be <b>four equally sized</b> chunks of bits, one for each trap.
	 *  <br> -Each chunk will be <b>split in half</b>, one half for white, the other for black. 
	 *  <br> -For each player's half, only one bit will be turned on, corresponding to the <i>status change</i>:
	 *  <br> [This bit is TrapStatus.NUM_STATUSES * prevStatus + currStatus (our version of change-encoding) into the player's half] */
	@Override
	public void updateBitSet(BitSet bitset) { //HAS NOT BEEN TESTED. Test me! :D
		int startOfRange = startOfRange(); //used later
		int numFeatures = endOfRange() - startOfRange + 1;
		int whiteOffset = 0;
		int blackOffset = TrapStatus.NUM_STATUSES * TrapStatus.NUM_STATUSES;
		
		for (int trap = 0; trap < TRAP.length; trap++) {
			byte prevStatusWhite = getTrapStatus(prev, trap, PL_WHITE);
			byte prevStatusBlack = getTrapStatus(prev, trap, PL_BLACK);
			byte currStatusWhite = getTrapStatus(curr, trap, PL_WHITE);
			byte currStatusBlack = getTrapStatus(curr, trap, PL_BLACK);
			
			int trapOffset = (trap * numFeatures) / TRAP.length;
			int white = trapOffset + whiteOffset + TrapStatus.NUM_STATUSES * prevStatusWhite + currStatusWhite;
			int black = trapOffset + blackOffset + TrapStatus.NUM_STATUSES * prevStatusBlack + currStatusBlack;
			
			bitset.set(startOfRange + white, true);
			bitset.set(startOfRange + black, true);
		}
	}

	@Override
	public int startOfRange() {
		return FeatureRange.TRAP_STATUS_START;
	}

	
	@Override
	public int endOfRange() {
		return FeatureRange.TRAP_STATUS_END;
	}
	
	/** Returns a byte containing one of the TrapStatus.NUM_STATUSES possible
	 * trap statuses (number of pieces on trap, elephant?, etc).
	 * @param state The current board
	 * @param trapNum Lower left, upper left, lower right, upper right
	 * @param playerNum PL_WHITE, PL_BLACK */
	public static byte getTrapStatus(GameState state, int trapNum, int playerNum) { //remove static, make private
		long player_trap_bb = TOUCH_TRAP[trapNum] & state.colour_bb[playerNum]; // all white or black pieces touching trap #trapNum
		byte numPieces = FeatureExtractor.countOneBits(player_trap_bb);
		assert(numPieces <= 4); //shouldn't be more than 4 pieces touching a trap...
		
		boolean elephant = isElephantTouchingTrap(state, trapNum, playerNum);
		return TrapStatus.convertToStatus(numPieces, elephant);
	}
	
	/** Returns whether an elephant for the given player (in the current game state)
	 * is on any of the four spaces adjacent to the given trap. */
	public static boolean isElephantTouchingTrap(GameState state, int trapNum, int playerNum) { //remove static once done testing, make private
		int boardNum = playerNum == PL_WHITE ? PT_WHITE_ELEPHANT : PT_BLACK_ELEPHANT;
		long elephant_bb = state.piece_bb[boardNum];
		//System.out.printf("%x\n", elephant_bb);
		return (elephant_bb & TOUCH_TRAP[trapNum]) != 0;
	}
}

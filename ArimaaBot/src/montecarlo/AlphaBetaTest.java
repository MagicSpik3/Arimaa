package montecarlo;

import java.util.ArrayList;

import org.junit.Test;

import utilities.helper_classes.ArimaaState;
import arimaa3.ArimaaEvaluate2;
import arimaa3.ArimaaMove;
import arimaa3.Constants;
import arimaa3.GameState;
import arimaa3.MoveList;

public class AlphaBetaTest {
	public static int DEPTH = 3; //TODO: must be an even number so that the evaluation will evaluate from the perspective of the root node
	private static class TestAgent extends AlphaBetaSearchAgent {

		private boolean firstMove = false;
		private ArimaaEvaluate2 eval;
		public ArrayList< ArrayList<Double> > depthPrint;
		

		public TestAgent(int depth) {
			super(null, false, depth);
			eval = new ArimaaEvaluate2();
			depthPrint = new ArrayList< ArrayList<Double> >();
			for(int i = 0; i < DEPTH; i++){
				depthPrint.add( new ArrayList<Double>() );
			}
		}

		@Override
		protected double getGameOverScore(GameState unused) { return Constants.SCORE_MATE; }

		private double getRank(long bb){
			if ( (bb & Constants.RANK_1) > 0){
				return 1;
			}
			else if ( (bb & Constants.RANK_2) > 0){
				return 2;
			}
			else if ( (bb & Constants.RANK_3) > 0){
				return 3;
			}
			else if ( (bb & Constants.RANK_4) > 0){
				return 4;
			}
			else if ( (bb & Constants.RANK_5) > 0){
				return 5;
			}
			else if ( (bb & Constants.RANK_6) > 0){
				return 6;
			}
			else if ( (bb & Constants.RANK_7) > 0){
				return 7;
			}			
			else if ( (bb & Constants.RANK_8) > 0){
				return 8;
			}
			
			
			return 0;
		}
		
		@Override
		protected double evaluation(ArimaaState state) {
			if (state.getCurr().player == Constants.PL_WHITE ){
				return getRank( state.getCurr().piece_bb[Constants.PT_WHITE_RABBIT] );
			} else {
				return -getRank( state.getCurr().piece_bb[Constants.PT_WHITE_RABBIT] );
			}
			//return eval.Evaluate(state.getCurr(), false);
		}

		public static int PRUNE = 3;
		
	
		public MoveList getMoves(GameState state) {
			MoveList moves = engine.genRootMoves(state); 
			MoveList m = new MoveList(PRUNE);
			int[] positions = {1,6,12};
			for(int i = 0; i < PRUNE; i++ ){
				ArimaaMove move = m.getMove();
				move.copy( moves.move_list[ positions[i] ] );
			}
			
			return m;
		}
		
		@Override
		public ArimaaMove selectMove(final ArimaaState arimaaState, MoveList moves){
			if (firstMove){
				eval.PreProcessRootPosition(arimaaState.getCurr());
				firstMove = false;
			}
			ArimaaMove bestMove = null;
			double maxAlpha = Double.NEGATIVE_INFINITY;
			
			for(ArimaaMove move : moves){
				double a = AlphaBeta(new ArimaaState(arimaaState.getPrevPrev(), arimaaState.getPrev(), arimaaState.getCurr(), 
								arimaaState.getPrevPrevMove(), arimaaState.getPrevMove(), move) , maxDepth - 1, maxAlpha, Double.POSITIVE_INFINITY, false);
				if (maxAlpha < a){
					maxAlpha = a;
					bestMove = move;
				}
			}
			System.out.println("Root Alpha:"+maxAlpha);
			System.out.println("Best Move:"+bestMove);
			
			for(int i = depthPrint.size() - 1; i >= 0; i-- ){
				ArrayList<Double> nodes = depthPrint.get(i);
				for(int j = 0; j < nodes.size(); j++){
					double t = nodes.get(j);
					if (t == Double.POSITIVE_INFINITY ){
						System.out.print( "| ");
					}
					else {
						System.out.print( nodes.get(j) + " ");
					}
				}
				
				System.out.println();
			}
			
			return bestMove;
		}
		
		//TOOD add prints
		private double AlphaBeta(final ArimaaState state, int depth, double alpha, double beta, boolean isMaxPlayer){
			GameState next = new GameState();
			next.playFullClear(state.getNextMove(), state.getCurr());
			
			int sign = (maxDepth - depth) % 2 == 0 ? 1 : -1;
			
			ArimaaState nextState = new ArimaaState(state.getPrev(), state.getCurr(), next, 
					state.getPrevMove(), state.getNextMove(), null);
			
			if (next.isGameOver()){
				double score =  sign*(-next.getGameResult());
				depthPrint.get(depth).add( score );
				return score;
			} else if (depth == 0){
				double score =  sign*evaluation(nextState);
				depthPrint.get(depth).add( score );
				return score; //TODO determine if we should put a negative here 
			}

			MoveList moves = getMoves(nextState);  //Changed getMoves for just a GameState and called it on next as oppsed to curr
			
			if (isMaxPlayer){
				for(ArimaaMove move: moves){
					alpha = Math.max(alpha, AlphaBeta(new ArimaaState(state.getPrev(), state.getCurr(), next,
							state.getPrevMove(), state.getNextMove(), move), depth - 1, alpha, beta, false) );
					if (beta <= alpha)
						break; // beta cut off
				}
				depthPrint.get(depth).add( alpha );
				depthPrint.get(depth-1).add( Double.POSITIVE_INFINITY );
				return alpha;
			} else {
				for(ArimaaMove move: moves){
					beta = Math.min(beta, AlphaBeta(new ArimaaState(state.getPrev(), state.getCurr(), next,
							state.getPrevMove(), state.getNextMove(), move), depth - 1, alpha, beta, true) );
					if (beta <= alpha)
						break; // alpha cut off
				}
				depthPrint.get(depth).add( beta );
				depthPrint.get(depth-1).add( Double.POSITIVE_INFINITY );
				return beta;
			}
			
		}

		@Override
		protected MoveList getMoves(ArimaaState state) {
			MoveList moves = engine.genRootMoves(state.getCurr()); 
			MoveList m = new MoveList(PRUNE);
			int[] positions = {1,6,12};
			for(int i = 0; i < PRUNE; i++ ){
				ArimaaMove move = m.getMove();
				move.copy( moves.move_list[ positions[i] ] );
			}
			
			return m;
		}
		
		
	}

	@Test
	public void test() {
		TestAgent agent = new TestAgent(DEPTH);
		ArimaaState game = new ArimaaState(new GameState("1w Rb1", "1b rg8"), null);
		agent.selectMove(game, agent.getMoves(game.getCurr()) );
	}

}

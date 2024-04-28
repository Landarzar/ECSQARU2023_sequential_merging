package kappaMerge.operators;


import edu.cs.ai.alchourron.logic.Formula;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalSignature;
import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;


/**
 * 
 * Classes have to implement the method 'buildRanking', that constructs a ranking function from a formula.
 * 
 * @author Kai Sauerwald
 *
 */

public interface RankingConstructor {
    
    
	/**
	 * Constructs a ranking function form a formula.
	 * 
	 * @param formula Propositional formula.
	 * @return	Ranking function representing the plausibility ranking regarding the formula.
	 */
	public RankingFunction<PropositionalInterpretation<Character>> buildRanking(Formula<PropositionalSignature<Character>> formula);
}

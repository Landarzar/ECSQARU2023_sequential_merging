package kappaMerge.operators.merging;

import kappaMerge.operators.MergingOperator;

import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;

/**
 * Implementation of the Delta_max operator proposed by Thomas Meyer.
 * Delta_max chooses the highest rank assigned to an interpretation. The result is
 * normalized after merging.
 * 
 * @link https://doi.org/10.1007/3-540-44533-1_31
 *
 */
public class MaxMerging implements MergingOperator<RankingFunction<PropositionalInterpretation<Character>>> {

    

    @Override
    public RankingFunction<PropositionalInterpretation<Character>> merge(
	    RankingFunction<PropositionalInterpretation<Character>> state1,
	    RankingFunction<PropositionalInterpretation<Character>> state2) {

	RankingFunction<PropositionalInterpretation<Character>> result = new RankingFunction<>();

	state1.forEach(pair -> {
	    result.add(pair.getFirst(), Math.max(pair.getSecond(), state2.getRank(pair.getFirst())));
	});
	result.normalize();

	if (result.getByRank(0).isEmpty())
	    System.out.println("MaxMerging - Empty");

	return result;

    }

    @Override
    public String toString() {
	return "Max";
    }
}

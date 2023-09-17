package kappaMerge.operators.merging;

import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;
import kappaMerge.operators.MergingOperator;

/**
 * Implementation of the Delta_min operator proposed by Thomas Meyer. 
 * 
 * Delta_min chooses the lowest rank assigned to an interpretation and doubles
 * it. If not all sources agree on the rank, the rank is incremented by 1. The result is
 * normalized after merging.
 * 
 * @link https://doi.org/10.1007/3-540-44533-1_31
 *
 */
public class MinMerging implements MergingOperator<RankingFunction<PropositionalInterpretation<Character>>> {

  
    public RankingFunction<PropositionalInterpretation<Character>> merge(
	    RankingFunction<PropositionalInterpretation<Character>> state1,
	    RankingFunction<PropositionalInterpretation<Character>> state2) {

	RankingFunction<PropositionalInterpretation<Character>> result = new RankingFunction<>();

	state1.forEach(pair -> {
	    if (pair.getSecond() == state2.getRank(pair.getFirst()))
		result.add(pair.getFirst(), Math.min(pair.getSecond(), state2.getRank(pair.getFirst())) * 2);
	    else
		result.add(pair.getFirst(), Math.min(pair.getSecond(), state2.getRank(pair.getFirst())) * 2 + 1);
	});
	result.normalize();

	if (result.getByRank(0).isEmpty())
	    System.out.println("MinMerging - ERROR: Empty");

	return result;

//	}
    }

    @Override
    public String toString() {
	return "Min";
    }

}
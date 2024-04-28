package kappaMerge.operators.merging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;
import kappaMerge.Main;
import kappaMerge.operators.MergingOperator;

/**
 * Implementation of the Delta_Rmin operator proposed by Thomas Meyer. 
 * 
 * Delta_Rmin assigns a rank to an interpretation based on the list-position of
 * the sequence that corresponds to the ranks that were assigned to that
 * interpretation in ranking functions that shall be merged. The list of
 * sequences favors the min values in a sequence. The result is
 * normalized after merging.
 * 
 * @link https://doi.org/10.1007/3-540-44533-1_31
 *
 */
public class RMinMerging extends RefinedOperator
	implements MergingOperator<RankingFunction<PropositionalInterpretation<Character>>> {


    @Override
    public RankingFunction<PropositionalInterpretation<Character>> merge(
	    RankingFunction<PropositionalInterpretation<Character>> state1,
	    RankingFunction<PropositionalInterpretation<Character>> state2) {

	int maxRank = 0;


	maxRank = Math.max(state1.getMaxRank(), state2.getMaxRank());
	
	List<RankingSequence> sequences = super.generateSequences(maxRank, 2);
	RankingFunction<PropositionalInterpretation<Character>> result = new RankingFunction<>();

	// Create a list of the ranks assigned to a world and compare it to the sequences in the list in oder to get the final rank.
	Main.getInterpretations().forEach(world -> {
	    List<Integer> worldRanks = new ArrayList<Integer>();
	    worldRanks.add(state1.getRank(world));
	    worldRanks.add(state2.getRank(world));
	    worldRanks.sort(Comparator.naturalOrder());
	    for (int j = 0; j < sequences.size(); j++) {
		if (worldRanks.equals(sequences.get(j).getValues())) {
		    result.add(world, j);

		}
	    }
	});

	result.normalize();

	if (result.getByRank(0).isEmpty())
	    System.out.println("RMin Merging ERROR: Empty");
	   
	return result;
    }

    @Override
    public String toString() {
	return "RMin";
    }

}
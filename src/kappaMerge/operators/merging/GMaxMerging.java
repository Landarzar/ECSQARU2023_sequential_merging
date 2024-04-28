package kappaMerge.operators.merging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;
import kappaMerge.Main;
import kappaMerge.operators.MergingOperator;

/**
 * Implementation of the Delta_Gmax operator proposed by Thomas Meyer. 
 * 
 * Delta_Gmax assigns a rank to an interpretation based on the list-position of
 * the sequence that corresponds to the ranks that were assigned to that
 * interpretation in ranking functions that shall be merged. The list of
 * sequences favors the max values in a sequence. The result is
 * normalized after merging.
 * 
 * @link https://doi.org/10.1007/3-540-44533-1_31
 *
 */
public class GMaxMerging extends RefinedOperator
	implements MergingOperator<RankingFunction<PropositionalInterpretation<Character>>> {

    @Override
    public List<RankingSequence> generateSequences(int maxRank, int lengthOfProfile) {

	List<RankingSequence> listOfSequences = super.generateSequences(maxRank, lengthOfProfile);

	// Re-sort each sequence in the list.
	for (RankingSequence sequence : listOfSequences) {
	    sequence.sortValuesNonIncreasingly();
	}

	Comparator<RankingSequence> lexicographicOrder = (sequence1, sequence2) -> {
	    for (int i = 0; i < sequence1.getValues().size(); i++) {
		int value = Integer.compare(sequence1.get(i), sequence2.get(i));
		if (value != 0)
		    return value;
	    }
	    return 0;
	};

	// Re-sort the list.
	listOfSequences.sort(lexicographicOrder);
	return listOfSequences;
    }


    @Override
    public RankingFunction<PropositionalInterpretation<Character>> merge(
	    RankingFunction<PropositionalInterpretation<Character>> state1,
	    RankingFunction<PropositionalInterpretation<Character>> state2) {

	int maxRank = 0;

	maxRank = Math.max(state1.getMaxRank(), state2.getMaxRank());

	List<RankingSequence> sequences = generateSequences(maxRank, 2);
	RankingFunction<PropositionalInterpretation<Character>> result = new RankingFunction<>();

	// Create a list of the ranks assigned to a world and compare it to the sequences in oder to get the final rank.
	Main.getInterpretations().forEach(world -> {
	    List<Integer> worldRanks = new ArrayList<Integer>();
	    worldRanks.add(state1.getRank(world));
	    worldRanks.add(state2.getRank(world));
	    worldRanks.sort(Comparator.reverseOrder());
	    for (int j = 0; j < sequences.size(); j++) {
		if (worldRanks.equals(sequences.get(j).getValues())) {
		    result.add(world, j);

		}
	    }
	});

	result.normalize();

	if (result.getByRank(0).isEmpty())
	    System.out.println("Gmax Merging ERROR: Empty");

	return result;
    }

    @Override
    public String toString() {
	return "GMax";
    }
}
package kappaMerge.operators.merging;

import java.util.ArrayList;
import java.util.List;

/**
 * Superclass of the refined operators. Provides a method, which generates a
 * list of sequences.
 * 
 */
public abstract class RefinedOperator {

    /**
     * Generates a lexicographically ordered list of all possible non-redundant and
     * non-decreasing sequences from 0 to maxValue, e.g., for maxValue = 2 and
     * length = 3: <0,0,0>, <0,0,1>, <0,0,2>, <0,1,1>, <0,1,2>, <0,2,2>,
     * <1,1,1>,<1,1,2>, <1,2,2>, <2,2,2>.
     * 
     * @param maxValue The maximum value in the entire profile.
     * @param length   The length of the profile, which determines also the length
     *                 of the sequences.
     * @return A list of RankingSequences.
     * @see RankingSequence
     */
    protected List<RankingSequence> generateSequences(int maxValue, int length) {

	if (length < 1)
	    throw new IllegalArgumentException("Length must be greater than 0.");
	List<RankingSequence> listOfSequences = new ArrayList<>();

	int lastIndex = length - 1;
	boolean finished = false;
	// Buffer for the last used sequence. Each list of sequences begins with the
	// sequence consisting of 0's.
	RankingSequence previousSequence = new RankingSequence(length);
	for (int i = 0; i < length; i++) {
	    previousSequence.add(0);
	}
	listOfSequences.add(previousSequence);

	while (finished != true) {
	    RankingSequence sequence = new RankingSequence(length);

	    // Copy the values of the last used sequence into the new sequence.
	    for (int j = 0; j < length; j++) {
		sequence.add(j, previousSequence.get(j));

	    }

	    // If the value at the last position of the sequence equals maxValue, all
	    // sequences beginning with the actual first value have been
	    // constructed.
	    if (sequence.get(lastIndex) < maxValue) {
		sequence.set(lastIndex, sequence.get(lastIndex) + 1);
		listOfSequences.add(sequence);
		// last value == max value. Search backwards for an entry lower than max value.
	    } else {
		int previousIndex = lastIndex - 1;
		while (previousIndex >= 0) {
		    // If found, increment the value and set all following positions in the sequence
		    // to this value.
		    if (sequence.get(previousIndex) < maxValue) {
			int incrementedValue = sequence.get(previousIndex) + 1;
			sequence.set(previousIndex, incrementedValue);
			for (int k = previousIndex; k < length; k++) {
			    sequence.set(k, incrementedValue);
			}
			// Add this sequence to the list of sequences.
			listOfSequences.add(sequence);
			break;
		    } else
			previousIndex--;
		}
		// All positions are on max value, i.e., all sequences were constructed.
		if (previousIndex < 0) {
		    finished = true;
		}
	    }
	    previousSequence = sequence;

	}

	return listOfSequences;
    }

}

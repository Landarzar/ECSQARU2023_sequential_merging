package kappaMerge.operators;

import java.util.LinkedList;
import java.util.List;

import edu.cs.ai.alchourron.logic.Formula;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalSignature;
import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;
import kappaMerge.DataOperator;
import kappaMerge.Main;

/**
 * Data operator that combines a ranking constructor and a merging operator.
 * This operator constructs ranking functions for the list of formulas
 * 'premises', and merges the ranking functions sequentially. This means the
 * merging result of the first two functions is merged with the third function,
 * the resulting function is then merged with the fourth function and so on.
 * 
 *
 */
public class SequentialMergingOperator implements DataOperator {

    public MergingOperator<RankingFunction<PropositionalInterpretation<Character>>> mergeOp;
    public RankingConstructor rfConstructor;
    RankingFunction<PropositionalInterpretation<Character>> currentState = new RankingFunction<>();

    public SequentialMergingOperator(RankingConstructor rfConstructor,
	    MergingOperator<RankingFunction<PropositionalInterpretation<Character>>> mergeOp) {

	this.rfConstructor = rfConstructor;
	this.mergeOp = mergeOp;

    }

    @Override
    public RankingFunction<PropositionalInterpretation<Character>> compute(
	    List<Formula<PropositionalSignature<Character>>> premises) {

	if (premises == null) {
	    throw new IllegalStateException("The list of premises is empty. Check the data set.");
	}

	RankingFunction<PropositionalInterpretation<Character>> result = new RankingFunction<>();
	LinkedList<RankingFunction<PropositionalInterpretation<Character>>> profile = new LinkedList<>();
	RankingFunction<PropositionalInterpretation<Character>> initialState = new RankingFunction<>();

	Main.getInterpretations().stream().forEach(i -> initialState.add(i, 0));
	profile.add(initialState);
	RankingFunction<PropositionalInterpretation<Character>> cState = new RankingFunction<>();

	for (int premiseCounter = 0; premiseCounter < premises.size(); premiseCounter++) {
	    cState = rfConstructor.buildRanking(premises.get(premiseCounter));
	    profile.add(cState);
	}

	profile.get(0).forEach(pair -> {
	    currentState.add(pair);

	});

	if (profile.get(1) != null) {
	    for (int stateNr = 1; stateNr < profile.size(); stateNr++) {

		result = mergeOp.merge(currentState, profile.get(stateNr));
		currentState = new RankingFunction<>();
		result.forEach(pair -> {
		    currentState.add(pair);
		});

	    }
	} else {
	    return currentState;

	}

	return result;
    }

    @Override
    public String toString() {
	return rfConstructor + "_" + mergeOp;
    }

}

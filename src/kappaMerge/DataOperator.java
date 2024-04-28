package kappaMerge;

import java.util.List;

import edu.cs.ai.alchourron.logic.Formula;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalSignature;
import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;


public interface DataOperator {

    public RankingFunction<PropositionalInterpretation<Character>> compute(
	    List<Formula<PropositionalSignature<Character>>> premises);

}

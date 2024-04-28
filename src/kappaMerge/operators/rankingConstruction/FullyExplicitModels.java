package kappaMerge.operators.rankingConstruction;

import java.util.Set;
import edu.cs.ai.alchourron.logic.Formula;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalSignature;
import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;
import kappaMerge.Main;
import kappaMerge.operators.RankingConstructor;

/**
 * This class provides a method to construct a ranking function from a formula
 * that reflects the principle of 'fully explicit models' of the mental model
 * theory, i.e. logically correct models.
 * 
 *
 */

public class FullyExplicitModels implements RankingConstructor {
    
    
    /**
     * This function assigns the rank 0 to models of the formula and rank 3 to non-models.
     *
     */
    @Override
    public RankingFunction<PropositionalInterpretation<Character>> buildRanking(Formula<PropositionalSignature<Character>> formula) {
	
	RankingFunction<PropositionalInterpretation<Character>> result = new RankingFunction<>();
	Set<PropositionalInterpretation<Character>> models = Main.getLogic().modelsOf(formula, Main.getSignature());
	models.forEach(i -> result.add(i, 0));
	Main.getInterpretations().stream().filter(i -> !models.contains(i)).forEach(i -> result.add(i, 3));
	return result;
    }

    @Override
    public String toString() {
	return "FEM";
    }

}

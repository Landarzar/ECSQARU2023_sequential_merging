package kappaMerge.operators.rankingConstruction;

import edu.cs.ai.alchourron.logic.Formula;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalSignature;
import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;
import edu.cs.ai.alchourron.logic.syntax.formula.FormulaBiImplication;
import edu.cs.ai.alchourron.logic.syntax.formula.FormulaImplication;
import kappaMerge.operators.RankingConstructor;



/**
 * This class provides a method for the construction a ranking function from a
 * formula that reflects the principle of preferred interpretations. According
 * to this principle of all possible cases for a conditional or biconditional
 * the conjunctive interpretation is considered maximally plausible (rank 0), a
 * biconditional interpretation is less plausible (rank 1) and the conditional
 * interpretation (applies only to conditionals) is considered the least
 * plausible (rank 2). Since the class PreferredInterpretationsAndFEM implements this assignment, 
 * an instance of that class is created and the formula is passed to its
 * buildRanking method. If the formula is neither a conditional nor a biconditional, an instance of
 * the class MentalModels is created and the formula is passed to its
 * buildRanking method instead.
 * 
 * @see PreferredInterpretationsAndFEM
 * @see MentalModels
 *
 */
public class PreferredInterpretationsAndMM implements RankingConstructor {

    @Override
    public RankingFunction<PropositionalInterpretation<Character>> buildRanking(
	    Formula<PropositionalSignature<Character>> formula) {

	if ((formula instanceof FormulaImplication) || (formula instanceof FormulaBiImplication)) {
	    PreferredInterpretationsAndFEM impModels = new PreferredInterpretationsAndFEM();
	    return impModels.buildRanking(formula);
	} else {
	    MentalModels otherModels = new MentalModels();
	    return otherModels.buildRanking(formula);
	}

    }

    @Override
    public String toString() {
	return "PoPI/MM";
    }

}

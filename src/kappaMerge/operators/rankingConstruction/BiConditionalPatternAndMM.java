package kappaMerge.operators.rankingConstruction;

import java.util.List;
import edu.cs.ai.alchourron.logic.Formula;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalLogic;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalSignature;
import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;
import edu.cs.ai.alchourron.logic.syntax.formula.FormulaBiImplication;
import edu.cs.ai.alchourron.logic.syntax.formula.FormulaImplication;
import kappaMerge.Main;
import kappaMerge.operators.RankingConstructor;

/**
 * This class provides a method for the construction a ranking function from a
 * formula that reflects the principle of a biconditional interpretation of a
 * conditional. According to this principle a conditional is treated like a
 * biconditional. This means the conjunctive interpretation and the
 * biconditional interpretation are considered maximally plausible (rank 0), but
 * the conditional interpretation is considered impossible (rank 3).
 * 
 * If the formula is neither a conditional nor a biconditional, an instance of
 * the class MentalModels is created and the formula is passed to its
 * buildRanking method. Since the class BiConditionalPatternAndFEM implements
 * the required assignment for conditionals and biconditionals, an instance of
 * that class is created for these formulas and it is passed to its buildRanking method.
 * 
 * @see BiConditionalPatternAndFEM
 * @see MentalModels
 *
 */
public class BiConditionalPatternAndMM implements RankingConstructor {

    final PropositionalLogic<Character> logic = Main.getLogic();
    PropositionalSignature<Character> signature = Main.getSignature();
    List<PropositionalInterpretation<Character>> interpretations = Main.getInterpretations();

    @Override
    public RankingFunction<PropositionalInterpretation<Character>> buildRanking(
	    Formula<PropositionalSignature<Character>> formula) {

	if ((formula instanceof FormulaImplication) || (formula instanceof FormulaBiImplication)) {

	    BiConditionalPatternAndFEM impModels = new BiConditionalPatternAndFEM();
	    return impModels.buildRanking(formula);
	} else {
	    MentalModels otherModels = new MentalModels();
	    return otherModels.buildRanking(formula);
	}

    }

    @Override
    public String toString() {
	return "BiC/MM";
    }
}
package kappaMerge.operators.rankingConstruction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cs.ai.alchourron.logic.Formula;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalLogic;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalSignature;
import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;
import edu.cs.ai.alchourron.logic.syntax.formula.FormulaAND;
import edu.cs.ai.alchourron.logic.syntax.formula.FormulaImplication;
import edu.cs.ai.alchourron.logic.syntax.formula.FormulaNeg;
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
 * If the formula is not a conditional, an instance of the class
 * FullyExplicitModels is created and the formula is passed to its buildRanking
 * method.
 * 
 * @see FullyExplicitModels
 *
 */
public class BiConditionalPatternAndFEM implements RankingConstructor {

    final PropositionalLogic<Character> logic = Main.getLogic();
    PropositionalSignature<Character> signature = Main.getSignature();
    List<PropositionalInterpretation<Character>> interpretations = Main.getInterpretations();

    @Override
    public RankingFunction<PropositionalInterpretation<Character>> buildRanking(
	    Formula<PropositionalSignature<Character>> formula) {

	if (!(formula instanceof FormulaImplication)) {
	    FullyExplicitModels otherModels = new FullyExplicitModels();
	    return otherModels.buildRanking(formula);
	}

	RankingFunction<PropositionalInterpretation<Character>> rankingFunction = new RankingFunction<>();

	Set<PropositionalInterpretation<Character>> remainI = new HashSet<>(interpretations);

	if (formula instanceof FormulaImplication) {
	    FormulaImplication<PropositionalSignature<Character>> iformula = (FormulaImplication<PropositionalSignature<Character>>) formula;

	    // Models of the premise and conclusion are the most preferred interpretation
	    FormulaAND<PropositionalSignature<Character>> conjPC = new FormulaAND<>(iformula.getPremise(),
		    iformula.getConclusion());
	    logic.modelsOf(conjPC, signature).stream().filter(i -> remainI.contains(i))
		    .forEach(i -> rankingFunction.add(i, 0));
	    remainI.removeAll(logic.modelsOf(conjPC, signature));

	    // Models of the negated premise and negated conclusion are 2nd
	    FormulaAND<PropositionalSignature<Character>> conjNPNC = new FormulaAND<>(
		    new FormulaNeg<>(iformula.getPremise()), new FormulaNeg<>(iformula.getConclusion()));
	    logic.modelsOf(conjNPNC, signature).stream().filter(i -> remainI.contains(i))
		    .forEach(i -> rankingFunction.add(i, 0));
	    remainI.removeAll(logic.modelsOf(conjNPNC, signature));

	    // Rest: Either premise or conclusion is negated but not both
	    remainI.forEach(i -> rankingFunction.add(i, 3));

	}
	return rankingFunction;

    }

    @Override
    public String toString() {
	return "BiC/FEM";
    }
}
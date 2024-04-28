package kappaMerge.operators.rankingConstruction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import edu.cs.ai.alchourron.logic.Formula;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalLogic;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalSignature;
import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;
import edu.cs.ai.alchourron.logic.syntax.formula.*;
import kappaMerge.Main;
import kappaMerge.operators.RankingConstructor;

/**
 * This class provides a method for the construction a ranking function from a
 * formula that reflects the principle of preferred interpretations. According
 * to this principle of all possible cases for a conditional or biconditional
 * the conjunctive interpretation is considered maximally plausible (rank 0), a
 * biconditional interpretation is less plausible (rank 1) and the conditional
 * interpretation (applies only to conditionals) is considered the least
 * plausible (rank 2).
 * 
 * If the formula is neither a conditional nor a biconditional, an instance of
 * the class FullyExplicitModels is created and the formula is passed to its
 * buildRanking method.
 * 
 * @see FullyExplicitModels
 *
 */

public class PreferredInterpretationsAndFEM implements RankingConstructor {

    final PropositionalLogic<Character> logic = Main.getLogic();
    PropositionalSignature<Character> signature = Main.getSignature();
    List<PropositionalInterpretation<Character>> interpretations = Main.getInterpretations();

    @Override
    public RankingFunction<PropositionalInterpretation<Character>> buildRanking(
	    Formula<PropositionalSignature<Character>> formula) {

	if ((!(formula instanceof FormulaImplication)) && (!(formula instanceof FormulaBiImplication))) {
	    FullyExplicitModels otherModels = new FullyExplicitModels();
	    return otherModels.buildRanking(formula);
	}

	RankingFunction<PropositionalInterpretation<Character>> rankingFunction = new RankingFunction<>();

	Set<PropositionalInterpretation<Character>> remainI = new HashSet<>(interpretations);

	if (formula instanceof FormulaImplication) {
	    FormulaImplication<PropositionalSignature<Character>> iformula = (FormulaImplication<PropositionalSignature<Character>>) formula;

	    // Models of the conjunction of the premise and conclusion are the most
	    // preferred interpretation
	    FormulaAND<PropositionalSignature<Character>> conjPC = new FormulaAND<>(iformula.getPremise(),
		    iformula.getConclusion());
	    logic.modelsOf(conjPC, signature).stream().filter(i -> remainI.contains(i))
		    .forEach(i -> rankingFunction.add(i, 0));
	    remainI.removeAll(logic.modelsOf(conjPC, signature));

	    // Models of the conjunction of the negated premise and negated conclusion are
	    // 2nd
	    FormulaAND<PropositionalSignature<Character>> conjNPNC = new FormulaAND<>(
		    new FormulaNeg<>(iformula.getPremise()), new FormulaNeg<>(iformula.getConclusion()));
	    logic.modelsOf(conjNPNC, signature).stream().filter(i -> remainI.contains(i))
		    .forEach(i -> rankingFunction.add(i, 1));
	    remainI.removeAll(logic.modelsOf(conjNPNC, signature));

	    // Models of the conjunction of conclusion and the negated premise are 3rd
	    FormulaAND<PropositionalSignature<Character>> conjNPC = new FormulaAND<>(
		    new FormulaNeg<>(iformula.getPremise()), iformula.getConclusion());
	    logic.modelsOf(conjNPC, signature).stream().filter(i -> remainI.contains(i))
		    .forEach(i -> rankingFunction.add(i, 2));
	    remainI.removeAll(logic.modelsOf(conjNPC, signature));

	    // Rest: Non-models
	    remainI.forEach(i -> rankingFunction.add(i, 3));

	} else {
	    // Models of the conjunction of both operands are the most preferred
	    // interpretations

	    FormulaBiImplication<PropositionalSignature<Character>> iformula = (FormulaBiImplication<PropositionalSignature<Character>>) formula;
	    FormulaAND<PropositionalSignature<Character>> conjOs = new FormulaAND<>(iformula.getFirst(),
		    iformula.getSecond());
	    logic.modelsOf(conjOs, signature).stream().filter(i -> remainI.contains(i))
		    .forEach(i -> rankingFunction.add(i, 0));
	    remainI.removeAll(logic.modelsOf(conjOs, signature));

	    // Models of the conjunction of the negated operands are 2nd

	    FormulaAND<PropositionalSignature<Character>> conjNO = new FormulaAND<>(
		    new FormulaNeg<>(iformula.getFirst()), new FormulaNeg<>(iformula.getSecond()));
	    logic.modelsOf(conjNO, signature).stream().filter(i -> remainI.contains(i))
		    .forEach(i -> rankingFunction.add(i, 1));
	    remainI.removeAll(logic.modelsOf(conjNO, signature));

	    // Rest: Non-models
	    remainI.forEach(i -> rankingFunction.add(i, 3));
	}

	return rankingFunction;

    }

    @Override
    public String toString() {
	return "PoPI/FEM";
    }
}
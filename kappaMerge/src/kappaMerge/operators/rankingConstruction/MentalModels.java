package kappaMerge.operators.rankingConstruction;

import java.util.HashSet;
import java.util.LinkedList;
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
 * This class provides a method to construct a ranking function from a formula,
 * which reflects the principle of truth and 'mental models' of the mental model
 * theory.
 * 
 *
 */

public class MentalModels implements RankingConstructor {

    final static PropositionalLogic<Character> logic = Main.getLogic();
    final static PropositionalSignature<Character> signature = Main.getSignature();
    final List<PropositionalInterpretation<Character>> interpretations = Main.getInterpretations();

    @Override
    public RankingFunction<PropositionalInterpretation<Character>> buildRanking(
	    Formula<PropositionalSignature<Character>> formula) {

	RankingFunction<PropositionalInterpretation<Character>> rankingFunction = new RankingFunction<>();
	Set<PropositionalInterpretation<Character>> models = Main.getLogic().modelsOf(formula, Main.getSignature());

	if (formula instanceof FormulaImplication) {

	    Set<PropositionalInterpretation<Character>> remainingInterpretations = new HashSet<>(interpretations);

	    FormulaImplication<PropositionalSignature<Character>> iformula = (FormulaImplication<PropositionalSignature<Character>>) formula;

	    // Models of the conjunction of premise and conclusion are the most preferred
	    // interpretations
	    FormulaAND<PropositionalSignature<Character>> conjPC = new FormulaAND<>(iformula.getPremise(),
		    iformula.getConclusion());
	    logic.modelsOf(conjPC, signature).stream().filter(i -> remainingInterpretations.contains(i))
		    .forEach(i -> rankingFunction.add(i, 0));
	    remainingInterpretations.removeAll(logic.modelsOf(conjPC, signature));

	    FormulaAND<PropositionalSignature<Character>> conjNPNC = new FormulaAND<>(
		    new FormulaNeg<>(iformula.getPremise()), new FormulaNeg<>(iformula.getConclusion()));
	    logic.modelsOf(conjNPNC, signature).stream().filter(i -> remainingInterpretations.contains(i))
		    .forEach(i -> rankingFunction.add(i, 2));
	    remainingInterpretations.removeAll(logic.modelsOf(conjNPNC, signature));

	    FormulaAND<PropositionalSignature<Character>> conjNPC = new FormulaAND<>(
		    new FormulaNeg<>(iformula.getPremise()), iformula.getConclusion());
	    logic.modelsOf(conjNPC, signature).stream().filter(i -> remainingInterpretations.contains(i))
		    .forEach(i -> rankingFunction.add(i, 2));
	    remainingInterpretations.removeAll(logic.modelsOf(conjNPC, signature));

	    remainingInterpretations.forEach(i -> rankingFunction.add(i, 3));
	    return rankingFunction;

	}

	if (formula instanceof FormulaBiImplication) {

	    Set<PropositionalInterpretation<Character>> remainingInterpretations = new HashSet<>(interpretations);

	    FormulaBiImplication<PropositionalSignature<Character>> iformula = (FormulaBiImplication<PropositionalSignature<Character>>) formula;

	    // Models of the conjunction are the most preferred interpretations
	    FormulaAND<PropositionalSignature<Character>> conjPC = new FormulaAND<>(iformula.getFirst(),
		    iformula.getSecond());
	    logic.modelsOf(conjPC, signature).stream().filter(i -> remainingInterpretations.contains(i))
		    .forEach(i -> rankingFunction.add(i, 0));
	    remainingInterpretations.removeAll(logic.modelsOf(conjPC, signature));

	    FormulaAND<PropositionalSignature<Character>> conjNO = new FormulaAND<>(
		    new FormulaNeg<>(iformula.getFirst()), new FormulaNeg<>(iformula.getSecond()));
	    logic.modelsOf(conjNO, signature).stream().filter(i -> remainingInterpretations.contains(i))
		    .forEach(i -> rankingFunction.add(i, 2));
	    remainingInterpretations.removeAll(logic.modelsOf(conjNO, signature));

	    remainingInterpretations.forEach(i -> rankingFunction.add(i, 3));
	    return rankingFunction;

	}

	if (formula instanceof FormulaNeg) {
	    List<Formula<PropositionalSignature<Character>>> operands = ((FormulaNeg<PropositionalSignature<Character>>) formula)
		    .getOperands();
	    Set<PropositionalInterpretation<Character>> remainingInterpretations = new HashSet<>(interpretations);

	    // Case NAND
	    if ((operands.size()) == 1 && (operands.get(0) instanceof FormulaAND)) {
		List<Formula<PropositionalSignature<Character>>> conjuncts = ((FormulaAND<PropositionalSignature<Character>>) operands
			.get(0)).getOperands();
		List<Formula<PropositionalSignature<Character>>> negConjuncts = new LinkedList<>();

		for (Formula<PropositionalSignature<Character>> conj : conjuncts) {
		    negConjuncts.add(new FormulaNeg<PropositionalSignature<Character>>(conj));
		}
		FormulaAND<PropositionalSignature<Character>> negMod = new FormulaAND<PropositionalSignature<Character>>(
			negConjuncts);
		logic.modelsOf(negMod, signature).stream().filter(i -> remainingInterpretations.contains(i))
			.forEach(i -> rankingFunction.add(i, 0));

		models.removeAll(logic.modelsOf(negMod, signature));
		remainingInterpretations.removeAll(logic.modelsOf(negMod, signature));
		models.stream().forEach(i -> rankingFunction.add(i, 1));
		remainingInterpretations.removeAll(models);
		remainingInterpretations.forEach(i -> rankingFunction.add(i, 3));
		return rankingFunction;

	    }
	}

	if (formula instanceof FormulaOR) {

	    Set<PropositionalInterpretation<Character>> remainingInterpretations = new HashSet<>(interpretations);

	    List<Formula<PropositionalSignature<Character>>> operands = ((FormulaOR<PropositionalSignature<Character>>) formula)
		    .getOperands();
	    boolean allOperandsLiteral = true;
	    for (Formula<PropositionalSignature<Character>> operand : operands) {
		allOperandsLiteral = operand.isAtom();
		// Check for negated atoms.
		if (operand instanceof FormulaNeg) {
		    List<Formula<PropositionalSignature<Character>>> negOperands = ((FormulaNeg<PropositionalSignature<Character>>) operand)
			    .getOperands();
		    if (negOperands.size() == 1) {
			allOperandsLiteral = true;
		    }
		}

		if (allOperandsLiteral == false)
		    break;

	    }

	    // Case "Simple disjunction"
	    if (allOperandsLiteral) {

		FormulaAND<PropositionalSignature<Character>> allDisjuncts = new FormulaAND<PropositionalSignature<Character>>(
			operands);
		logic.modelsOf(allDisjuncts, signature).stream().filter(i -> remainingInterpretations.contains(i))
			.forEach(i -> rankingFunction.add(i, 1));

		models.removeAll(logic.modelsOf(allDisjuncts, signature));
		remainingInterpretations.removeAll(logic.modelsOf(allDisjuncts, signature));
		models.stream().forEach(i -> rankingFunction.add(i, 0));
		remainingInterpretations.removeAll(models);
		remainingInterpretations.forEach(i -> rankingFunction.add(i, 3));
		return rankingFunction;

	    }

	}

	FullyExplicitModels otherModels = new FullyExplicitModels();
	return otherModels.buildRanking(formula);

    }

    @Override
    public String toString() {
	return "MM";
    }

}

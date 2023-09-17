package kappaMerge;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import edu.cs.ai.alchourron.logic.Formula;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalSignature;
import edu.cs.ai.alchourron.logic.syntax.formula.FormulaAND;
import edu.cs.ai.alchourron.logic.syntax.formula.FormulaBiImplication;
import edu.cs.ai.alchourron.logic.syntax.formula.FormulaImplication;
import edu.cs.ai.alchourron.logic.syntax.formula.FormulaNeg;
import edu.cs.ai.alchourron.logic.syntax.formula.FormulaOR;
import edu.cs.ai.alchourron.logic.syntax.formula.FormulaPropositionalAtom;
import edu.cs.ai.math.settheory.Pair;



/**
 * 
 * Represents a single task for a participant including its sequence number in the experiment, the answer options and the given response.
 * The plain text is parsed into formulas, @see edu.cs.ai.alchourron.logic.Formula.
 * 
 *
 */

public class DataRecord {

	public String participant;
	public int sequence;
	public List<Formula<PropositionalSignature<Character>>> task = new LinkedList<>();
	public List<Formula<PropositionalSignature<Character>>> answerOptions = new LinkedList<>();
	public Formula<PropositionalSignature<Character>> response = null;
	public String taskText;
	public String responseText;

	private static <T> T[] suffixArray(T[] array, int beg) {
		return Arrays.copyOfRange(array, beg, array.length);
	}

	// Parser 
	void fill(String[] dataRow) {

		this.participant = dataRow[0];
		this.sequence = Integer.parseInt(dataRow[1]);

		// task
		String[] dataToSplit = dataRow[2].split("/");
		for (String data : dataToSplit) {
			this.task.add(buildformula(data.split(";")).getFirst());
		}
		this.taskText = getTaskText();

		// choices
		dataToSplit = dataRow[3].replace('|', '/').split("/");
		for (String data : dataToSplit) {
			if (data.equals("nothing"))
				continue;
			this.answerOptions.add(buildformula(data.split(";")).getFirst());
		}

		// response
		if (!dataRow[4].equals("nothing")) {
			this.response = buildformula(dataRow[4].split(";")).getFirst();
			this.responseText = response.toString(); 
		}
		else 
			this.responseText = "nothing";
		
	}

	private Pair<Formula<PropositionalSignature<Character>>, Integer> buildformula(String[] data) {
		if (data[0].trim().toLowerCase().equals("a"))
			return new Pair<Formula<PropositionalSignature<Character>>, Integer>(
					new FormulaPropositionalAtom<Character, PropositionalSignature<Character>>('a'), 1);
		if (data[0].trim().toLowerCase().equals("b"))
			return new Pair<Formula<PropositionalSignature<Character>>, Integer>(
					new FormulaPropositionalAtom<Character, PropositionalSignature<Character>>('b'), 1);
		if (data[0].trim().toLowerCase().equals("not")) {
			Pair<Formula<PropositionalSignature<Character>>, Integer> rec1 = buildformula(suffixArray(data, 1));
			return new Pair<Formula<PropositionalSignature<Character>>, Integer>(
					new FormulaNeg<PropositionalSignature<Character>>(rec1.getFirst()), rec1.getSecond());
		}
		if (data[0].trim().toLowerCase().equals("and")) {
			Pair<Formula<PropositionalSignature<Character>>, Integer> rec1 = buildformula(suffixArray(data, 1));
			Pair<Formula<PropositionalSignature<Character>>, Integer> rec2 = buildformula(
					suffixArray(data, 1 + rec1.getSecond()));
			return new Pair<Formula<PropositionalSignature<Character>>, Integer>(
					new FormulaAND<>(rec1.getFirst(), rec2.getFirst()),
					1 + rec2.getSecond());
		}
		if (data[0].trim().toLowerCase().equals("or")) {
			Pair<Formula<PropositionalSignature<Character>>, Integer> rec1 = buildformula(suffixArray(data, 1));
			Pair<Formula<PropositionalSignature<Character>>, Integer> rec2 = buildformula(
					suffixArray(data, 1 + rec1.getSecond()));
			return new Pair<Formula<PropositionalSignature<Character>>, Integer>(
					new FormulaOR<>(rec1.getFirst(), rec2.getFirst()),
					1 + rec1.getSecond() + rec2.getSecond());
		}
		if (data[0].trim().toLowerCase().equals("if")) {
			Pair<Formula<PropositionalSignature<Character>>, Integer> rec1 = buildformula(suffixArray(data, 1));
			Pair<Formula<PropositionalSignature<Character>>, Integer> rec2 = buildformula(
					suffixArray(data, 1 + rec1.getSecond()));
			return new Pair<Formula<PropositionalSignature<Character>>, Integer>(
					new FormulaImplication<>(rec1.getFirst(),
							rec2.getFirst()),
					1 + rec1.getSecond() + rec2.getSecond());
		}
		if (data[0].trim().toLowerCase().equals("iff")) {
			Pair<Formula<PropositionalSignature<Character>>, Integer> rec1 = buildformula(suffixArray(data, 1));
			Pair<Formula<PropositionalSignature<Character>>, Integer> rec2 = buildformula(
					suffixArray(data, 1 + rec1.getSecond()));
			return new Pair<Formula<PropositionalSignature<Character>>, Integer>(
					new FormulaBiImplication<>(rec1.getFirst(),
							rec2.getFirst()),
					1 + rec1.getSecond() + rec2.getSecond());
		}
		throw new UnsupportedOperationException("Data format not supported!");
	}
	
	// Improves readability of the task text.
	private String getTaskText() {
		
		String taskText = "| ";
		
		for (int i = 1; i <= task.size(); i++) {
		    taskText = taskText.concat("Premise " + i + ": " + task.get(i-1) + " | "); 
		}
		
		return taskText;

		
	    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((participant == null) ? 0 : participant.hashCode());
		result = prime * result + sequence;
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataRecord other = (DataRecord) obj;
		if (participant == null) {
			if (other.participant != null)
				return false;
		} else if (!participant.equals(other.participant))
			return false;
		if (sequence != other.sequence)
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.participant + ": " + this.sequence + " | " + this.taskText
				+ " Reponse: " + this.responseText;

	}
}

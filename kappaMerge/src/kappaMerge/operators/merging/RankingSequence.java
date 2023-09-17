package kappaMerge.operators.merging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Representation of a sequence of natural numbers. Provides several methods to
 * sort and add the values in the sequence.
 *
 */
public class RankingSequence {

    private List<Integer> values;

    public RankingSequence() {
	values = new ArrayList<Integer>();
    }

    public RankingSequence(int length) {
	values = new ArrayList<Integer>(length);
    }

    /**
     * Adds a value at the end of the sequence.
     * 
     * @param value The integer value to be added.
     */
    public void add(int value) {
	values.add(value);

    }

    /**
     * Adds a value to a specific position of the sequence.
     * 
     * @param position Integer value of the index.
     * @param value    Integer value to be added at the position.
     */
    public void add(int position, int value) {
	values.add(position, value);

    }

    /**
     * Returns the value at a specific position.
     * 
     * @param position The position of the value.
     * @return The value at the position, returned as Integer.
     */
    public Integer get(int position) {
	return values.get(position);
    }

    /**
     * Changes the value at a specific position.
     * 
     * @param position The position of the value to be changed.
     * @param value    New integer value to be set at the position.
     */
    public void set(int position, int value) {
	values.set(position, value);

    }

    // Sorts the sequence
    /**
     * Sorts the sequence in reverse order.
     * 
     */
    public void sortValuesNonIncreasingly() {

	values.sort(Comparator.reverseOrder());

    }

    /**
     * Sorts the sequence in natural order.
     */
    public void sortValuesNonDecreasingly() {

	values.sort(Comparator.naturalOrder());

    }

    /**
     * 
     * Returns a list with the values in the sequence.
     * 
     * @return List of Integers with the copied values of the sequence.
     */
    public List<Integer> getValues() {

	List<Integer> listCopy = List.copyOf(values);

	return listCopy;
    }

    /**
     * Returns the sum of the values in the sequence.
     * 
     * @return Sum of the values in the sequence.
     */
    public int getSum() {

	return values.stream().mapToInt(s -> s).sum();
    }

    /**
     * Returns the summed distances of the values in the sequence.
     * 
     * @return The summed distances of the values in the sequence as integer.
     */
    public int getSummedDistances() {

	int sum = 0;
	int distance = 0;

	for (int i = 0; i < values.size(); i++) {
	    for (int j = i + 1; j < values.size(); j++) {
		distance = Math.abs(values.get(i) - values.get(j));
		sum = sum + distance;
	    }

	}
	return sum;

    }

    @Override
    public int hashCode() {
	return Objects.hash(values);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	RankingSequence other = (RankingSequence) obj;
	return Objects.equals(values, other.values);
    }

    public String toString() {
	return values.toString();
    }

}

package kappaMerge.operators;

/**
 * Interface for merging operators. 
 *
 */
public interface MergingOperator<T> {

    
    
  
    /**
     * Merges two equal structures and returns a merged structure.
     * 
     * @param op1	First structure.
     * @param op2	Second structure.
     * @return		Merged structure.
     */
    public T merge(T op1, T op2);
    
    
}

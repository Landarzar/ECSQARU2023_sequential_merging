package kappaMerge;

/**
 * 
 * Tasks are grouped by the type of their major premise. If the formula cannot be assigned to one the four main groups, the category 'other' is used.
 *
 */
public enum TaskGroup {
    Conditional, Biconditional, InclusiveDisjunction, ExclusiveDisjunction, Other

}

package whowhatwhere.model.criteria;

public abstract class BinaryCriteriaOperator<T, S> implements Criteria<T, S>
{
	protected Criteria<T, S> left;
	protected Criteria<T, S> right;

	public BinaryCriteriaOperator(Criteria<T, S> left, Criteria<T, S> right)
	{
		this.left = left;
		this.right = right;
	}
}

package whowhatwhere.model.criteria;

public class OrCriteria<T> extends BinaryCriteriaOperator<T, Boolean> implements Criteria<T, Boolean>
{
	public OrCriteria(Criteria<T, Boolean> left, Criteria<T, Boolean> right)
	{
		super(left, right);
	}

	@Override
	public Boolean meetCriteria(T itemToCheck)
	{
		return left.meetCriteria(itemToCheck) || right.meetCriteria(itemToCheck);
	}

	@Override
	public String getCriteriaAsText()
	{
		return "(" + left.getCriteriaAsText() + " OR " + right.getCriteriaAsText() + ")";
	}
}

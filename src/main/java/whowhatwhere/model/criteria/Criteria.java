package whowhatwhere.model.criteria;

public interface Criteria<T, S>
{
	public S meetCriteria(T itemToCheck);
	public String getCriteriaAsText();
}

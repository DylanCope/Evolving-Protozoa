package protoevo.biology;

import protoevo.env.Tank;

public abstract class EdibleCell extends Cell
{
	private static final long serialVersionUID = -5482090072120647315L;
	private final Food.Type foodType;

	public EdibleCell(float radius, Food.Type foodType, Tank tank)
	{
		super(tank);
		this.setRadius(radius);
		this.foodType = foodType;
	}

	@Override
	public boolean isEdible() {
		return true;
	}

	public Food.Type getFoodType() {
		return foodType;
	}

}

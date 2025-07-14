package org.pancakelab.model.pancakes;

import java.util.UUID;

public abstract class AbstractPancake implements PancakeRecipe {

	protected UUID orderId;

	@Override
	public UUID getOrderId() {
		return orderId;
	}

	@Override
	public void setOrderId(UUID orderId) {
		this.orderId = orderId;
	}

}

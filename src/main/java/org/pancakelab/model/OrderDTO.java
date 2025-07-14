package org.pancakelab.model;

import java.util.UUID;

public class OrderDTO {

	private final UUID id;
	private final int building;
	private final int room;

	public OrderDTO(UUID id, int building, int room) {
		this.id = id;
		this.building = building;
		this.room = room;
	}

	public static OrderDTO fromData(Order data) {
		return new OrderDTO(data.getId(), data.getBuilding(), data.getRoom());
	}

	public Order toData() {
		return new Order(building, room);
	}

	public UUID getId() {
		return id;
	}

	public int getBuilding() {
		return building;
	}

	public int getRoom() {
		return room;
	}

}

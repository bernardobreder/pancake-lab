package org.pancakelab.service;

import java.util.List;

import org.pancakelab.model.Order;
import org.pancakelab.model.pancakes.PancakeRecipe;

public class OrderLog {

	private final StringBuilder log = new StringBuilder();

	public synchronized void logAddPancake(Order order, String description, List<PancakeRecipe> pancakes) {
		log.append("Added pancake with description '%s' ".formatted(description)) //
				.append("to order %s containing %d pancakes, ".formatted(order.getId(), pancakes.size())) //
				.append("for building %d, room %d.".formatted(order.getBuilding(), order.getRoom())) //
				.append("\n");
	}

	public synchronized void logRemovePancakes(Order order, String description, int count, List<PancakeRecipe> pancakes) {
		log.append("Removed %d pancake(s) with description '%s' ".formatted(count, description)) //
				.append("from order %s now containing %d pancakes, ".formatted(order.getId(), pancakes.size())) //
				.append("for building %d, room %d.".formatted(order.getBuilding(), order.getRoom())) //
				.append("\n");
	}

	public synchronized void logCancelOrder(Order order, List<PancakeRecipe> pancakes) {
		log.append("Cancelled order %s with %d pancakes ".formatted(order.getId(), pancakes.size())) //
				.append("for building %d, room %d.".formatted(order.getBuilding(), order.getRoom())) //
				.append("\n");
	}

	public synchronized void logDeliverOrder(Order order, List<PancakeRecipe> pancakes) {
		log.append("Order %s with %d pancakes ".formatted(order.getId(), pancakes.size())) //
				.append("for building %d, room %d out for delivery.".formatted(order.getBuilding(), order.getRoom())) //
				.append("\n");
	}

}

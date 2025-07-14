package org.pancakelab.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.pancakelab.model.Order;
import org.pancakelab.model.OrderDTO;
import org.pancakelab.model.pancakes.CustomPancake;
import org.pancakelab.model.pancakes.DarkChocolatePancake;
import org.pancakelab.model.pancakes.DarkChocolateWhippedCreamHazelnutsPancake;
import org.pancakelab.model.pancakes.DarkChocolateWhippedCreamPancake;
import org.pancakelab.model.pancakes.MilkChocolateHazelnutsPancake;
import org.pancakelab.model.pancakes.MilkChocolatePancake;
import org.pancakelab.model.pancakes.PancakeIngredient;
import org.pancakelab.model.pancakes.PancakeRecipe;

public class PancakeService {

	private final OrderLog log;
	private final Set<UUID> completedOrders = new HashSet<>();
	private final Set<UUID> preparedOrders = new HashSet<>();
	private final Map<UUID, PancakeServiceEntry> entities = new ConcurrentHashMap<>();

	public PancakeService(OrderLog log) {
		this.log = log;
	}

	public OrderDTO createOrder(int building, int room) {
		var data = new Order(building, room);
		entities.put(data.getId(), new PancakeServiceEntry(data.getId(), data));
		return OrderDTO.fromData(data);
	}

	public void createCustom(UUID orderId) {
		var entry = entryOrThrow(orderId);
		synchronized (entry) {
			if (entry.custom != null) {
				throw new IllegalStateException("pancake in progress");
			}
			entry.custom = new CustomPancake();
		}
	}

	public void addIngredient(UUID orderId, PancakeIngredient ingredient) {
		var entry = entryOrThrow(orderId);
		synchronized (entry) {
			if (entry.custom == null) {
				throw new IllegalStateException("No pancake in progress");
			}
			entry.custom.addIngredient(ingredient);
		}
	}

	public void finishCustom(UUID orderId) {
		var entry = entryOrThrow(orderId);
		synchronized (entry) {
			if (entry.custom == null) {
				throw new IllegalStateException("No pancake in progress");
			}
			entry.custom.finish();
			addPancake(entry.custom, entry);
			entry.custom = null;
		}
	}

	public void addDarkChocolatePancake(UUID orderId, int count) {
		var entry = entryOrThrow(orderId);
		synchronized (entry) {
			for (int i = 0; i < count; ++i) {
				addPancake(new DarkChocolatePancake(), entry);
			}
		}
	}

	public void addDarkChocolateWhippedCreamPancake(UUID orderId, int count) {
		var entry = entryOrThrow(orderId);
		synchronized (entry) {
			for (int i = 0; i < count; ++i) {
				addPancake(new DarkChocolateWhippedCreamPancake(), entry);
			}
		}
	}

	public void addDarkChocolateWhippedCreamHazelnutsPancake(UUID orderId, int count) {
		var entry = entryOrThrow(orderId);
		synchronized (entry) {
			for (int i = 0; i < count; ++i) {
				addPancake(new DarkChocolateWhippedCreamHazelnutsPancake(), entry);
			}
		}
	}

	public void addMilkChocolatePancake(UUID orderId, int count) {
		var entry = entryOrThrow(orderId);
		synchronized (entry) {
			for (int i = 0; i < count; ++i) {
				addPancake(new MilkChocolatePancake(), entry);
			}
		}
	}

	public void addMilkChocolateHazelnutsPancake(UUID orderId, int count) {
		var entry = entryOrThrow(orderId);
		synchronized (entry) {
			for (int i = 0; i < count; ++i) {
				addPancake(new MilkChocolateHazelnutsPancake(), entry);
			}
		}
	}

	public List<String> viewOrder(UUID orderId) {
		var entry = entryOrNull(orderId);
		if (entry == null) {
			return Collections.emptyList();
		}
		synchronized (entry) {
			return entry.recipes.stream().map(PancakeRecipe::description).toList();
		}
	}

	public void removePancakes(String description, UUID orderId, int count) {
		var entry = entryOrThrow(orderId);
		synchronized (entry) {
			var removed = 0;
			for (int i = 0; removed < count && i < entry.recipes.size(); i++) {
				if (entry.recipes.get(i).description().equals(description)) {
					entry.recipes.remove(i--);
					removed++;
				}
			}
			log.logRemovePancakes(entry.order, description, removed, entry.recipes);
		}
	}

	public void cancelOrder(UUID orderId) {
		var entry = entryOrThrow(orderId);
		synchronized (entry) {
			entities.remove(orderId);
			synchronized (completedOrders) {
				completedOrders.remove(orderId);
				preparedOrders.remove(orderId);
			}

			log.logCancelOrder(entry.order, entry.recipes);
		}
	}

	public void completeOrder(UUID orderId) {
		synchronized (completedOrders) {
			completedOrders.add(orderId);
		}
	}

	public Set<UUID> listCompletedOrders() {
		synchronized (completedOrders) {
			return new HashSet<>(completedOrders);
		}
	}

	public void prepareOrder(UUID orderId) {
		var entry = entryOrThrow(orderId);
		synchronized (entry) {
			if (entry.custom != null) {
				throw new IllegalStateException("custom recipe was not finished");
			}
			synchronized (completedOrders) {
				completedOrders.remove(orderId);
				preparedOrders.add(orderId);
			}
		}
	}

	public Set<UUID> listPreparedOrders() {
		synchronized (completedOrders) {
			return new HashSet<>(preparedOrders);
		}
	}

	public DeliverOrder deliverOrder(UUID orderId) {
		var entry = entryOrThrow(orderId);
		synchronized (entry) {
			synchronized (completedOrders) {
				if (!preparedOrders.contains(orderId)) {
					return null;
				}
			}

			var pancakesToDeliver = viewOrder(orderId);
			log.logDeliverOrder(entry.order, entry.recipes);

			entities.remove(orderId);
			synchronized (completedOrders) {
				preparedOrders.remove(orderId);
			}

			return new DeliverOrder(OrderDTO.fromData(entry.order), pancakesToDeliver);
		}
	}

	private PancakeServiceEntry entryOrThrow(UUID orderId) {
		return Optional.ofNullable(entryOrNull(orderId)) //
				.orElseThrow(() -> new IllegalStateException(String.format("order %s not found", orderId)));
	}

	private PancakeServiceEntry entryOrNull(UUID orderId) {
		return orderId == null ? null : entities.get(orderId);
	}

	private void addPancake(PancakeRecipe pancake, PancakeServiceEntry entry) {
		pancake.setOrderId(entry.id);
		entry.recipes.add(pancake);

		log.logAddPancake(entry.order, pancake.description(), entry.recipes);
	}

	public record DeliverOrder(OrderDTO order, List<String> pancakesToDeliver) {
	};

	private static class PancakeServiceEntry {

		public final UUID id;
		public final Order order;
		public final List<PancakeRecipe> recipes = Collections.synchronizedList(new ArrayList<>());
		public CustomPancake custom;

		public PancakeServiceEntry(UUID id, Order order) {
			this.id = id;
			this.order = order;
		}

	}

}

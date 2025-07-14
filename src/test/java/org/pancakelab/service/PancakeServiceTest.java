package org.pancakelab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.pancakelab.model.OrderDTO;
import org.pancakelab.service.PancakeService.DeliverOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PancakeServiceTest {

	private OrderLog log;
	private PancakeService pancakeService;

	private OrderDTO order = null;

	private final static String DARK_CHOCOLATE_PANCAKE_DESCRIPTION = "Delicious pancake with dark chocolate!";
	private final static String MILK_CHOCOLATE_PANCAKE_DESCRIPTION = "Delicious pancake with milk chocolate!";
	private final static String MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION = "Delicious pancake with milk chocolate, hazelnuts!";

	@BeforeAll
	public void beforeTest() {
		log = new OrderLog();
		pancakeService = new PancakeService(log);
	}

	@Test
	@org.junit.jupiter.api.Order(10)
	public void GivenOrderDoesNotExist_WhenCreatingOrder_ThenOrderCreatedWithCorrectData_Test() {
		// setup

		// exercise
		order = pancakeService.createOrder(10, 20);

		assertEquals(10, order.getBuilding());
		assertEquals(20, order.getRoom());

		// verify

		// tear down
	}

	@Test
	@org.junit.jupiter.api.Order(20)
	public void GivenOrderExists_WhenAddingPancakes_ThenCorrectNumberOfPancakesAdded_Test() {
		// setup

		// exercise
		addPancakes();

		// verify
		List<String> ordersPancakes = pancakeService.viewOrder(order.getId());

		assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION, //
				DARK_CHOCOLATE_PANCAKE_DESCRIPTION, //
				DARK_CHOCOLATE_PANCAKE_DESCRIPTION, //
				MILK_CHOCOLATE_PANCAKE_DESCRIPTION, //
				MILK_CHOCOLATE_PANCAKE_DESCRIPTION, //
				MILK_CHOCOLATE_PANCAKE_DESCRIPTION, //
				MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION, //
				MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION, //
				MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), ordersPancakes);

		// tear down
	}

	@Test
	@org.junit.jupiter.api.Order(30)
	public void GivenPancakesExists_WhenRemovingPancakes_ThenCorrectNumberOfPancakesRemoved_Test() {
		// setup

		// exercise
		pancakeService.removePancakes(DARK_CHOCOLATE_PANCAKE_DESCRIPTION, order.getId(), 2);
		pancakeService.removePancakes(MILK_CHOCOLATE_PANCAKE_DESCRIPTION, order.getId(), 3);
		pancakeService.removePancakes(MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION, order.getId(), 1);

		// verify
		List<String> ordersPancakes = pancakeService.viewOrder(order.getId());

		assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION, //
				MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION, //
				MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), ordersPancakes);

		// tear down
	}

	@Test
	@org.junit.jupiter.api.Order(40)
	public void GivenOrderExists_WhenCompletingOrder_ThenOrderCompleted_Test() {
		// setup

		// exercise
		pancakeService.completeOrder(order.getId());

		// verify
		Set<UUID> completedOrdersOrders = pancakeService.listCompletedOrders();
		assertTrue(completedOrdersOrders.contains(order.getId()));

		// tear down
	}

	@Test
	@org.junit.jupiter.api.Order(50)
	public void GivenOrderExists_WhenPreparingOrder_ThenOrderPrepared_Test() {
		// setup

		// exercise
		pancakeService.prepareOrder(order.getId());

		// verify
		Set<UUID> completedOrders = pancakeService.listCompletedOrders();
		assertFalse(completedOrders.contains(order.getId()));

		Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
		assertTrue(preparedOrders.contains(order.getId()));

		// tear down
	}

	@Test
	@org.junit.jupiter.api.Order(60)
	public void GivenOrderExists_WhenDeliveringOrder_ThenCorrectOrderReturnedAndOrderRemovedFromTheDatabase_Test() {
		// setup
		List<String> pancakesToDeliver = pancakeService.viewOrder(order.getId());

		// exercise
		DeliverOrder deliveredOrder = pancakeService.deliverOrder(order.getId());

		// verify
		Set<UUID> completedOrders = pancakeService.listCompletedOrders();
		assertFalse(completedOrders.contains(order.getId()));

		Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
		assertFalse(preparedOrders.contains(order.getId()));

		List<String> ordersPancakes = pancakeService.viewOrder(order.getId());

		assertEquals(List.of(), ordersPancakes);
		assertEquals(order.getId(), deliveredOrder.order().getId());
		assertEquals(pancakesToDeliver, deliveredOrder.pancakesToDeliver());

		// tear down
		order = null;
	}

	@Test
	@org.junit.jupiter.api.Order(70)
	public void GivenOrderExists_WhenCancellingOrder_ThenOrderAndPancakesRemoved_Test() {
		// setup
		order = pancakeService.createOrder(10, 20);
		addPancakes();

		// exercise
		pancakeService.cancelOrder(order.getId());

		// verify
		Set<UUID> completedOrders = pancakeService.listCompletedOrders();
		assertFalse(completedOrders.contains(order.getId()));

		Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
		assertFalse(preparedOrders.contains(order.getId()));

		List<String> ordersPancakes = pancakeService.viewOrder(order.getId());

		assertEquals(List.of(), ordersPancakes);

		// tear down
	}

	private void addPancakes() {
		pancakeService.addDarkChocolatePancake(order.getId(), 3);
		pancakeService.addMilkChocolatePancake(order.getId(), 3);
		pancakeService.addMilkChocolateHazelnutsPancake(order.getId(), 3);
	}
}

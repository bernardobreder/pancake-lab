package org.pancakelab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.pancakes.PancakeIngredient;

public class PancakeServiceCustomTDDTest {

	public OrderLog log;
	public PancakeService service;

	@BeforeEach
	public void beforeTest() {
		log = new OrderLog();
		service = new PancakeService(log);
	}

	@Test
	public void testJustInit() {
		var order = service.createOrder(10, 5);
		assertNotNull(order);
	}

	@Test
	public void testCreateWithoutOrder() {
		assertThrows(IllegalStateException.class, () -> service.createCustom(UUID.randomUUID()));
	}

	@Test
	public void testCreateWithNullOrder() {
		assertThrows(IllegalStateException.class, () -> service.createCustom(null));
	}

	@Test
	public void testInitAndAdd2Recipe() {
		var order = service.createOrder(10, 5);
		var oid = order.getId();
		service.createCustom(oid);
		service.addIngredient(oid, PancakeIngredient.DARK_CHOCOLATE);
		service.addIngredient(oid, PancakeIngredient.HAZLNUTS);
		service.addIngredient(oid, PancakeIngredient.MUSTARD);
		service.finishCustom(oid);
		service.prepareOrder(oid);
		assertEquals(list("Delicious pancake with dark chocolate, hazelnuts, mustard!"), list(service.viewOrder(oid)));
		service.cancelOrder(oid);
	}

	@Test
	public void testInitAndAdd2RecipeComplete() {
		var order = service.createOrder(10, 5);
		var oid = order.getId();
		service.createCustom(oid);
		service.addIngredient(oid, PancakeIngredient.DARK_CHOCOLATE);
		service.addIngredient(oid, PancakeIngredient.HAZLNUTS);
		service.finishCustom(oid);
		assertEquals(list(), list(service.listPreparedOrders()));
		service.prepareOrder(oid);
		assertEquals(list(oid), list(service.listPreparedOrders()));
		assertEquals(list(), list(service.listCompletedOrders()));
		assertEquals(list("Delicious pancake with dark chocolate, hazelnuts!"), list(service.viewOrder(oid)));
		var deliver = service.deliverOrder(oid);
		service.completeOrder(oid);
		assertEquals(list(oid), list(service.listCompletedOrders()));
	}

	@Test
	public void testInitTwice() {
		var order = service.createOrder(10, 5);
		var oid = order.getId();
		service.createCustom(oid);
		assertThrows(IllegalStateException.class, () -> service.createCustom(oid));
	}

	@Test
	public void testInitNotPrepare() {
		var order = service.createOrder(10, 5);
		var oid = order.getId();
		service.createCustom(oid);
		service.addIngredient(oid, PancakeIngredient.DARK_CHOCOLATE);
		service.finishCustom(oid);
		assertNull(service.deliverOrder(oid));
	}

	@Test
	public void testInitAndAddWithErrorAddWithoutCreate() {
		var order = service.createOrder(10, 5);
		var oid = order.getId();
		assertThrows(IllegalStateException.class, () -> {
			service.addIngredient(oid, PancakeIngredient.DARK_CHOCOLATE);
		});
	}

	@Test
	public void testInitAndAddWithErrorFinishedWithoutCreate() {
		var order = service.createOrder(10, 5);
		var oid = order.getId();
		assertThrows(IllegalStateException.class, () -> service.finishCustom(oid));
	}

	@Test
	public void testInitAndAddWithErrorNoFinished() {
		var order = service.createOrder(10, 5);
		var oid = order.getId();
		service.createCustom(oid);
		assertThrows(IllegalStateException.class, () -> service.prepareOrder(oid));
	}

	private static String list(Object... strings) {
		return Arrays.asList(strings).stream().map(e -> e.toString()).toList().toString();
	}

	private static String list(Collection<?> strings) {
		return strings.stream().map(e -> e.toString()).toList().toString();
	}

}

package org.pancakelab.service;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

	public OrderNotFoundException(UUID id) {
		super(String.format("product %s not found", id));
	}

}

package org.pancakelab.model.pancakes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomPancake extends AbstractPancake {

	private final List<PancakeIngredient> ingredientList = Collections.synchronizedList(new ArrayList<>());

	private List<String> ingredientNames = Collections.emptyList();

	public CustomPancake() {
	}

	public void addIngredient(PancakeIngredient ingredient) {
		ingredientList.add(ingredient);
	}

	public void finish() {
		ingredientNames = ingredientList.stream().map(e -> e.getTitle()).toList();
	}

	@Override
	public List<String> ingredients() {
		if (ingredientNames == null) {
			throw new IllegalStateException("not finished");
		}
		return ingredientNames;
	}

}

package org.pancakelab.model.pancakes;

public enum PancakeIngredient {

	MILK_CHOCOLATE("milk chocolate"), //
	DARK_CHOCOLATE("dark chocolate"), //
	HAZLNUTS("hazelnuts"), //
	WHIPPED_CREAM("whipped cream"), //
	MUSTARD("mustard");

	private final String title;

	private PancakeIngredient(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

}

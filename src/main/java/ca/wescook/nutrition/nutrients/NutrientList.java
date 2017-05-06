package ca.wescook.nutrition.nutrients;

import ca.wescook.nutrition.configs.Config;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

// Maintains list of information about nutrients (name, color, icon)
// Stored client and server-side
public class NutrientList {
	private static List<NutrientJson> nutrientJson = new ArrayList<>(); // Raw deserialized data from JSON
	private static List<Nutrient> nutrients = new ArrayList<>(); // Polished nutrients list

	// Nutrient JSON
	// Data imported as object, but stored in strings
	// Run during Pre-Init
	public static void registerNutrientJson(NutrientJson nutrientJson) {
		NutrientList.nutrientJson.add(nutrientJson);
	}

	// Iterate through JSON objects
	// Run during Post-Init, so most foodItems will be in-game by now
	public static void registerNutrients() {
		for (NutrientJson nutrientRaw : nutrientJson) {
			// Copying and cleaning data
			Nutrient nutrient = new Nutrient();
			nutrient.name = nutrientRaw.name; // Localization key used in lang file
			nutrient.icon = new ItemStack(Item.getByNameOrId(nutrientRaw.icon)); // Create ItemStack used to represent icon
			nutrient.color = Integer.parseUnsignedInt("ff" + nutrientRaw.color, 16); // Convert hex string to int
			nutrient.foodOreDict = nutrientRaw.food.oredict; // Ore dicts remains as strings

			// Food - Items
			for (String itemName : nutrientRaw.food.items) {
				Item foodItem = Item.getByNameOrId(itemName);
				if (foodItem == null) // If food has valid item
					logFoodError(itemName + " is not a valid item (" + nutrient.name + ")");
				else if (!(foodItem instanceof ItemFood) && Config.enableLogging) // If item is specified as a food
					logFoodError(itemName + " is not a valid food (" + nutrient.name + ")");
				else
					nutrient.foodItems.add((ItemFood) foodItem); // Register it!
			}

			// Register nutrient
			nutrients.add(nutrient);
		}
	}

	// Return list of all nutrients
	public static List<Nutrient> get() {
		return nutrients;
	}

	// Return nutrient by name (null if not found)
	public static Nutrient get(String name) {
		for (Nutrient nutrient : nutrients) {
			if (nutrient.name.equals(name))
				return nutrient;
		}
		return null;
	}

	// This method mostly exists to simplify the if/else chain up there
	static private void logFoodError(String msg) {
		if (Config.enableLogging)
			System.out.println(msg);
	}
}
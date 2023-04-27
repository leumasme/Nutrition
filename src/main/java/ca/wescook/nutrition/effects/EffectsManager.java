package ca.wescook.nutrition.effects;

import ca.wescook.nutrition.data.PlayerDataHandler;
import ca.wescook.nutrition.nutrients.Nutrient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EffectsManager {

    // Called from EventWorldTick#PlayerTickEvent and EventEatFood#reapplyEffectsFromMilk
    public static void reapplyEffects(EntityPlayer player) {
        List<Effect> effects = removeDuplicates(getEffectsInThreshold(player));

        for (Effect effect : effects) {
            boolean ambient = (effect.particles == Effect.EnumParticleVisibility.TRANSLUCENT); // Determine if particles should be shown, and what strength
            player.addPotionEffect(new PotionEffect(effect.potion.id, 619, effect.amplifier, ambient));
        }
    }

    // Returns which effects match threshold conditions
    private static List<Effect> getEffectsInThreshold(EntityPlayer player) {
        // List of effects being turned on
        List<Effect> effectsInThreshold = new ArrayList<>();

        // Get player nutrition
        Map<Nutrient, Float> playerNutrition = PlayerDataHandler.getForPlayer(player).get();

        // Read in list of potion effects to apply
        for (Effect effect : EffectsList.get()) {

            // Apply effect based on "detect" condition
            switch (effect.detectionType) {
                // If any nutrient is within the threshold
                case ANY -> {
                    // Loop relevant nutrients
                    for (Nutrient nutrient : effect.nutrients) {
                        // If any are found within threshold
                        if (playerNutrition.get(nutrient) >= effect.minimum && playerNutrition.get(nutrient) <= effect.maximum) {
                            effectsInThreshold.add(effect); // Add effect, once
                            break;
                        }
                    }
                }

                // If the average of all nutrients is within the threshold
                case AVERAGE -> {
                    // Reset counter each new loop
                    Float total = 0f;
                    float average;

                    // Loop relevant nutrients
                    for (Nutrient nutrient : effect.nutrients)
                        total += playerNutrition.get(nutrient); // Add each value to total

                    // Divide by number of nutrients for average (division by zero check)
                    int size = effect.nutrients.size();
                    average = (size != 0) ? total / size : -1f;

                    // Check average is inside the threshold
                    if (average >= effect.minimum && average <= effect.maximum)
                        effectsInThreshold.add(effect);
                }

                // If all nutrients are within the threshold
                case ALL -> {
                    // Condition starts true, and must be triggered to fail
                    boolean allWithinThreshold = true;

                    // Loop relevant nutrients
                    for (Nutrient nutrient : effect.nutrients) {
                        if (!(playerNutrition.get(nutrient) >= effect.minimum && playerNutrition.get(nutrient) <= effect.maximum)) // If nutrient isn't within threshold
                            allWithinThreshold = false; // Fail check
                    }

                    // If check wasn't failed, set effect
                    if (allWithinThreshold)
                        effectsInThreshold.add(effect);
                }

                // For each nutrient within the threshold, the amplifier increases by one
                case CUMULATIVE -> {
                    // Reset counter each new loop
                    int cumulativeCount = 0;

                    // Loop relevant nutrients
                    for (Nutrient nutrient : effect.nutrients) {
                        // For each nutrient found within threshold
                        if (playerNutrition.get(nutrient) >= effect.minimum && playerNutrition.get(nutrient) <= effect.maximum)
                            cumulativeCount++;
                    }

                    // Save number of nutrients found as amplifier
                    // We're saving this for the entire effect, which is crazy hacky.
                    // However, it's otherwise unused, and the simplest way of storing this information.
                    effect.amplifier = (cumulativeCount * effect.cumulativeModifier) - 1;

                    // If any were found, set effect
                    if (cumulativeCount > 0) {
                        effectsInThreshold.add(effect); // Add effect, once
                    }
                }
            }
        }

        return effectsInThreshold;
    }

    // Determines highest amplifier for duplicates, and removes any others of the same type
    private static List<Effect> removeDuplicates(List<Effect> effectsInput) {
        List<Effect> effectsOutput = new ArrayList<>();
        boolean foundMatch = false;
        for (Effect effectIn : effectsInput) { // Loop through supplied effects
            for (Effect effectOut : effectsOutput) { // Loop through curated list, if it exists
                if (effectIn.potion == effectOut.potion) { // Potion types match (eg. Weakness I and Weakness II)
                    if (effectIn.amplifier > effectOut.amplifier) { // New effect has a higher amplifier
                        int listIndex = effectsOutput.indexOf(effectOut); // Get index of position in list
                        effectsOutput.add(listIndex, effectIn); // Replace entry
                    }
                    foundMatch = true;
                    break;
                }
            }

            // If potion wasn't already found, add to list
            if (!foundMatch)
                effectsOutput.add(effectIn);
        }

        return effectsOutput;
    }
}

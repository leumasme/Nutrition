package ca.wescook.nutrition.events;

import ca.wescook.nutrition.Nutrition;
import ca.wescook.nutrition.gui.GuiButtonNutrition;
import ca.wescook.nutrition.gui.IGuiContainerGetters;
import ca.wescook.nutrition.gui.ModGuiHandler;
import ca.wescook.nutrition.utility.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EventNutritionButton {

    private GuiButtonNutrition buttonNutrition;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void guiOpen(GuiScreenEvent.InitGuiEvent.Post event) {
        // If any inventory except player inventory is opened, get out
        GuiScreen gui = event.gui;
        if (!(gui instanceof GuiInventory))
            return;

        // Get button position
        int[] pos = calculateButtonPosition(gui);
        int x = pos[0];
        int y = pos[1];

        // Create button
        buttonNutrition = new GuiButtonNutrition(x, y);
        event.buttonList.add(buttonNutrition);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void guiButtonClick(GuiScreenEvent.ActionPerformedEvent.Post event) {
        // Only run on GuiInventory
        if (!(event.gui instanceof GuiInventory))
            return;

        // If nutrition button is clicked
        if (event.button.equals(buttonNutrition)) {
            // Get data
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            World world = Minecraft.getMinecraft().theWorld;

            // Open GUI
            player.openGui(Nutrition.instance, ModGuiHandler.NUTRITION_GUI_ID, world, (int) player.posX, (int) player.posY, (int) player.posZ);
        } else {
            // Presumably recipe book button was clicked - recalculate nutrition button position
            int[] pos = calculateButtonPosition(event.gui);
            int xPosition = pos[0];
            int yPosition = pos[1];
            buttonNutrition.setPosition(xPosition, yPosition);
        }
    }

    // Return array [x,y] of button coordinates
    @SideOnly(Side.CLIENT)
    private int[] calculateButtonPosition(GuiScreen gui) {
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;

        // Get bounding box of origin
        if (Config.buttonOrigin.equals("screen")) {
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            width = scaledResolution.getScaledWidth();
            height = scaledResolution.getScaledHeight();
        } else if (Config.buttonOrigin.equals("gui") && gui instanceof GuiInventory) {
            width = ((IGuiContainerGetters) (Object) gui).getXSize();
            height = ((IGuiContainerGetters) (Object) gui).getYSize();
        }

        // Calculate anchor position from origin (eg. x/y pixels at right side of gui)
        // The x/y is still relative to the top/left corner of the screen at this point
        switch (Config.buttonAnchor) {
            case "top": x = width / 2; y = 0; break;
            case "right": x = width; y = height / 2; break;
            case "bottom": x = width / 2; y = height; break;
            case "left": x = 0; y = height / 2; break;
            case "top-left": x = 0; y = 0; break;
            case "top-right": x = width; y = 0; break;
            case "bottom-right": x = width; y = height; break;
            case "bottom-left": x = 0; y = height; break;
            case "center": x = width / 2; y = height / 2; break;
        }

        // If origin=gui, add the offset to the button's position
        if (Config.buttonOrigin.equals("gui") && gui instanceof GuiInventory) {
            x += ((IGuiContainerGetters) (Object) gui).getGuiLeft();
            y += ((IGuiContainerGetters) (Object) gui).getGuiTop();
        }

        // Then add the offset as defined in the config file
        x += Config.buttonXPosition;
        y += Config.buttonYPosition;

        return new int[]{x, y};
    }
}

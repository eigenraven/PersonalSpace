package xyz.kubasz.personalspace.gui;

import codechicken.lib.gui.GuiDraw;
import cpw.mods.fml.client.config.GuiButtonExt;
import cpw.mods.fml.client.config.GuiSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;
import org.lwjgl.opengl.GL11;
import xyz.kubasz.personalspace.CommonProxy;
import xyz.kubasz.personalspace.Config;
import xyz.kubasz.personalspace.block.PortalTileEntity;
import xyz.kubasz.personalspace.net.Packets;
import xyz.kubasz.personalspace.world.DimensionConfig;

import java.util.ArrayList;
import java.util.List;

public class GuiEditWorld extends GuiScreen {

    public PortalTileEntity tile;

    int xSize, ySize, guiLeft, guiTop;

    DimensionConfig desiredConfig = new DimensionConfig();
    GuiSlider skyRed;
    GuiSlider skyGreen;
    GuiSlider skyBlue;
    GuiSlider starBrightness;
    GuiTextField biome;
    GuiButton generateTrees;
    GuiButton generateVegetation;
    GuiButton save;
    GuiTextField presetEntry;
    List<GuiButton> presetButtons = new ArrayList<>();
    List<GuiTextField> textFields = new ArrayList<>();
    String voidPresetName = "gui.personalWorld.voidWorld";

    public GuiEditWorld(PortalTileEntity tile) {
        super();
        this.tile = tile;
        int targetDimId = 0;
        DimensionConfig currentDimConfig = DimensionConfig.getForDimension(tile.getWorldObj().provider.dimensionId, true);
        if (currentDimConfig != null) {
            this.desiredConfig.copyFrom(currentDimConfig, false, true, true);
        } else if (tile.active && tile.targetDimId > 1) {
            DimensionConfig currentConfig = CommonProxy.getDimensionConfigObjects(true).get(tile.targetDimId);
            if (currentConfig != null) {
                this.desiredConfig.copyFrom(currentConfig, false, true, true);
            } else {
                this.desiredConfig.setAllowGenerationChanges(true);
            }
        } else {
            this.desiredConfig.setAllowGenerationChanges(true);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (GuiTextField tf : textFields) {
            tf.updateCursorCounter();
        }
        if (!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead) {
            this.mc.thePlayer.closeScreen();
        }
    }

    private void addButton(GuiButton btn) {
        this.buttonList.add(btn);
        this.ySize += btn.height + 6;
    }

    @Override
    public void initGui() {
        this.xSize = 400;
        this.ySize = 8;

        this.skyRed = new GuiSlider(0, 48, this.ySize, 200, 20, I18n.format("gui.personalWorld.skyColor.red"), "", 0.0F, 255.0F, ((desiredConfig.getSkyColor() >> 16) & 0xFF), false, true);
        addButton(this.skyRed);
        this.skyGreen = new GuiSlider(1, 48, this.ySize, 200, 20, I18n.format("gui.personalWorld.skyColor.green"), "", 0.0F, 255.0F, ((desiredConfig.getSkyColor() >> 8) & 0xFF), false, true);
        addButton(this.skyGreen);
        this.skyBlue = new GuiSlider(2, 48, this.ySize, 200, 20, I18n.format("gui.personalWorld.skyColor.blue"), "", 0.0F, 255.0F, (desiredConfig.getSkyColor() & 0xFF), false, true);
        addButton(this.skyBlue);

        this.starBrightness = new GuiSlider(3, 48, this.ySize, 200, 20, I18n.format("gui.personalWorld.starBrightness"), "", 0.0F, 1.0F, desiredConfig.getStarBrightness(), true, true);
        addButton(this.starBrightness);

        GuiButtonExt lblBiome = new GuiButtonExt(-1, 48, this.ySize, 200, 20, I18n.format("gui.personalWorld.biome"));
        lblBiome.enabled = false;
        addButton(lblBiome);
        this.biome = new GuiTextField(fontRendererObj, 8, this.ySize, 400, 20);
        this.biome.setMaxStringLength(4096);
        this.biome.setText(desiredConfig.getBiomeId());
        this.textFields.add(this.biome);
        this.ySize += 26;
        this.generateTrees = new GuiButtonExt(4, 8, this.ySize, 200, 20, "trees");
        addButton(this.generateTrees);
        this.generateVegetation = new GuiButtonExt(5, 8, this.ySize, 200, 20, "veg");
        addButton(this.generateVegetation);

        voidPresetName = I18n.format("gui.personalWorld.voidWorld");

        this.presetEntry = new GuiTextField(fontRendererObj, 8, this.ySize, 400, 20);
        this.presetEntry.setMaxStringLength(4096);
        this.presetEntry.setText(desiredConfig.getLayersAsString());
        if (this.presetEntry.getText().isEmpty()) {
            this.presetEntry.setText(voidPresetName);
        }
        this.textFields.add(this.presetEntry);
        this.ySize += 26;

        int i = 9;
        for (String preset : Config.defaultPresets) {
            if (preset.isEmpty()) {
                preset = voidPresetName;
            }
            presetButtons.add(new GuiButtonExt(++i, 8, this.ySize, 400, 20, preset));
            addButton(presetButtons.get(presetButtons.size() - 1));
        }

        this.save = new GuiButton(6, 8, this.ySize, 200, 20, I18n.format("gui.done"));
        addButton(save);

        this.ySize += 8;
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        GL11.glPushMatrix();
        GL11.glTranslatef(this.guiLeft, this.guiTop, 0.0f);
        mouseX -= guiLeft;
        mouseY -= guiTop;
        int skyR = MathHelper.clamp_int(skyRed.getValueInt(), 0, 255);
        int skyG = MathHelper.clamp_int(skyGreen.getValueInt(), 0, 255);
        int skyB = MathHelper.clamp_int(skyBlue.getValueInt(), 0, 255);
        desiredConfig.setSkyColor((skyR << 16) | (skyG << 8) | skyB);
        desiredConfig.setStarBrightness((float) this.starBrightness.getValue());
        this.generateTrees.displayString = I18n.format(desiredConfig.isGeneratingTrees() ? "gui.personalWorld.trees.on" : "gui.personalWorld.trees.off");
        this.generateVegetation.displayString = I18n.format(desiredConfig.isGeneratingVegetation() ? "gui.personalWorld.vegetation.on" : "gui.personalWorld.vegetation.off");
        boolean generationEnabled = desiredConfig.getAllowGenerationChanges();
        this.generateTrees.enabled = generationEnabled;
        this.generateVegetation.enabled = generationEnabled;
        for (GuiButton presetBtn : presetButtons) {
            presetBtn.enabled = generationEnabled;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.presetEntry.setEnabled(generationEnabled);
        String actualText = this.presetEntry.getText();
        if (voidPresetName.equals(actualText)) {
            actualText = "";
        }
        if (!generationEnabled) {
            this.presetEntry.setTextColor(0x909090);
        } else if (!DimensionConfig.PRESET_VALIDATION_PATTERN.matcher(actualText).matches()) {
            this.presetEntry.setTextColor(0xFF0000);
        } else if (!DimensionConfig.canUseLayers(actualText)) {
            this.presetEntry.setTextColor(0xFFFF00);
        } else {
            this.presetEntry.setTextColor(0xA0FFA0);
            this.desiredConfig.setLayers(actualText);
        }
        this.desiredConfig.setBiomeId(this.biome.getText());
        if (!generationEnabled) {
            this.biome.setTextColor(0x909090);
        } else if (!this.desiredConfig.getBiomeId().equalsIgnoreCase(BiomeGenBase.getBiome(this.desiredConfig.getRawBiomeId()).biomeName)) {
            this.biome.setTextColor(0xFF0000);
        } else if (!DimensionConfig.canUseBiome(this.desiredConfig.getBiomeId())) {
            this.biome.setTextColor(0xFFFF00);
        } else {
            this.biome.setTextColor(0xA0FFA0);
        }
        for (GuiTextField tf : textFields) {
            tf.drawTextBox();
        }
        GuiDraw.gui.setZLevel(99.f);
        GuiDraw.drawRect(8, 8, 32, 72, 0xFF000000 | desiredConfig.getSkyColor());
        int starBright = MathHelper.clamp_int((int) (desiredConfig.getStarBrightness() * 255.0F), 0, 255);
        GuiDraw.drawRect(8, this.starBrightness.yPosition, 32, 20, 0xFF000000 | starBright * 0x010101);
        GL11.glPopMatrix();
    }

    @Override
    protected void keyTyped(char character, int key) {
        super.keyTyped(character, key);
        for (GuiTextField tf : textFields) {
            if (tf.isFocused()) {
                tf.textboxKeyTyped(character, key);
            }
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        x -= guiLeft;
        y -= guiTop;
        super.mouseClicked(x, y, button);
        for (GuiTextField tf : textFields) {
            tf.mouseClicked(x, y, button);
        }
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        x -= guiLeft;
        y -= guiTop;
        super.mouseMovedOrUp(x, y, button);
    }

    @Override
    protected void mouseClickMove(int x, int y, int lastBtn, long timeDragged) {
        x -= guiLeft;
        y -= guiTop;
        super.mouseClickMove(x, y, lastBtn, timeDragged);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == this.save) {
            Packets.INSTANCE.sendChangeWorldSettings(this.tile, desiredConfig).sendToServer();
            Minecraft.getMinecraft().displayGuiScreen(null);
        } else if (button == this.generateTrees) {
            desiredConfig.setGeneratingTrees(!desiredConfig.isGeneratingTrees());
        } else if (button == this.generateVegetation) {
            desiredConfig.setGeneratingVegetation(!desiredConfig.isGeneratingVegetation());
        } else if (button.id >= 9) {
            this.presetEntry.setText(button.displayString);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}

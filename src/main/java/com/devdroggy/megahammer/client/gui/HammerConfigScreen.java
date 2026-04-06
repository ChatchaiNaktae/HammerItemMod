package com.devdroggy.megahammer.client.gui;

import com.devdroggy.megahammer.item.HammerItem;
import com.devdroggy.megahammer.network.HammerConfigPacket;
import com.devdroggy.megahammer.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class HammerConfigScreen extends Screen {
    private final ItemStack hammer;
    private int up, down, left, right, depth;

    private static final int MAX_RANGE = 3;
    private static final int MAX_DEPTH = 4;

    public HammerConfigScreen(ItemStack hammer) {
        super(Component.literal("Mega Hammer Config"));
        this.hammer = hammer;
        if (hammer.getItem() instanceof HammerItem hammerItem) {
            this.up = hammerItem.getHammerRange(hammer, "RangeUp", 1);
            this.down = hammerItem.getHammerRange(hammer, "RangeDown", 1);
            this.left = hammerItem.getHammerRange(hammer, "RangeLeft", 1);
            this.right = hammerItem.getHammerRange(hammer, "RangeRight", 1);
            this.depth = hammerItem.getHammerRange(hammer, "RangeDepth", 0);
        }
    }

    @Override
    protected void init() {
        super.init();
        int cX = this.width / 2;
        int cY = this.height / 2;

        // --- ส่วนควบคุมขนาด (เหมือนเดิม) ---
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> changeValue("up", -1)).bounds(cX - 50, cY - 55, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> changeValue("up", 1)).bounds(cX + 30, cY - 55, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> changeValue("down", -1)).bounds(cX - 50, cY + 35, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> changeValue("down", 1)).bounds(cX + 30, cY + 35, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> changeValue("depth", -1)).bounds(cX - 50, cY + 80, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> changeValue("depth", 1)).bounds(cX + 30, cY + 80, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> changeValue("left", -1)).bounds(cX - 150, cY - 10, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> changeValue("left", 1)).bounds(cX - 70, cY - 10, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> changeValue("right", -1)).bounds(cX + 50, cY - 10, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> changeValue("right", 1)).bounds(cX + 130, cY - 10, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Revert"), b -> resetValues()).bounds(cX + 65, cY + 80, 60, 20).build());

        // --- NEW: ปุ่มสลับไปหน้าจอ Upgrades ---
        this.addRenderableWidget(Button.builder(Component.literal("Upgrades..."), b -> {
            // เมื่อกดปุ่มนี้ จะสั่งเปิดหน้าจอใหม่ และส่งหน้าจอตัวเอง (this) ไปด้วยเผื่อกด Back
            this.minecraft.setScreen(new HammerUpgradeScreen(this.hammer, this));
        }).bounds(cX - 100, cY - 110, 200, 20).build());
    }

    private void changeValue(String type, int amount) {
        switch (type) {
            case "up" -> up = Math.min(MAX_RANGE, Math.max(0, up + amount));
            case "down" -> down = Math.min(MAX_RANGE, Math.max(0, down + amount));
            case "left" -> left = Math.min(MAX_RANGE, Math.max(0, left + amount));
            case "right" -> right = Math.min(MAX_RANGE, Math.max(0, right + amount));
            case "depth" -> depth = Math.min(MAX_DEPTH, Math.max(0, depth + amount));
        }
        ModMessages.sendToServer(new HammerConfigPacket(up, down, left, right, depth));
    }

    private void resetValues() {
        this.up = 1; this.down = 1; this.left = 1; this.right = 1; this.depth = 0;
        ModMessages.sendToServer(new HammerConfigPacket(up, down, left, right, depth));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int cX = this.width / 2; int cY = this.height / 2;

        guiGraphics.drawCenteredString(this.font, "Up: " + up, cX, cY - 50, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "Down: " + down, cX, cY + 40, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "Depth: " + depth, cX, cY + 85, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "Left: " + left, cX - 110, cY - 5, 0xFFFF55);
        guiGraphics.drawCenteredString(this.font, "Right: " + right, cX + 110, cY - 5, 0xFFFF55);

        int totalWidth = left + right + 1;
        int totalHeight = up + down + 1;
        String sizeText = totalWidth + " x " + totalHeight + " x " + (depth + 1);
        guiGraphics.drawCenteredString(this.font, sizeText, cX, cY + 110, 0x55FF55);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
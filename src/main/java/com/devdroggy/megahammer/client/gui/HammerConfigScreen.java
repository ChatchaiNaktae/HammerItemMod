package com.devdroggy.megahammer.client.gui;

import com.devdroggy.megahammer.item.HammerItem;
import com.devdroggy.megahammer.network.HammerConfigPacket;
import com.devdroggy.megahammer.network.HammerUpgradePacket;
import com.devdroggy.megahammer.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class HammerConfigScreen extends Screen {
    private final ItemStack hammer;
    private int up, down, left, right, depth;
    private int smeltState, magnetState, voidState, durabilityState;

    private boolean isUpgradeListOpen = false; // สถานะสำหรับเปิด/ปิดเมนูอัปเกรด
    private final List<Button> upgradeButtons = new ArrayList<>(); // List เก็บปุ่มย่อย

    private final List<ItemStack> smeltableList = List.of(
            new ItemStack(Items.RAW_IRON), new ItemStack(Items.RAW_GOLD),
            new ItemStack(Items.RAW_COPPER), new ItemStack(Items.COBBLESTONE),
            new ItemStack(Items.ANCIENT_DEBRIS)
    );

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

            this.smeltState = hammerItem.getHammerRange(hammer, "AutoSmelt", 0);
            this.magnetState = hammerItem.getHammerRange(hammer, "Magnet", 0);
            this.voidState = hammerItem.getHammerRange(hammer, "Void", 0);
            this.durabilityState = hammerItem.getHammerRange(hammer, "Durability", 0);
        }
    }

    @Override
    protected void init() {
        super.init();
        int cX = this.width / 2;
        int cY = this.height / 2;
        upgradeButtons.clear(); // ล้าง List ปุ่มย่อยทิ้งก่อนทุกครั้งที่โหลดจอ

        // --- ส่วนควบคุมขนาด ---
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

        // --- ระบบ Dropdown ใหม่ ---
        int dropdownX = cX - 100;
        int dropdownY = cY - 110;

        // 1. ปุ่มหลัก "Upgrade Lists"
        this.addRenderableWidget(Button.builder(Component.literal(isUpgradeListOpen ? "v Close Upgrades v" : "> Open Upgrade Lists <"), b -> {
            isUpgradeListOpen = !isUpgradeListOpen;
            // วน loop Foreach เพื่อสลับสถานะการมองเห็นของปุ่มย่อยทั้งหมด!
            for (Button btn : upgradeButtons) {
                btn.visible = isUpgradeListOpen;
            }
        }).bounds(dropdownX, dropdownY, 200, 20).build());

        // 2. เตรียมปุ่มย่อย (ตั้งค่า yOffset ให้ต่อแถวเรียงลงมา)
        int yOffset = 22; // ขยับลงทีละ 22 พิกเซล
        upgradeButtons.add(createUpgradeBtn(0, smeltState, "Auto-Smelt", dropdownX, dropdownY + yOffset)); yOffset += 22;
        upgradeButtons.add(createUpgradeBtn(1, magnetState, "Magnet", dropdownX, dropdownY + yOffset)); yOffset += 22;
        upgradeButtons.add(createUpgradeBtn(2, voidState, "Void Junk", dropdownX, dropdownY + yOffset)); yOffset += 22;
        upgradeButtons.add(createUpgradeBtn(3, durabilityState, "Unbreakable", dropdownX, dropdownY + yOffset));

        // 3. นำปุ่มย่อยทั้งหมดไปลงทะเบียนในหน้าจอ และตั้งค่าเริ่มต้นให้มองไม่เห็น
        for (Button btn : upgradeButtons) {
            btn.visible = false; // เริ่มต้นให้ซ่อนไว้
            this.addRenderableWidget(btn);
        }
    }

    // แก้ไขฟังก์ชันให้ "สร้าง" ปุ่ม (Create) แทนการ "เพิ่ม" ทันที เพื่อเก็บไว้ใน List ได้
    private Button createUpgradeBtn(int id, int state, String name, int x, int y) {
        String text = state == 0 ? "Unlock " + name + " (30 Lvl)" : (state == 1 ? name + ": OFF" : name + ": ON");
        return Button.builder(Component.literal(text), b -> {
            ModMessages.sendToServer(new HammerUpgradePacket(id, state == 0 ? 0 : 1));
            this.minecraft.setScreen(null); // ปิดหน้าจอเพื่อให้ Refresh
        }).bounds(x, y, 200, 20).build();
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
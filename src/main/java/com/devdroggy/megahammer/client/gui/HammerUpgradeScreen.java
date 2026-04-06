package com.devdroggy.megahammer.client.gui;

import com.devdroggy.megahammer.config.ModConfig;
import com.devdroggy.megahammer.item.HammerItem;
import com.devdroggy.megahammer.network.HammerUpgradePacket;
import com.devdroggy.megahammer.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class HammerUpgradeScreen extends Screen {
    private final ItemStack hammer;
    private final Screen parentScreen; // ตัวแปรเก็บหน้าจอเดิมเอาไว้กดย้อนกลับ
    private int smeltState, magnetState, voidState, durabilityState;

    // รายการแร่ตัวอย่างสำหรับโชว์
    private final List<ItemStack> smeltableList = List.of(
            new ItemStack(Items.RAW_IRON), new ItemStack(Items.RAW_GOLD),
            new ItemStack(Items.RAW_COPPER), new ItemStack(Items.COBBLESTONE),
            new ItemStack(Items.ANCIENT_DEBRIS)
    );

    public HammerUpgradeScreen(ItemStack hammer, Screen parentScreen) {
        super(Component.literal("Mega Hammer Upgrades"));
        this.hammer = hammer;
        this.parentScreen = parentScreen;

        if (hammer.getItem() instanceof HammerItem hammerItem) {
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

        // วางปุ่มไล่ระดับแกน Y ลงมาทีละอัน
        this.addRenderableWidget(createUpgradeBtn(0, smeltState, "Auto-Smelt", cX - 100, cY - 85));

        // (เว้นพื้นที่ cY - 60 ไว้สำหรับวาดรูปแร่)

        this.addRenderableWidget(createUpgradeBtn(1, magnetState, "Magnet", cX - 100, cY - 25));
        this.addRenderableWidget(createUpgradeBtn(2, voidState, "Void Junk", cX - 100, cY));
        this.addRenderableWidget(createUpgradeBtn(3, durabilityState, "Unbreakable", cX - 100, cY + 25));

        // ปุ่ม Done สำหรับกดย้อนกลับไปหน้าเดิม
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            this.minecraft.setScreen(parentScreen);
        }).bounds(cX - 100, cY + 65, 200, 20).build());
    }

    private Button createUpgradeBtn(int id, int state, String name, int x, int y) {
        int cost = ModConfig.UPGRADE_XP_COST.get();

        String text = state == 0 ? "Unlock " + name + " (" + cost + " Lvl)" : (state == 1 ? name + ": OFF" : name + ": ON");
        return Button.builder(Component.literal(text), b -> {
            ModMessages.sendToServer(new HammerUpgradePacket(id, state == 0 ? 0 : 1));
            this.minecraft.setScreen(null);
        }).bounds(x, y, 200, 20).build();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int cX = this.width / 2;
        int cY = this.height / 2;

        // ชื่อหัวข้อหน้าจอ
        guiGraphics.drawCenteredString(this.font, this.title, cX, cY - 110, 0xFFFFFF);

        // วาดกล่องโชว์แร่ที่จะถูกเผา (จัดให้อยู่ใต้ปุ่ม Auto-Smelt พอดีเป๊ะ)
        int startX = cX - ((smeltableList.size() * 18) / 2);
        int startY = cY - 60;

        // วาดกรอบสีดำโปร่งแสงรองพื้นหลัง
        guiGraphics.fill(startX - 2, startY - 2, startX + (smeltableList.size() * 18) + 2, startY + 18, 0x66000000);

        int offset = 0;
        for (ItemStack item : smeltableList) {
            guiGraphics.renderItem(item, startX + offset, startY);
            offset += 18;
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
package pl.armorviewer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

public class ArmorScreen extends Screen {

	private static final EquipmentSlot[] SLOTS = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	private static final Map<String, String> SET_NAMES = new HashMap<>();

	static {
		SET_NAMES.put("leather", "Skorzany");
		SET_NAMES.put("chainmail", "Kolczy");
		SET_NAMES.put("iron", "Zelazny");
		SET_NAMES.put("golden", "Zloty");
		SET_NAMES.put("diamond", "Diamentowy");
		SET_NAMES.put("netherite", "Netherytowy");
		SET_NAMES.put("turtle", "Zolwia skorupa");
	}

	private static final int PANEL_W = 220;
	private static final int PANEL_H = 230;
	private static final int BUTTONS_W = 150;
	private static final int GAP = 10;

	private final ArmorConfig cfg = ArmorConfig.get();

	public ArmorScreen() {
		super(Text.literal("Armor Viewer"));
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private int panelLeft() {
		return this.width / 2 - (PANEL_W + GAP + BUTTONS_W) / 2;
	}

	private int panelTop() {
		return this.height / 2 - PANEL_H / 2;
	}

	@Override
	protected void init() {
		int bx = panelLeft() + PANEL_W + GAP;
		int by = panelTop();
		int h = 20;
		int step = 24;

		addDrawableChild(ButtonWidget.builder(modelLabel(), b -> {
			cfg.showModel = !cfg.showModel;
			b.setMessage(modelLabel());
		}).dimensions(bx, by, BUTTONS_W, h).build());

		addDrawableChild(ButtonWidget.builder(nickLabel(), b -> {
			cfg.showNick = !cfg.showNick;
			b.setMessage(nickLabel());
		}).dimensions(bx, by + step, BUTTONS_W, h).build());

		addDrawableChild(ButtonWidget.builder(durabilityLabel(), b -> {
			cfg.durabilityMode = (cfg.durabilityMode + 1) % ArmorConfig.DURABILITY_MODES.length;
			b.setMessage(durabilityLabel());
		}).dimensions(bx, by + step * 2, BUTTONS_W, h).build());

		addDrawableChild(ButtonWidget.builder(setLabel(), b -> {
			cfg.showSet = !cfg.showSet;
			b.setMessage(setLabel());
		}).dimensions(bx, by + step * 3, BUTTONS_W, h).build());

		addDrawableChild(ButtonWidget.builder(colorLabel(), b -> {
			cfg.panelColor = (cfg.panelColor + 1) % ArmorConfig.PANEL_COLORS.length;
			b.setMessage(colorLabel());
		}).dimensions(bx, by + step * 4, BUTTONS_W, h).build());

		addDrawableChild(ButtonWidget.builder(sizeLabel(), b -> {
			cfg.modelSize = (cfg.modelSize + 1) % ArmorConfig.SIZES.length;
			b.setMessage(sizeLabel());
		}).dimensions(bx, by + step * 5, BUTTONS_W, h).build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Resetuj ustawienia"), b -> {
			cfg.reset();
			this.clearAndInit();
		}).dimensions(bx, by + step * 6, BUTTONS_W, h).build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Gotowe"), b -> this.close())
				.dimensions(bx, by + step * 7 + 8, BUTTONS_W, h).build());
	}

	private Text modelLabel() {
		return Text.literal("Model gracza: " + (cfg.showModel ? "TAK" : "NIE"));
	}

	private Text nickLabel() {
		return Text.literal("Nick: " + (cfg.showNick ? "TAK" : "NIE"));
	}

	private Text durabilityLabel() {
		return Text.literal("Wytrzymalosc: " + ArmorConfig.DURABILITY_MODES[cfg.durabilityMode]);
	}

	private Text setLabel() {
		return Text.literal("Nazwa seta: " + (cfg.showSet ? "TAK" : "NIE"));
	}

	private Text colorLabel() {
		return Text.literal("Tlo: " + ArmorConfig.PANEL_COLOR_NAMES[cfg.panelColor]);
	}

	private Text sizeLabel() {
		return Text.literal("Rozmiar modelu: " + ArmorConfig.SIZE_NAMES[cfg.modelSize]);
	}

	@Override
	public void removed() {
		ArmorConfig.save();
		super.removed();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		MinecraftClient mc = MinecraftClient.getInstance();
		ClientPlayerEntity player = mc.player;
		if (player == null) {
			return;
		}

		int left = panelLeft();
		int top = panelTop();
		int cx = left + PANEL_W / 2;

		// Tlo panelu
		context.fill(left, top, left + PANEL_W, top + PANEL_H, ArmorConfig.PANEL_COLORS[cfg.panelColor]);
		context.drawBorder(left, top, PANEL_W, PANEL_H, 0xFF8A8A8A);

		// Zbroja nad nickiem
		int slotSize = 18;
		int gap = 6;
		int rowW = SLOTS.length * slotSize + (SLOTS.length - 1) * gap;
		int rowX = cx - rowW / 2;
		int rowY = top + 14;

		for (int i = 0; i < SLOTS.length; i++) {
			ItemStack stack = player.getEquippedStack(SLOTS[i]);
			int x = rowX + i * (slotSize + gap);

			context.fill(x - 1, rowY - 1, x + slotSize - 1, rowY + slotSize - 1, 0x80404040);
			if (!stack.isEmpty()) {
				context.drawItem(stack, x, rowY);
				if (cfg.durabilityMode != 2) {
					context.drawStackOverlay(this.textRenderer, stack, x, rowY);
				}
			}

			if (cfg.durabilityMode != 2) {
				String dur;
				int color;
				if (stack.isEmpty()) {
					dur = "-";
					color = 0xFF777777;
				} else if (stack.isDamageable()) {
					int remaining = stack.getMaxDamage() - stack.getDamage();
					if (cfg.durabilityMode == 0) {
						dur = Math.round(remaining * 100f / stack.getMaxDamage()) + "%";
					} else {
						dur = String.valueOf(remaining);
					}
					color = 0xFF000000 | stack.getItemBarColor();
				} else {
					dur = "OK";
					color = 0xFFFFFFFF;
				}
				context.drawCenteredTextWithShadow(this.textRenderer, dur, x + slotSize / 2 - 1, rowY + slotSize + 3, color);
			}
		}

		int nextY = rowY + slotSize + 18;

		// Nick pod zbroja
		if (cfg.showNick) {
			context.drawCenteredTextWithShadow(
					this.textRenderer,
					Text.literal(player.getName().getString()).formatted(Formatting.BOLD),
					cx, nextY, 0xFFFFFFFF
			);
		}
		nextY += 14;

		// Model gracza
		if (cfg.showModel) {
			InventoryScreen.drawEntity(context, left + 10, nextY, left + PANEL_W - 10, nextY + 120,
					ArmorConfig.SIZES[cfg.modelSize], 0.0625f, mouseX, mouseY, player);
		}

		// Set
		if (cfg.showSet) {
			context.drawCenteredTextWithShadow(this.textRenderer, detectSet(player), cx, top + PANEL_H - 14, 0xFFFFD700);
		}
	}

	private Text detectSet(ClientPlayerEntity player) {
		String material = null;
		int pieces = 0;
		boolean mixed = false;

		for (EquipmentSlot slot : SLOTS) {
			ItemStack stack = player.getEquippedStack(slot);
			if (stack.isEmpty()) {
				continue;
			}
			pieces++;
			String path = Registries.ITEM.getId(stack.getItem()).getPath();
			int idx = path.lastIndexOf('_');
			String mat = idx > 0 ? path.substring(0, idx) : path;
			if (material == null) {
				material = mat;
			} else if (!material.equals(mat)) {
				mixed = true;
			}
		}

		if (pieces == 0) {
			return Text.literal("Set: brak zbroi");
		}
		if (mixed) {
			return Text.literal("Set: mieszany (" + pieces + "/4)");
		}
		String name = SET_NAMES.getOrDefault(material, material);
		if (pieces == 4) {
			return Text.literal("Set: " + name + " (pelny)");
		}
		return Text.literal("Set: " + name + " (" + pieces + "/4)");
	}
}

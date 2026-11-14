package pl.armorviewer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ArmorViewerClient implements ClientModInitializer {

	private static KeyBinding openKey;

	@Override
	public void onInitializeClient() {
		ArmorConfig.load();

		openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.armorviewer.open",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_O,
				"category.armorviewer"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openKey.wasPressed()) {
				if (client.player != null && client.currentScreen == null) {
					client.setScreen(new ArmorScreen());
				}
			}
		});
	}
}

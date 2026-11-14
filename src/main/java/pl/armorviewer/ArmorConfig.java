package pl.armorviewer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArmorConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("armorviewer.json");

	public static final int[] PANEL_COLORS = {0xCC101010, 0xCC0B1F3A, 0xCC1F0B3A, 0xCC0B3A1F, 0xCC3A0B0B, 0x00000000};
	public static final String[] PANEL_COLOR_NAMES = {"Czarny", "Niebieski", "Fioletowy", "Zielony", "Czerwony", "Przezroczysty"};
	public static final String[] SIZE_NAMES = {"Maly", "Sredni", "Duzy"};
	public static final int[] SIZES = {35, 45, 55};
	public static final String[] DURABILITY_MODES = {"Procenty", "Liczba", "Wylaczona"};

	public boolean showModel = true;
	public boolean showNick = true;
	public boolean showSet = true;
	public int durabilityMode = 0;
	public int panelColor = 0;
	public int modelSize = 1;

	private static ArmorConfig instance = new ArmorConfig();

	public static ArmorConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(FILE)) {
			try (Reader reader = Files.newBufferedReader(FILE)) {
				ArmorConfig loaded = GSON.fromJson(reader, ArmorConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		instance.clamp();
	}

	public static void save() {
		try (Writer writer = Files.newBufferedWriter(FILE)) {
			GSON.toJson(instance, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reset() {
		ArmorConfig d = new ArmorConfig();
		showModel = d.showModel;
		showNick = d.showNick;
		showSet = d.showSet;
		durabilityMode = d.durabilityMode;
		panelColor = d.panelColor;
		modelSize = d.modelSize;
	}

	private void clamp() {
		durabilityMode = Math.floorMod(durabilityMode, DURABILITY_MODES.length);
		panelColor = Math.floorMod(panelColor, PANEL_COLORS.length);
		modelSize = Math.floorMod(modelSize, SIZES.length);
	}
}

import com.google.common.base.Objects;
import com.mojang.authlib.GameProfile;
import eu.the5zig.mod.MinecraftFactory;
import eu.the5zig.mod.asm.Names;
import eu.the5zig.mod.asm.Transformer;
import eu.the5zig.mod.gui.elements.IButton;
import eu.the5zig.mod.gui.elements.ITextfield;
import eu.the5zig.mod.manager.SearchEntry;
import eu.the5zig.util.Callback;
import io.netty.buffer.ByteBuf;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class ClassProxy {

	private static final Field byteBuf;
	private static Field buttonList;
	private static final Field serverList;

	private static boolean tryFix = false;

	private static final UUID SPRINT_MODIFIER_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");

	private ClassProxy() {
	}

	static {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			Class<?> c = classLoader.loadClass(Names.packetBuffer.getName());
			byteBuf = Transformer.FORGE ? c.getDeclaredField("field_150794_a") : c.getDeclaredField("a");
			byteBuf.setAccessible(true);
			if (Transformer.FORGE) {
				buttonList = classLoader.loadClass(Names.guiScreen.getName()).getDeclaredField("field_146292_n");
				buttonList.setAccessible(true);
			}
			Class<azl> aClass = azl.class;
			if (Transformer.FORGE) {
				serverList = aClass.getDeclaredField("field_148198_l");
			} else {
				serverList = aClass.getDeclaredField("v");
			}
			serverList.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		/*if (Transformer.FORGE) {
			// Forge mod list
			try {
				Class<?> metadataClass = classLoader.loadClass("net.minecraftforge.fml.common.ModMetadata");
				Object metadata = metadataClass.newInstance();
				metadataClass.getField("modId").set(metadata, "the5zigmod");
				metadataClass.getField("name").set(metadata, "The 5zig Mod");
				metadataClass.getField("version").set(metadata, Version.VERSION);
				metadataClass.getField("description").set(metadata, "A mod that enhances your Minecraft PvP experience by displaying all kinds of useful stats.");
				metadataClass.getField("url").set(metadata, "https://5zig.eu");
				metadataClass.getField("authorList").set(metadata, Collections.singletonList("5zig"));
				Class<?> modContainerClass = classLoader.loadClass("net.minecraftforge.fml.common.DummyModContainer");
				Object modContainer = modContainerClass.getConstructor(metadataClass).newInstance(metadata);

				Class<?> loaderClass = classLoader.loadClass("net.minecraftforge.fml.common.Loader");
				Object loader = loaderClass.getMethod("instance").invoke(null);
				Field modListField = loaderClass.getDeclaredField("mods");
				modListField.setAccessible(true);
				List list = Lists.newArrayList((List) modListField.get(loader));
				list.add(modContainer);
				modListField.set(loader, ImmutableList.copyOf(list));
				Class<?> loadControllerClass = classLoader.loadClass("net.minecraftforge.fml.common.LoadController");
				Field modControllerField = loaderClass.getDeclaredField("modController");
				modControllerField.setAccessible(true);
				Object modController = modControllerField.get(loader);
				List activeModList = (List) loadControllerClass.getMethod("getActiveModList").invoke(modController);
				activeModList.add(modContainer);
			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
		}*/
	}

	public static void patchMainMenu(Object instance, int paramInt1, int paramInt2, boolean isForge) {
		aya guiMainMenu = (aya) instance;
		if (!MinecraftFactory.getClassProxyCallback().isShowLastServer())
			return;
		String lastServer;
		IButton button;
		String server = MinecraftFactory.getClassProxyCallback().getLastServer();
		int x = guiMainMenu.l / 2 + 2;
		int y = paramInt1 + paramInt2;
		if (server != null) {
			String[] parts = server.split(":");
			String host = parts[parts.length - 2];
			int port = Integer.parseInt(parts[parts.length - 1]);
			if (port == 25565) {
				lastServer = host;
			} else {
				lastServer = host + ":" + port;
			}
			lastServer = MinecraftFactory.getVars().shortenToWidth(lastServer, 88);
			button = MinecraftFactory.getVars().createButton(99, x, y, 98, 20, lastServer);
		} else {
			button = MinecraftFactory.getVars().createButton(98, x, y, 98, 20, MinecraftFactory.getClassProxyCallback().translate("menu.no_last_server"));
		}
		List<avs> list;
		if (Transformer.FORGE) {
			try {
				list = ((List<avs>) buttonList.get(guiMainMenu));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			list = guiMainMenu.n;
		}
		list.add((avs) button);

		for (avs b : list) {
			int id = b.k;
			if (id == 2) {
				b.f = 98;
			}
		}
	}

	public static void guiMainActionPerformed(Object b) {
		avs button = (avs) b;
		if (button.k == 99) {
			String server = MinecraftFactory.getClassProxyCallback().getLastServer();
			if (server == null)
				return;
			String[] parts = server.split(":");
			String host = parts[parts.length - 2];
			int port = Integer.parseInt(parts[parts.length - 1]);
			MinecraftFactory.getVars().joinServer(host, port);
		}
	}

	public static IButton getThe5zigModButton(Object instance) {
		axu guiScreen = (axu) instance;
		return MinecraftFactory.getVars().createButton(42, guiScreen.l / 2 - 155, guiScreen.m / 6 + 24 - 6, 150, 20, MinecraftFactory.getClassProxyCallback().translate("menu.the5zigMod"));
	}

	public static void guiOptionsActionPerformed(Object instance, Object b) {
		axu guiScreen = (axu) instance;
		avs button = (avs) b;
		if (button.k == 42) {
			MinecraftFactory.getClassProxyCallback().displayGuiSettings(new WrappedGui(guiScreen));
		}
	}

	public static IButton getMCPVPButton(axu guiScreen) {
		return MinecraftFactory.getVars().createButton(9, guiScreen.l / 2 - 23, guiScreen.m - 28, 46, 20, MinecraftFactory.getClassProxyCallback().translate("menu.mcpvp"));
	}

	public static void guiMultiplayerActionPerformed(axu guiScreen, avs button) {
		if (button.k == 9) {
//			The5zigMod.getVars().displayScreen(new ServerListMCPvP(guiScreen));
		}
	}

	public static void setupPlayerTextures(GameProfile gameProfile) {
		((Variables) MinecraftFactory.getVars()).getResourceManager().loadPlayerTextures(gameProfile);
	}

	public static boolean onRenderItemPerson(Object instance, Object itemStackObject, Object entityPlayerObject, Object cameraTransformTypeObject, boolean leftHand) {
		return ((Variables) MinecraftFactory.getVars()).getResourceManager().renderInPersonMode(instance, itemStackObject, entityPlayerObject, cameraTransformTypeObject);
	}

	public static boolean onRenderItemInventory(Object instance, Object itemStackObject, int x, int y) {
		return ((Variables) MinecraftFactory.getVars()).getResourceManager().renderInInventory(instance, itemStackObject, x, y);
	}

	public static ByteBuf packetBufferToByteBuf(Object packetBuffer) {
		try {
			return (ByteBuf) byteBuf.get(packetBuffer);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static void appendCategoryToCrashReport(Object crashReport) {
		((b) crashReport).g().a("The 5zig Mod Version", MinecraftFactory.getClassProxyCallback().getVersion());
		((b) crashReport).g().a("The 5zig Mod Plugins", MinecraftFactory.getClassProxyCallback().getModList());
		((b) crashReport).g().a("Forge", Transformer.FORGE);
		((b) crashReport).g().a("GUI", MinecraftFactory.getVars().getCurrentScreen());
	}

	public static void publishCrashReport(Throwable cause, File crashFile) {
		if (MinecraftFactory.getClassProxyCallback() != null) {
			MinecraftFactory.getClassProxyCallback().launchCrashHopper(cause, crashFile);
		}
	}

	public static void handleGuiDisconnectedDraw(Object instance) {
		axh gui = (axh) instance;
		MinecraftFactory.getClassProxyCallback().checkAutoreconnectCountdown(gui.l, gui.m);
	}

	public static void setServerData(Object serverData) {
		String host = ((bde) serverData).b;
		if (!"5zig.eu".equalsIgnoreCase(host) && !"5zig.net".equalsIgnoreCase(host)) {
			MinecraftFactory.getClassProxyCallback().setAutoreconnectServerData(serverData);
		} else {
			MinecraftFactory.getClassProxyCallback().setAutoreconnectServerData(null);
		}
	}

	public static void fixOptionButtons(Object instance) {
		if (!tryFix)
			return;
		axu guiScreen = (axu) instance;
		tryFix = false;
		List<avs> list;
		if (Transformer.FORGE) {
			try {
				list = ((List<avs>) buttonList.get(guiScreen));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			list = guiScreen.n;
		}
		for (avs button : list) {
			if (button.k != 42 && button.h == guiScreen.l / 2 - 155 && button.i == guiScreen.m / 6 + 24 - 6) {
				button.h = guiScreen.l / 2 + 5;
				button.f = 150;
			}
		}
	}

	public static void handleGuiResourcePackInit(Object instance, Object listObject, Object listObject2) {
		List list = (List) listObject;
		List list2 = (List) listObject2;
		azo gui = (azo) instance;
		Comparator comparator = new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				if (!(o1 instanceof azr) || !(o2 instanceof azr))
					return 0;
				azr resourcePackListEntryFound1 = (azr) o1;
				azr resourcePackListEntryFound2 = (azr) o2;
				return resourcePackListEntryFound1.j().d().toLowerCase(Locale.ROOT).compareTo(resourcePackListEntryFound2.j().d().toLowerCase(Locale.ROOT));
			}
		};
		MinecraftFactory.getClassProxyCallback().addSearch(
				new SearchEntry<azp>(MinecraftFactory.getVars().createTextfield(MinecraftFactory.getClassProxyCallback().translate("gui.search"), 9991, gui.l / 2 - 200, gui.m - 70, 170, 16),
						list, comparator) {
					@Override
					public boolean filter(String text, azp o) {
						if (!(o instanceof azr))
							return true;
						azr resourcePackListEntryFound = (azr) o;
						return resourcePackListEntryFound.j().d().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT)) || resourcePackListEntryFound.j().e().toLowerCase(
								Locale.ROOT).contains(text.toLowerCase(Locale.ROOT));
					}
				},
				new SearchEntry<azp>(MinecraftFactory.getVars().createTextfield(MinecraftFactory.getClassProxyCallback().translate("gui.search"), 9992, gui.l / 2 + 8, gui.m - 70, 170, 16),
						list2, new Comparator<azp>() {
					@Override
					public int compare(azp o1, azp o2) {
						return 0;
					}
				}) {
					@Override
					public boolean filter(String text, azp o) {
						if (!(o instanceof azr))
							return true;
						azr resourcePackListEntryFound = (azr) o;
						return resourcePackListEntryFound.j().d().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT)) || resourcePackListEntryFound.j().e().toLowerCase(
								Locale.ROOT).contains(text.toLowerCase(Locale.ROOT));
					}

					@Override
					protected int getAddIndex() {
						return 1;
					}
				});
	}

	public static void handleGuiMultiplayerInit(Object instance, Object serverSelectionListInstance) {
		final azh guiMultiplayer = (azh) instance;
		final azl serverSelectionList = (azl) serverSelectionListInstance;
		final List<azk> list;
		try {
			list = (List<azk>) ClassProxy.serverList.get(serverSelectionList);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		ITextfield textfield = MinecraftFactory.getVars().createTextfield(MinecraftFactory.getClassProxyCallback().translate("gui.search"), 9991, (guiMultiplayer.l - 305) / 2 + 6,
				guiMultiplayer.m - 84, 170, 16);
		final SearchEntry<azk> searchEntry = new SearchEntry<azk>(textfield, list) {

			@Override
			public void draw() {
				super.draw();
				if (serverSelectionList.e() >= list.size()) {
					serverSelectionList.c(-1);
				}
			}

			@Override
			public boolean filter(String text, azk serverListEntry) {
				return serverListEntry.a().a.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT)) || serverListEntry.a().b.toLowerCase(Locale.ROOT).contains(
						text.toLowerCase(Locale.ROOT));
			}
		};
		Callback<azk> enterCallback = new Callback<azk>() {
			@Override
			public void call(azk callback) {
				guiMultiplayer.b(0);
				guiMultiplayer.f();
				searchEntry.reset();
			}
		};
		searchEntry.setEnterCallback(enterCallback);
		MinecraftFactory.getClassProxyCallback().addSearch(searchEntry);
	}

	public static void handleGuiSelectWorldInit(Object instance, List list) {
		final axv guiSelectWorld = (axv) instance;
		ITextfield textfield = MinecraftFactory.getVars().createTextfield(MinecraftFactory.getClassProxyCallback().translate("gui.search"), 9991, (guiSelectWorld.l - 220) / 2 + 6,
				guiSelectWorld.m - 84, 170, 16);
		final SearchEntry<ats> searchEntry = new SearchEntry<ats>(textfield, list) {
			@Override
			public boolean filter(String text, ats saveFormatComparator) {
				return saveFormatComparator.a().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT)) || saveFormatComparator.b().contains(text.toLowerCase(Locale.ROOT));
			}
		};
		searchEntry.setEnterCallback(new Callback<ats>() {
			@Override
			public void call(ats callback) {
				guiSelectWorld.i(0);
			}
		});
		searchEntry.setComparator(new Comparator<ats>() {
			@Override
			public int compare(ats saveFormatComparator1, ats saveFormatComparator2) {
				return Long.valueOf(saveFormatComparator1.e()).compareTo(saveFormatComparator2.e());
			}
		});
		MinecraftFactory.getClassProxyCallback().addSearch(searchEntry);
	}

	public static void renderSnow() {
		if (MinecraftFactory.getVars().getMinecraftScreen() instanceof aya) {
			MinecraftFactory.getClassProxyCallback().renderSnow(((aya) MinecraftFactory.getVars().getMinecraftScreen()).l, ((aya) MinecraftFactory.getVars().getMinecraftScreen()).m);
		}
	}

	public static float getCustomFOVModifier(Object object) {
		bet playerInstance = (bet) object;

		float modifier = 1.0F;
		if (playerInstance.bA.b) {
			// is Flying
			modifier *= 1.1F;
		}
		qc movementSpeedAttribute = playerInstance.a(vy.d);
		if (movementSpeedAttribute.a(SPRINT_MODIFIER_UUID) != null) {
			modifier = (float) ((double) modifier * (movementSpeedAttribute.b() * 1.30000001192092896D / playerInstance.bA.b() + 1.0) / 2.0D);
		}
		if (playerInstance.bA.b() == 0.0F || Float.isNaN(modifier) || Float.isInfinite(modifier)) {
			modifier = 1.0F;
		}

		if (playerInstance.bS() && playerInstance.bQ() != null && playerInstance.bQ().b() == zy.f) {
			// is using bow
			int itemInUseDuration = playerInstance.bT();
			float itemInUseDurationSeconds = (float) itemInUseDuration / 20.0F;
			if (itemInUseDurationSeconds > 1.0F) {
				itemInUseDurationSeconds = 1.0F;
			} else {
				itemInUseDurationSeconds *= itemInUseDurationSeconds;
			}

			modifier *= 1.0F - itemInUseDurationSeconds * 0.15F;
		}

		return modifier;
	}

	public static String[] getSignText(Object signTile) {
		eu[] lines = ((aln) signTile).a;
		String[] result = new String[lines.length];
		for (int i = 0; i < lines.length; i++) {
			result[i] = lines[i].d();
		}
		return result;
	}

	public static void setSignText(Object signTile, String[] text) {
		for (int i = 0; i < text.length; i++) {
			eu[] components = ((aln) signTile).a;
			if (!Objects.equal(components[i].d(), text[i])) {
				components[i] = new fa(text[i]);
			}
		}
	}

}

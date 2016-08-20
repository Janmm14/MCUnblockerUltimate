package de.janmm14.mcunblockerultimate;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.ListTag;
import com.flowpowered.nbt.StringTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.flowpowered.nbt.stream.NBTOutputStream;
import com.google.gson.Gson;
import de.janmm14.mcunblockerultimate.ping.Pinger;
import de.janmm14.mcunblockerultimate.proxy.Proxy;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.Getter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class McUnblockerUltimate {

	private static final int WIDTH = 660;
	private static final int MIN_HEIGHT = 250;
	private static final int DEF_HEIGHT = 474 + 74;
	private static final Color BACKGROUND_COLOR = new Color(50, 32, 32);
	private static final Gson GSON = new Gson();
	@Getter
	private JFrame frame;
	private WebView browser;
	private WebEngine webEngine;
	private Group root;
	private JFXPanel jfxPanel;
	private static final boolean RESIZABLE = false;

	@SuppressWarnings("Convert2Lambda")
	public static void main(String[] args) throws Exception {
		Thread.currentThread().setName("McUnblockerUltimate main thread");
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					if (OS.getCurrentPlatform() == OS.UNKNOWN) {
						JOptionPane.showMessageDialog(null, "Your operating system is not supported :/", "McUnblockerUltimate v1.0 by Janmm14", JOptionPane.ERROR_MESSAGE);
						System.exit(2);
						return;
					}
					McUnblockerUltimate mcuu = new McUnblockerUltimate();
					new Thread(() -> Proxy.start(mcuu), "McUnblockerUltimate proxy starter thread").start();
					mcuu.frame.setVisible(true);

					mcuu.startWebEngine();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void startWebEngine() {
		Platform.runLater(() -> {
			browser = new WebView();
			browser.setContextMenuEnabled(false);
			final int width = jfxPanel.getWidth();
			final int height = jfxPanel.getHeight();
			resizeBrowser(width, height);
			jfxPanel.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					resizeBrowser(e.getComponent().getWidth(), e.getComponent().getHeight());
				}
			});
			webEngine = browser.getEngine();
			webEngine.setOnError(event -> {
				System.out.println("error: " + event);
				event.getException().printStackTrace();
			});
			webEngine.impl_getDebugger().setMessageCallback(param -> {
				System.out.println("debug: " + param);
				return null;
			});
			webEngine.setJavaScriptEnabled(true);
			try {
				addMcUnblockerUltimateServer();
				servers = importServers();
			} catch (Exception e) {
				e.printStackTrace();
			}
			updateWebPanel();
//			webEngine.load("file:///C:/Users/Jan%20Marian%20Meyer/IdeaProjects/mcunblockerultimate/test.html");
//			webEngine.loadContent("<html><body style='overflow-y:scroll;background:yellow'>Hello!<div style='color:red;border-radius:10px;width:200px;height:400px;background:black;padding:15px;text-align:center'>abcd</div></body>");
			root.getChildren().add(browser);
			new Thread() {
				@Override
				public void run() {
					while (true) {
						try {
							Thread.sleep(2000);
//							Platform.runLater(webEngine::reload);
						} catch (InterruptedException e) {
							e.printStackTrace();
							break;
						}
					}
				}
			}.start();
		});
	}

	public void updateWebPanel() {
		webEngine.loadContent(HtmlCreator.getHtml(servers));
	}

	private static File getMinecraftFolder() {
		final String userHome = System.getProperty("user.home", "./");
		switch (OS.getCurrentPlatform()) {
			case LINUX:
				return new File(userHome + ".minecraft/");
			case WINDOWS: {
				final String appdataPath = System.getenv("APPDATA");
				final String folder = (appdataPath != null) ? appdataPath : userHome;
				return new File(folder, ".minecraft/");
			}
			case OSX:
				return new File(userHome + "Library/Application Support/minecraft");
			default:
				return new File(userHome + "minecraft/");
		}
	}

	private static File getMcUnblockerUltimateFolder() {
		final String userHome = System.getProperty("user.home", "./");
		switch (OS.getCurrentPlatform()) {
			case LINUX:
				return new File(userHome + ".mcunblockerultimate/");
			case WINDOWS: {
				final String appdataPath = System.getenv("APPDATA");
				final String folder = (appdataPath != null) ? appdataPath : userHome;
				return new File(folder, ".mcunblockerultimate/");
			}
			case OSX:
				return new File(userHome + "Library/Application Support/mcunblockerultimate");
			default:
				return new File(userHome + "mcunblockerultimate/");
		}
	}

	@Getter
	private List<Server> servers = new ArrayList<>();

	public void loadServers() {
		File folder = getMcUnblockerUltimateFolder();
		folder.mkdirs();
		File file = new File(folder, "servers.json");
		try {
			file.createNewFile();
			FileReader fileReader = new FileReader(file);
			servers = GSON.fromJson(fileReader, ServerList.class);
			fileReader.close();
			updateWebPanel();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveServers() {
		String json = GSON.toJson(servers, ServerList.class);
		File folder = getMcUnblockerUltimateFolder();
		folder.mkdirs();
		File file = new File(folder, "servers.json");
		try {
			Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Server> importServers() throws Exception {
		List<Server> servers = new ArrayList<>();
		File file = new File(getMinecraftFolder(), "servers.dat");
		NBTInputStream nbt = new NBTInputStream(new FileInputStream(file), false);
		CompoundTag tag = ((CompoundTag) nbt.readTag());
		CompoundMap map = tag.getValue();
		ListTag<CompoundTag> serversCompound = (ListTag) map.get("servers");
		for (CompoundTag compoundTag : serversCompound.getValue()) {
			CompoundMap value = compoundTag.getValue();
			Tag<?> ips = value.get("ip");
			String[] ip = ((String) ips.getValue()).split(":");
			Integer port = ip.length > 1 ? Integer.valueOf(ip[1]) : 25565;
			Tag<?> icon = value.get("icon");
			String pic = null;
			if (icon != null) {
				pic = getIconHtml((String) icon.getValue());
			}
			String name = (String) value.get("name").getValue();
			if (!"§a§lMcUnblockerUltimate§r".equals(name)) {
				Server s = new Server(ip[0], port, name, pic);
				servers.add(s);
			}
		}
		nbt.close();
		return servers;
	}

	@SuppressWarnings("unchecked")
	public void addMcUnblockerUltimateServer() throws Exception {
		File file = new File(getMinecraftFolder(), "servers.dat");
		NBTInputStream nbt = new NBTInputStream(new FileInputStream(file), false);
		CompoundTag tag = ((CompoundTag) nbt.readTag());
		CompoundMap map = tag.getValue();
		ListTag<CompoundTag> serversCompound = (ListTag) map.get("servers");

		CompoundMap myMap = new CompoundMap();
		myMap.put("ip", new StringTag("ip", "localhost"));
		myMap.put("name", new StringTag("name", "§a§lMcUnblockerUltimate§r"));

		List<CompoundTag> value = new ArrayList<>(serversCompound.getValue());
		Iterator<CompoundTag> iterator = value.iterator();
		while (iterator.hasNext()) {
			CompoundMap next = iterator.next().getValue();
			String name = (String) next.get("name").getValue();
			if ("§a§lMcUnblockerUltimate§r".equals(name)) {
				iterator.remove();
			}
		}
		value.add(0, new CompoundTag("", myMap));

		ListTag<CompoundTag> newServersCompound = new ListTag<>("servers", CompoundTag.class, value);
		CompoundMap newMap = new CompoundMap();
		newMap.put("servers", newServersCompound);
		CompoundTag newTag = new CompoundTag("servers", newMap);

		nbt.close();
		NBTOutputStream out = new NBTOutputStream(new FileOutputStream(file), false);
		out.writeTag(newTag);
		out.flush();
		out.close();
	}

	public static String getIconHtml(String base64) {
		return "<img src=\"data:image/png;base64," + base64 + "\">";
	}

	public int getSelectedServerId() {
		Document document = webEngine.getDocument();
		NodeList childNodes = document.getChildNodes().item(1).getChildNodes();

		Node body = childNodes.item(1);

		NodeList div = body.getChildNodes();
		List<Node> divs = new ArrayList<>();
		for (int i = 0; i < div.getLength(); i++) {
			Node item = div.item(i);
			if (item.getNodeName().equalsIgnoreCase("div")) {
				divs.add(item);
			}
		}
		Node selected = null;
		for (Node node : divs) {
			Node element = node.getChildNodes().item(0);
			NamedNodeMap attributes = element.getAttributes();
			Node aClass = attributes.getNamedItem("class");
			String nodeValue = aClass.getNodeValue();
			if (nodeValue.contains("selected")) {
				selected = element;
			}
		}
		if (selected != null) {
			return Integer.valueOf(selected.getAttributes().getNamedItem("id").getNodeValue());
		}
		return -1;
	}

	private void resizeBrowser(int width, int height) {
		browser.setMinSize(width, height);
		browser.setPrefSize(width, height);
		browser.setMaxSize(width, height);
	}

	public McUnblockerUltimate() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame("McUnblockerUltimate v1.0 by Janmm14");
		frame.setMinimumSize(new Dimension(WIDTH, MIN_HEIGHT));
		frame.setMaximumSize(new Dimension(WIDTH, 1000));
		frame.setPreferredSize(new Dimension(WIDTH, DEF_HEIGHT));
		frame.setSize(new Dimension(WIDTH, DEF_HEIGHT));
		frame.setResizable(RESIZABLE);
		frame.setLocationRelativeTo(null);
		frame.setBackground(Color.BLACK);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				int result = JOptionPane.showConfirmDialog(frame, "Do you really want to close McUnblockerUltimate?\n\nIf you currently play through McUnblockerUltimate, you will be disconnected!", "McUnblockerUltimate v1.0 by Janmm14", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					System.exit(0);
				}
			}
		});

		jfxPanel = new JFXPanel();
		root = new Group();
		Scene scene = new Scene(root);
		jfxPanel.setScene(scene);
		jfxPanel.setBackground(BACKGROUND_COLOR);

		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.setBackground(BACKGROUND_COLOR);
		btnRefresh.addActionListener(e -> pingServers());

		JButton btnHelp = new JButton("Help");
		btnHelp.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Using this tool is really easy!\n\n1. Refresh your minecraft server list, to see the McUnblockerUltimate server.\n2. Select a server in McUnblockerUltimate's window.\n3. Refresh the server list. You should see your selected servers motd.\n4. Join the McUnblockerUltimate server.\n5. Enjoy!", "Help of McUnblockerUltimate v1.0 by Janmm14", JOptionPane.PLAIN_MESSAGE));
		btnHelp.setBackground(BACKGROUND_COLOR);

		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(jfxPanel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 625, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnRefresh, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnHelp, GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
						))
					.addGap(9))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(jfxPanel, GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnRefresh, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
						.addComponent(btnHelp, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
					)
					.addContainerGap())
		);

		frame.getContentPane().setLayout(groupLayout);
		frame.getContentPane().setBackground(BACKGROUND_COLOR);
	}

	private void pingServers() {
		servers.forEach(server -> Pinger.pingAndUpdate(this, server));
	}
}

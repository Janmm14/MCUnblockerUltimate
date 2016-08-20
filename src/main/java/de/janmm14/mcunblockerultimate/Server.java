package de.janmm14.mcunblockerultimate;

import de.janmm14.mcunblockerultimate.ping.PingResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;

@Getter
@Setter
@NoArgsConstructor
public class Server {

	private transient int online;
	private transient int max;
	private transient int ping;
	private transient String motd;
	private String ip;
	private int port;
	private String name;
	private String picture;

	public Server(String ip, int port, String name, String picture) {
		this.ip = ip;
		this.port = port;
		this.name = name;
		this.picture = picture;
	}

	public void setData(PingResponse res) {
		setMotd(TextComponent.toLegacyText(res.getDescription()));
		String iconHtml = getIconHtml(res.getFavicon());
		setPicture(iconHtml);
		PingResponse.PlayerInfo pi = res.getPlayers();
		setOnline(pi.getOnline());
		setMax(pi.getMax());
		//TODO display online player name information
		PingResponse.VersionInfo ver = res.getVersion();
		//TODO display version information
	}

	public static String getIconHtml(String base64) {
		return "<img src=\"" + base64 + "\">";
	}

}

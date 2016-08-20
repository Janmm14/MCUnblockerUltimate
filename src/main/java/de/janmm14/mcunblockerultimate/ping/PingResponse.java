package de.janmm14.mcunblockerultimate.ping;

import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;

@Getter
@ToString
public class PingResponse {

	private VersionInfo version;
	private PlayerInfo players;
	private BaseComponent description;
	private String favicon;

	@Getter
	public class VersionInfo {
		private String name;
		private int protocol;
	}

	@Getter
	public class PlayerInfo {
		private int online;
		private int max;
	}
}

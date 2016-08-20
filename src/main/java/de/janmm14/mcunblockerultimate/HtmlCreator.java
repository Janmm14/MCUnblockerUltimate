package de.janmm14.mcunblockerultimate;

import java.util.List;

import net.md_5.bungee.api.ChatColor;

public class HtmlCreator {

	private HtmlCreator() {
		throw new UnsupportedOperationException();
	}

	public static String getHtml(List<Server> servers) {
		StringBuilder sb = new StringBuilder(ITEM.length() + servers.size() * ITEM.length() * 3 + END.length());
		sb.append(START);
		for (int i = 0, serversSize = servers.size(); i < serversSize; i++) {
			Server server = servers.get(i);
			//player count and ping
			String pcp = server.getOnline() + "/" + server.getMax() + " "; //+ server.getPing() + "ms";
			String picture = server.getPicture();
			String motd = server.getMotd();
			if (motd != null) {
				//TODO handle colors
				motd = ChatColor.stripColor(motd);
				motd = motd.replace(" ", "  ");
			}
			String s = String.format(ITEM, i, nullAlternative(picture, ""), server.getName(), pcp, nullAlternative(motd, ""));
			sb.append(s);
		}
		return sb.append(END).toString();
	}

	public static <T> T nullAlternative(T object, T alternative) {
		return object == null ? alternative : object;
	}
	private static final String ITEM = "" +
		"<div class=\"outer\"><div class=\"element\" id=\"%s\">" +
		"<div class=\"img\">%s</div>" +
		"<div class=\"name\">%s</div>" +
		"<div class=\"pcp\"><span>%s</span></div>" +
		"<div class=\"motd\">%s</div>" +
		"<div class=\"rightline\"></div>" +
		"</div></div>";

	private static final String START = "<!Doctype html><html><head>" +
		"<script src=\"file:///C:/xampp-neu/htdocs/obscurely-origin/js/jquery-1.4.2.min.js\"></script>" +
		"<style>" +
		"@font-face {font-family:\"Minecraft Regular\";src:url(\"file:///C:/xampp-neu/minecraft_font_by_pwnage_block-d37t6nb.eot?\") format(\"eot\"),url(\"file:///C:/xampp-neu/minecraft_font_by_pwnage_block-d37t6nb.woff\") format(\"woff\"),url(\"file:///C:/xampp-neu/minecraft_font_by_pwnage_block-d37t6nb.ttf\") format(\"truetype\"),url(\"file:///C:/xampp-neu/minecraft_font_by_pwnage_block-d37t6nb.svg#Minecraft\") format(\"svg\");font-weight:normal;font-style:normal;}" +
		"* { -webkit-user-select: none; cursor:default; }" +
		"html, body { margin:0; padding:0; }" +
		"html::-webkit-scrollbar { background: rgb(50,32,32); }" +
		"html::-webkit-scrollbar-button { display:none; background: transparent; }" +
		"html::-webkit-scrollbar-track { background: transparent; }" +
		"html::-webkit-scrollbar-track-piece { background: transparent; }" +
		"html::-webkit-scrollbar-thumb {" +
		"background: rgb(167,167,167); border-right: 2px solid gray; border-bottom: 2px solid gray; }" +
		"html::-webkit-scrollbar-corner { background: transparent; }" +
		"html::-webkit-resizer { background: transparent; }" +
		"html, body, .pcp > span, .element { background:rgb(50,32,32); }" +
		"body { overflow-x:hidden; overflow-y:scroll; font-family: \"Minecraft Regular\", \"Courier New\", sans-serif; }" +
		".element { position:relative; width:100%; height:70px; border:2px rgb(50,32,32) solid; }" +
		".element.selected { border:2px lightgray solid; }" +
		".element > div { position:absolute; display:inline-block; }" +
		".name, .motd { left:76px; }" +
		".img { top:0px; left:0px; width:70px; height:70px; }" +
		".img > img { position:absolute;top:2px; left:2px }" +
		".name { color:white; top:0px; width:420px; }" +
		".pcp {/*player count & ping*/ z-index:2; color:lightgray; top:0px; right:6px; width:400px; height:100%; text-align:right; }" +
		".pcp > span { }" +
		".motd { top:22px; color:lightgray; width:600px; height:40px; white-space: pre-wrap }" +
		".rightline { display:none; }" +
		".selected > .rightline { display:inline-block; right:0px; top:0px; bottom:0px; background:lightgray; width:4px; } </style>" +
		"</head>" +
		"<body>";

	private static final String END = "<script>" +
		"$(\".element\").click(function(){" +
		"$(\".element\").attr(\"class\", \"element\");" +
		"$(this).attr(\"class\", \"element selected\");" +
		"});" +
		"</script>" +
		"</body>" +
		"</html>";
}

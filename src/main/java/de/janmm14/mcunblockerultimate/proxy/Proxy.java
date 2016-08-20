package de.janmm14.mcunblockerultimate.proxy;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.janmm14.mcunblockerultimate.McUnblockerUltimate;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public final class Proxy {

	private Proxy() {
		throw new UnsupportedOperationException();
	}

	public static void start(McUnblockerUltimate mcuu) {
		System.out.println("Starting...");
		try {
			new ServerBootstrap()
				.group(new NioEventLoopGroup(2, new ThreadFactoryBuilder().setNameFormat("McUnblockerUltimate proxy network thread #%d").build()))
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000)
				.option(ChannelOption.SO_TIMEOUT, 20000)
				.option(ChannelOption.TCP_NODELAY, true)
				.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000)
				.childHandler(new MyClientChannelInitializer(mcuu))
				.bind(25565)
				.sync();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

package de.janmm14.mcunblockerultimate.proxy;

import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.janmm14.mcunblockerultimate.McUnblockerUltimate;
import de.janmm14.mcunblockerultimate.Server;
import lombok.RequiredArgsConstructor;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

@RequiredArgsConstructor
final class MyClientChannelInitializer extends ChannelInitializer<SocketChannel> {

	static final AttributeKey<SocketChannel> SERVER_CHANNEL_ATTRIBUTE_KEY = AttributeKey.valueOf("serverChannel");

	private final McUnblockerUltimate mcuu;
	private static final EventLoopGroup EVENT_LOOP_GROUP = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("McUnblockerUltimate server connection thread #%d").build());

	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		System.out.println("connected client: " + ch.remoteAddress().getAddress().getHostAddress() + ":" + ch.remoteAddress().getPort());
		int id = mcuu.getSelectedServerId();
		if (id == -1) {
			mcuu.getFrame().requestFocus();
			ch.close();
			System.out.println("closed because no endpoint specified");
			return;
		}

		try
		{
			ch.config().setOption( ChannelOption.IP_TOS, 0x18 );
		} catch ( ChannelException ex )
		{
			// IP_TOS is not supported (Windows XP / Windows Server 2003)
		}
		ch.config().setAllocator( PooledByteBufAllocator.DEFAULT );

		Attribute<SocketChannel> servChAttr = ch.attr(SERVER_CHANNEL_ATTRIBUTE_KEY);

		Server server = mcuu.getServers().get(id);
		new Bootstrap()
			.group(EVENT_LOOP_GROUP)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.TCP_NODELAY, true)
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000)
			.option(ChannelOption.IP_TOS, 0x18)
			.handler(new MyServerChannelInitializer(servChAttr, ch, server))
			.connect(server.getIp(), server.getPort())
			.sync();

		ch.pipeline().addLast(new ReadTimeoutHandler(20, TimeUnit.SECONDS));
		ch.pipeline().addLast(new ClientInbondHandler());
	}
}

package de.janmm14.mcunblockerultimate.proxy;

import de.janmm14.mcunblockerultimate.Server;
import lombok.RequiredArgsConstructor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.Attribute;

@RequiredArgsConstructor
final class MyServerChannelInitializer extends ChannelInitializer<SocketChannel> {

	private final Attribute<SocketChannel> servChAttr;
	private final SocketChannel clientChannel;
	private final Server server;

	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		System.out.println("connected to server " + server.getIp() + ":" + server.getPort());
		servChAttr.set(ch);
		try
		{
			ch.config().setOption( ChannelOption.IP_TOS, 0x18 );
		} catch ( ChannelException ex )
		{
			// IP_TOS is not supported (Windows XP / Windows Server 2003)
		}
		ch.config().setAllocator( PooledByteBufAllocator.DEFAULT );
		ch.pipeline().addLast(new ReadTimeoutHandler(20));
		ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
			@Override
			protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
				clientChannel.writeAndFlush(buf.copy());
			}
		});
	}
}

package de.janmm14.mcunblockerultimate.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

final class ClientInbondHandler extends SimpleChannelInboundHandler<ByteBuf> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
		ctx.channel().attr(MyClientChannelInitializer.SERVER_CHANNEL_ATTRIBUTE_KEY).get().writeAndFlush(buf.copy());
	}
}

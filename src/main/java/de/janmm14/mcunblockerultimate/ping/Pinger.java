package de.janmm14.mcunblockerultimate.ping;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.janmm14.mcunblockerultimate.McUnblockerUltimate;
import de.janmm14.mcunblockerultimate.Server;
import javafx.application.Platform;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.chat.TextComponentSerializer;
import net.md_5.bungee.chat.TranslatableComponentSerializer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

public final class Pinger {

	private static final Gson gson = new GsonBuilder()
		.registerTypeAdapter( BaseComponent.class, new ComponentSerializer() )
		.registerTypeAdapter( TextComponent.class, new TextComponentSerializer() )
		.registerTypeAdapter( TranslatableComponent.class, new TranslatableComponentSerializer() ).create();
	private static final NioEventLoopGroup EVENT_LOOP_GROUP = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("McUnblockerUltimate server pinger #%d").build());

	private Pinger() {
		throw new UnsupportedOperationException();
	}

	public static void pingAndUpdate(McUnblockerUltimate mcuu, Server server) {
		new Thread(() -> new Bootstrap()
			.channel( NioSocketChannel.class )
			.group(EVENT_LOOP_GROUP)
			.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast( new ReadTimeoutHandler( 10, TimeUnit.SECONDS ) );
					ReplayingDecoder.class.getName();
					ch.pipeline().addLast( new ReplayingDecoder<Void>() {
						@Override
						public void channelActive(ChannelHandlerContext ctx) throws Exception {
							String s = server.getIp();
							byte[] b = s.getBytes(Charsets.UTF_8);
							ByteBuf buf = ctx.alloc().directBuffer();
							Util.writeVarInt(0x00, buf);//packet id
							Util.writeVarInt(47, buf);//protocol version

							Util.writeVarInt(b.length, buf);
							buf.writeBytes(b);

							buf.writeShort(server.getPort()); //port
							Util.writeVarInt(1, buf); //requested protocol - 1 for motd

							ByteBuf buf2 = ctx.alloc().directBuffer(5);
							Util.writeVarInt(buf.writerIndex(), buf2);
							ctx.channel().writeAndFlush(buf2);
							ctx.channel().writeAndFlush(buf);

							//handshake request
							ByteBuf buf3 = ctx.alloc().directBuffer(5);
							Util.writeVarInt(0x00, buf3);
							ByteBuf buf4 = ctx.alloc().directBuffer(5);
							Util.writeVarInt(buf3.writerIndex(), buf4);
							ctx.channel().writeAndFlush(buf4);
							ctx.channel().writeAndFlush(buf3);
						}

						@Override
						protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
							int packetLength = Util.readVarInt(buf);
							int packetId = Util.readVarInt(buf);

							String str = Util.readString(buf);

							list.add(str);
							try {
								PingResponse res = gson.fromJson(str, PingResponse.class);
								if (res != null) {
									server.setData(res);
								} else {
									server.setMotd("Could not decode json #1");
								}
							} catch (Exception ex) {
								server.setMotd("Could not decode json #2");
								ex.printStackTrace();
							}
							Platform.runLater(mcuu::updateWebPanel);
							ctx.close().await();
						}

						private Error getSignal() throws ReflectiveOperationException {
							Field replay = ReplayingDecoder.class.getDeclaredField("REPLAY");
							replay.setAccessible(true);
							return (Error) replay.get(null);
						}

						@Override
						public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
							String localMsg = cause.getLocalizedMessage();
							server.setMotd(localMsg == null || localMsg.trim().isEmpty() ? cause.getClass().getSimpleName() : localMsg);
							Platform.runLater(mcuu::updateWebPanel);
							cause.printStackTrace();
						}
					});
				}
			})
			.option( ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000 ) // TODO: Configurable
			.remoteAddress( server.getIp(), server.getPort() )
			.connect()
			.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (!future.isSuccess()) {
						server.setMotd("§cCould not connect to server.");
						Platform.runLater(mcuu::updateWebPanel);
					}
				}
			})).start();
	}
}

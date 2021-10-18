package org.jire.swiftfup.server.net

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.IdleStateHandler
import it.unimi.dsi.fastutil.ints.Int2ByteMap
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap
import org.jire.swiftfup.server.net.codec.FileServerRequestDecoder
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Jire
 */
@Sharable
class FileServerChannelInitializer(
	private val eventLoopGroup: EventLoopGroup,
	private val fileRequestResponses: FileRequestResponses
) : ChannelInitializer<SocketChannel>(), Runnable {
	
	private val ipToConnections: Int2ByteMap = Int2ByteOpenHashMap()
	
	init {
		eventLoopGroup.scheduleAtFixedRate(this, IP_CYCLE_TIME, IP_CYCLE_TIME, IP_CYCLE_TIME_UNIT)
	}
	
	override fun initChannel(ch: SocketChannel) {
		val address = ch.remoteAddress().address
		val ip = Arrays.hashCode(address.address)
		
		synchronized(ipToConnections) {
			val connections = ipToConnections.get(ip)
			if (connections >= IP_CYCLE_THRESHOLD)
				throw IllegalStateException("Too many connections from $address")
			
			ipToConnections[ip] = (connections + 1).toByte()
		}
		
		ch.pipeline()
			.addLast(IdleStateHandler(15, 0, 0))
			.addLast(FileServerRequestDecoder(fileRequestResponses))
	}
	
	companion object {
		private const val IP_CYCLE_THRESHOLD = 1
		
		private const val IP_CYCLE_TIME = 5L
		private val IP_CYCLE_TIME_UNIT = TimeUnit.SECONDS
	}
	
	override fun run() {
		synchronized(ipToConnections) {
			ipToConnections.clear()
		}
	}
	
}
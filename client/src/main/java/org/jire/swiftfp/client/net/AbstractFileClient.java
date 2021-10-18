package org.jire.swiftfp.client.net;

import org.jire.swiftfp.client.FileChecksumRequester;
import org.jire.swiftfp.client.FileRequest;
import org.jire.swiftfp.client.FileRequester;
import org.jire.swiftfp.client.FileResponse;

import java.util.function.Consumer;

/**
 * @author Jire
 */
public abstract class AbstractFileClient
		<RESPONSE extends FileResponse,
				REQUESTER extends FileRequester<RESPONSE> & FileChecksumRequester>
		implements FileClient<RESPONSE, REQUESTER> {
	
	private final String host;
	private final int port;
	
	private final REQUESTER requester;
	
	private final Consumer<FileRequest<RESPONSE>> whenNew;
	
	public AbstractFileClient(String host, int port, REQUESTER requester) {
		this.host = host;
		this.port = port;
		this.requester = requester;
		
		whenNew = request -> {
			if (!request.isDone())
				request(request);
		};
	}
	
	@Override
	public String getHost() {
		return host;
	}
	
	@Override
	public int getPort() {
		return port;
	}
	
	@Override
	public REQUESTER getFileRequester() {
		return requester;
	}
	
	@Override
	public Consumer<FileRequest<RESPONSE>> getWhenNew() {
		return whenNew; // optimized to avoid alloc.
	}
	
}

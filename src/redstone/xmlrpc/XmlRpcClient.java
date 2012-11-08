/*
    Copyright (c) 2007 Redstone Handelsbolag

    This library is free software; you can redistribute it and/or modify it under the terms
    of the GNU Lesser General Public License as published by the Free Software Foundation;
    either version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
    without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License along with this
    library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
    Boston, MA  02111-1307  USA
 */

package redstone.xmlrpc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;

import cn.code.notes.share.WizGlobals;

/**
 * An XmlRpcClient represents a connection to an XML-RPC enabled server. It
 * implements the XmlRpcInvocationHandler so that it may be used as a relay to
 * other XML-RPC servers when installed in an XmlRpcServer.
 * 
 * @author Greger Olsson
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class XmlRpcClient extends XmlRpcParser implements
		XmlRpcInvocationHandler {
	/**
	 * Creates a new client with the ability to send XML-RPC messages to the the
	 * server at the given URL.
	 * 
	 * @param url
	 *            the URL at which the XML-RPC service is locaed
	 * 
	 * @param streamMessages
	 *            Indicates whether or not to stream messages directly or if the
	 *            messages should be completed locally before being sent all at
	 *            once. Streaming is not directly supported by XML-RPC, since
	 *            the Content-Length header is not included in the HTTP post. If
	 *            the other end is not relying on Content-Length, streaming the
	 *            message directly is much more efficient.
	 * @throws MalformedURLException
	 */

	private Context mContext;

	public XmlRpcClient(Context ctx, String url, boolean streamMessages)
			throws MalformedURLException {
		this(new URL(url), streamMessages);
		mContext = ctx;
	}

	/**
	 * @see XmlRpcClient(String,boolean)
	 */

	public XmlRpcClient(URL url, boolean streamMessages) {
		this.url = url;
		this.streamMessages = streamMessages;

		if (!streamMessages) {
			writer = new StringWriter(2048);
		}
	}

	/**
	 * Sets the HTTP request properties that the client will use for the next
	 * invocation, and any invocations that follow until setRequestProperties()
	 * is invoked again. Null is accepted and means that no special HTTP request
	 * properties will be used in any future XML-RPC invocations using this
	 * XmlRpcClient instance.
	 * 
	 * @param requestProperties
	 *            The HTTP request properties to use for future invocations made
	 *            using this XmlRpcClient instance. These will replace any
	 *            previous properties set using this method or the
	 *            setRequestProperty() method.
	 */

	public void setRequestProperties(Map requestProperties) {
		this.requestProperties = requestProperties;
	}

	/**
	 * Sets a single HTTP request property to be used in future invocations.
	 * 
	 * @see setRequestProperties()
	 * 
	 * @param name
	 *            Name of the property to set
	 * @param value
	 *            The value of the property
	 */

	public void setRequestProperty(String name, String value) {
		if (requestProperties == null) {
			requestProperties = new HashMap();
		}

		requestProperties.put(name, value);
	}

	/**
	 * Invokes a method on the terminating XML-RPC end point. The supplied
	 * method name and argument collection is used to encode the call into an
	 * XML-RPC compatible message.
	 * 
	 * @param method
	 *            The name of the method to call.
	 * 
	 * @param arguments
	 *            The arguments to encode in the call.
	 * 
	 * @return The object returned from the terminating XML-RPC end point.
	 * 
	 * @throws XmlRpcException
	 *             One or more of the supplied arguments are unserializable.
	 *             That is, the built-in serializer connot parse it or find a
	 *             custom serializer that can. There may also be problems with
	 *             the socket communication.
	 * @throws
	 */

	public synchronized Object invoke(String method, List arguments)
			throws XmlRpcException, XmlRpcFault {
		beginCall(mContext, method);

		if (arguments != null) {
			Iterator argIter = arguments.iterator();

			while (argIter.hasNext()) {
				try {
					writer.write("<param>");
					serializer.serialize(argIter.next(), writer);
					writer.write("</param>");
				} catch (IOException ioe) {
					throw new XmlRpcNetworkException(
							XmlRpcMessages
									.getString("XmlRpcClient.NetworkError"),
							ioe);
				}
			}
		}

		return endCall(mContext, method);
	}

	/**
	 * Invokes a method on the terminating XML-RPC end point. The supplied
	 * method name and argument vector is used to encode the call into XML-RPC.
	 * 
	 * @param method
	 *            The name of the method to call.
	 * 
	 * @param arguments
	 *            The arguments to encode in the call.
	 * 
	 * @return The object returned from the terminating XML-RPC end point.
	 * 
	 * @throws XmlRpcException
	 *             One or more of the supplied arguments are unserializable.
	 *             That is, the built-in serializer connot parse it or find a
	 *             custom serializer that can. There may also be problems with
	 *             the socket communication.
	 */
	final static public String ApiMethodName_DownloadAttachmentData = "attachment.getMobileData";

	public synchronized Object invoke(String method, Object[] arguments)
			throws XmlRpcException, XmlRpcFault {

		beginCall(mContext, method);

		if (arguments != null) {
			for (int i = 0; i < arguments.length; ++i) {
				try {
					writer.write("<param>");
					// 把hashMap中的值取出写在xml中
					// if (method.equals("attachment.postSimpleData")
					// || method == "attachment.postSimpleData") {
					//
					// serializer.serializeattachment(arguments[i], writer);
					// }else {

					serializer.serialize(arguments[i], writer);
					// }
					writer.write("</param>");
				} catch (IOException ioe) {
					throw new XmlRpcNetworkException(
							XmlRpcMessages
									.getString("XmlRpcClient.NetworkError"),
							ioe);
				}
			}
		}

		return endCall(mContext, method);
	}

	/**
	 * A asynchronous version of invoke performing the call in a separate thread
	 * and reporting responses, faults, and exceptions through the supplied
	 * XmlRpcCallback. Determine on proper strategy for instantiating Threads.
	 * 
	 * @param method
	 *            The name of the method at the server.
	 * 
	 * @param arguments
	 *            The arguments for the call. This may be either a
	 *            java.util.List descendant, or a java.lang.Object[] array.
	 * 
	 * @param callback
	 *            An object implementing the XmlRpcCallback interface. If
	 *            callback is null, the call will be performed but any results,
	 *            faults, or exceptions will be ignored (fire and forget).
	 */

	public void invokeAsynchronously(final String method,
			final Object arguments, final XmlRpcCallback callback) {
		if (callback == null) {
			new Thread() {
				public void run() {
					try // Just fire and forget.
					{
						if (arguments instanceof Object[])
							invoke(method, (Object[]) arguments);
						else
							invoke(method, (List) arguments);
					} catch (XmlRpcFault e) { /* Ignore, no callback. */
					} catch (XmlRpcException e) { /* Ignore, no callback. */
					}
				}
			}.start();
		} else {
			new Thread() {
				public void run() {
					Object result = null;

					try {
						if (arguments instanceof Object[])
							result = invoke(method, (Object[]) arguments);
						else
							result = invoke(method, (List) arguments);

						callback.onResult(result);
					} catch (XmlRpcException e) {
						callback.onException(e);
					} catch (XmlRpcFault e) {
						callback.onFault(e.getErrorCode(), e.getMessage());
					}
				}
			}.start();
		}
	}

	/**
	 * Initializes the XML buffer to be sent to the server with the XML-RPC
	 * content common to all method calls, or serializes it directly over the
	 * writer if streaming is used. The parameters to the call are added in the
	 * execute() method, and the closing tags are appended when the call is
	 * finalized in endCall().
	 * 
	 * @param methodName
	 *            The name of the method to call.
	 */

	// xml的开头相同部分
	private void beginCall(Context ctx, String methodName)
			throws XmlRpcException {
		try {
			if (streamMessages) {
				openConnection(ctx);
				writer = new BufferedWriter(new OutputStreamWriter(
						connection.getOutputStream(),
						XmlRpcMessages.getString("XmlRpcClient.Encoding")),
						1024 * 8);
			} else {
				((StringWriter) writer).getBuffer().setLength(0);
			}

			/**
			 * xml头文件
			 */
			writer.write("<?xml version=\"1.0\" encoding=\"");
			writer.write(XmlRpcMessages.getString("XmlRpcClient.Encoding"));// 获取编码方式
			writer.write("\"?>");

			writer.write("<methodCall><methodName>");
			writer.write(methodName);
			writer.write("</methodName><params>");
		} catch (IOException ioe) {
			throw new XmlRpcNetworkException(
					XmlRpcMessages.getString("XmlRpcClient.NetworkError"), ioe);
		}
	}

	/**
	 * Finalizaes the XML buffer to be sent to the server, and creates a HTTP
	 * buffer for the call. Both buffers are combined into an XML-RPC message
	 * that is sent over a socket to the server.
	 * 
	 * @return The parsed return value of the call.
	 * 
	 * @throws XmlRpcException
	 *             when some IO problem occur.
	 */

	private Object endCall(Context ctx, String method) throws XmlRpcException,
			XmlRpcFault {
		try {
			writer.write("</params>");
			writer.write("</methodCall>");

			if (streamMessages) {
				writer.flush();
			} else {
				StringBuffer buffer = ((StringWriter) writer).getBuffer();
				//

				// 打开网络连接
				openConnection(ctx);
				// 获取到一个字符数组
				byte[] data = buffer.toString().getBytes();
				// 输出到指定文件
				/*
				 * 测试时使用，发布时注销
				 */
				// java.io.FileOutputStream s = new java.io.FileOutputStream(
				// new java.io.File("/sdcard/xml-rpc-request.xml"));
				// s.write(data);
				// s.close();

				connection.setRequestProperty("Content-Length",
						String.valueOf(data.length));

				// 输出给网络数据
				OutputStream output = new BufferedOutputStream(
						connection.getOutputStream(), 1024 * 8);
				output.write(data);
				output.flush();
				output.close();
			}

			handleResponse(method);
		} catch (IOException ioe) {
			throw new XmlRpcNetworkException(
					XmlRpcMessages.getString("XmlRpcClient.NetworkError"), ioe);
		} finally {
			try {
				writer.close();
			} catch (IOException ignore) { /*
											 * Closed or not, we don't care at
											 * this point.
											 */
			}

			connection.disconnect();
			connection = null;
		}

		return returnValue;
	}

	/**
	 * Handles the response returned by the XML-RPC server. If the server
	 * responds with a "non-200"-HTTP response or if the XML payload is
	 * unparseable, this is interpreted as an error in communication and will
	 * result in an XmlRpcException.
	 * <p>
	 * 
	 * If the user does not want the socket to be kept alive or if the server
	 * does not support keep-alive, the socket is closed.
	 * 
	 * @param inout
	 *            The stream containing the server response to interpret.
	 * 
	 * @throws IOException
	 *             If a socket error occurrs, or if the XML returned is
	 *             unparseable. This exception is currently also thrown if a
	 *             HTTP response other than "200 OK" is received.
	 * @throws XmlRpcFault
	 */

	private void handleResponse(String method) throws XmlRpcFault {
		try {
			java.io.InputStream is = connection.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, 1024 * 8);
			parse(method, bis);
		} catch (IOException ioe) {
			throw new XmlRpcNetworkException(
					XmlRpcMessages.getString("XmlRpcClient.NetworkError"), ioe);
		} catch (XmlRpcException e) {
			throw e;
		} catch (XmlRpcFault e) {
			throw e;
		} catch (Exception e) {
			throw new XmlRpcException(
					XmlRpcMessages.getString("XmlRpcClient.ParseError"), e);
		}

	}

	/**
	 * Stores away the one and only value contained in XML-RPC responses.
	 * 
	 * @param value
	 *            The contained return value.
	 */

	protected void handleParsedValue(Object value) {
		returnValue = value;
	}

	/**
	 * Opens a connection to the URL associated with the client instance. Any
	 * HTTP request properties set using setRequestProperties() are recorded
	 * with the internal HttpURLConnection and are used in the HTTP request.
	 * 
	 * @throws IOException
	 *             If a connection could not be opened. The exception is
	 *             propagated out of any unsuccessful calls made into the
	 *             internal java.net.HttpURLConnection.
	 */

	private void openConnection(Context ctx) throws IOException {
		if (WizGlobals.isCMWAP(ctx)) {
			URL url2 = new URL(url.getProtocol(), "10.0.0.172", url.getPort(),
					url.getFile());
			connection = (HttpURLConnection) url2.openConnection();
			connection.setRequestProperty("X-Online-Host", url.getHost());
			//
			/*
			 * Proxy proxy = new Proxy(Proxy.Type.HTTP, new
			 * InetSocketAddress("10.0.0.172", 80)); connection = (
			 * HttpURLConnection ) url.openConnection(proxy);
			 */
		} else {
			connection = (HttpURLConnection) url.openConnection();
		}

		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "text/xml; charset="
				+ XmlRpcMessages.getString("XmlRpcClient.Encoding"));

		if (requestProperties != null) {
			for (Iterator propertyNames = requestProperties.keySet().iterator(); propertyNames
					.hasNext();) {
				String propertyName = (String) propertyNames.next();

				connection.setRequestProperty(propertyName,
						(String) requestProperties.get(propertyName));
			}
		}
	}

	/** The server URL. */
	private URL url;

	/** Connection to the server. */
	private HttpURLConnection connection;

	/**
	 * HTTP request properties, or null if none have been set by the
	 * application.
	 */
	private Map requestProperties;

	/** The parsed value returned in a response. */
	private Object returnValue;

	/** Writer to which XML-RPC messages are serialized. */
	private Writer writer;

	/**
	 * Indicates wheter or not we shall stream the message directly or build
	 * them locally?
	 */
	private boolean streamMessages;

	/** Indicates whether or not the incoming response is a fault response. */

	/** The serializer used to serialize arguments. */
	private XmlRpcSerializer serializer = new XmlRpcSerializer();
}
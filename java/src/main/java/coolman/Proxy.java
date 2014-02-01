package coolman;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A HTTP Proxy for servlet architecture.
 * 
 * @author oz
 * 
 */
public class Proxy implements Servlet {
	private Logger logger = LoggerFactory
			.getLogger(Proxy.class.getSimpleName());
	private ServletContext servletContext;
	private ServletConfig config;
	private HttpClient proxyClient;
	private String hostHeader;

	public void init(ServletConfig config) throws ServletException {
		this.config = config;
		this.servletContext = config.getServletContext();
		hostHeader = config.getInitParameter("HostHeader");
		try {
			initProxyClient(config);
			if (servletContext != null) {
				servletContext.setAttribute(
						config.getServletName() + ".Logger", logger);
				servletContext.setAttribute(config.getServletName()
						+ ".ThreadPool", proxyClient.getThreadPool());

				servletContext.setAttribute(config.getServletName()
						+ ".HttpClient", proxyClient);
			}

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Init and set up the servlet. This method sets up a thread pool. The
	 * thread pool parameters are confiruable from web.xml
	 * 
	 * @param config
	 * @return
	 * @throws Exception
	 */
	private void initProxyClient(ServletConfig config) throws Exception {
		// Create a client to forward the requests.
		proxyClient = new HttpClient();
		// Use the select architecture that does not require a thread per socket
		// approach.
		proxyClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		String paramStr; // A temp variable to hold a configuration parameter
		// Allows configuration of maxThreads in the web.xml, for Android set a
		// small number of threads.
		paramStr = config.getInitParameter("maxThreads");
		// Init and create a thread pool.
		if (paramStr != null)
			proxyClient.setThreadPool(new QueuedThreadPool(Integer
					.parseInt(paramStr)));
		else
			proxyClient.setThreadPool(new QueuedThreadPool());

		// The following block is to allow thread pool configuration from the
		// servlet declaration in web.xml
		// {

		// The timeout for http response to arrive.
		paramStr = config.getInitParameter("timeout");
		if (paramStr != null) {
			proxyClient.setTimeout(Long.parseLong(paramStr));
		}

		// The maximum number of connections that can stay open
		paramStr = config.getInitParameter("maxConnections");
		if (paramStr != null) {
			proxyClient.setMaxConnectionsPerAddress(Integer.parseInt(paramStr));
		}

		// This one is very important, it indicates how long the connection can
		// stay open.
		// Setting this number too high can lead to thread starvation or DOS
		paramStr = config.getInitParameter("idleTimeout");

		if (paramStr != null) {
			proxyClient.setIdleTimeout(Long.parseLong(paramStr));
		}

		// The size of headers
		paramStr = config.getInitParameter("requestHeaderSize");

		if (paramStr != null) {
			proxyClient.setRequestHeaderSize(Integer.parseInt(paramStr));
		}

		paramStr = config.getInitParameter("requestBufferSize");

		if (paramStr != null) {
			proxyClient.setRequestBufferSize(Integer.parseInt(paramStr));
		}

		paramStr = config.getInitParameter("responseHeaderSize");

		if (paramStr != null) {
			proxyClient.setResponseHeaderSize(Integer.parseInt(paramStr));
		}

		paramStr = config.getInitParameter("responseBufferSize");

		if (paramStr != null) {
			proxyClient.setResponseBufferSize(Integer.parseInt(paramStr));
		}

		proxyClient.start();
	}

	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		final HttpServletRequest request = (HttpServletRequest) req;
		final HttpServletResponse response = (HttpServletResponse) res;

		if ("CONNECT".equalsIgnoreCase(request.getMethod())) {
			throw new ServletException("CONNECT not implemented");
		}

		// Get tne in and out stream of the requests
		final InputStream in = request.getInputStream();
		final OutputStream out = response.getOutputStream();

		// A HTTP asnyc operation support requires a ContinuationSupport
		final Continuation continuation = ContinuationSupport
				.getContinuation(request);

		if (!continuation.isInitial())
			response.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT);
		else {

			String uri = request.getRequestURI();
			if (request.getQueryString() != null)
				uri += "?" + request.getQueryString();

			HttpURI url = proxyHttpURI(request.getScheme(),
					request.getServerName(), request.getServerPort(), uri);

			logger.info("Proxy " + uri + "-->" + url);

			if (url == null) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				logger.error("URL is null, returning FORBIDDEN");
				return;
			}

			//Exchange = (request & response) 
			HttpExchange exchange = new HttpExchange() {
				protected void onRequestCommitted() throws IOException {
				}

				protected void onRequestComplete() throws IOException {
					logger.debug("onRequestComplete");
				}

				protected void onResponseComplete() throws IOException {
					logger.debug(" onResponseComplete");
					continuation.complete();
				}

				protected void onResponseContent(Buffer content)
						throws IOException {
					content.writeTo(out);
				}

				protected void onResponseHeaderComplete() throws IOException {
				}

				@SuppressWarnings("deprecation")
				protected void onResponseStatus(Buffer version, int status,
						Buffer reason) throws IOException {

					if (reason != null && reason.length() > 0)
						response.setStatus(status, reason.toString());
					else
						response.setStatus(status);
				}

				protected void onResponseHeader(Buffer name, Buffer value)
						throws IOException {
					response.addHeader(name.toString(), value.toString());
				}

				protected void onConnectionFailed(Throwable ex) {
					handleOnConnectionFailed(ex, request, response);
					if (!continuation.isInitial()) {
						continuation.complete();
					}
				}

				protected void onException(Throwable ex) {
					if (ex instanceof EofException) {
						Log.ignore(ex);
						return;
					}
					handleOnException(ex, request, response);
					if (!continuation.isInitial()) {
						continuation.complete();
					}
				}

				protected void onExpire() {
					continuation.complete();
				}

			};

			// Some sites try to use https
			exchange.setScheme(HttpSchemes.HTTPS.equals(request.getScheme()) ? HttpSchemes.HTTPS_BUFFER
					: HttpSchemes.HTTP_BUFFER);

			exchange.setMethod(request.getMethod());
			exchange.setURL(url.toString());
			exchange.setVersion(request.getProtocol());

			// check connection header
			String connectionHdr = request.getHeader("Connection");
			if (connectionHdr != null) {
				connectionHdr = connectionHdr.toLowerCase();
				if (connectionHdr.indexOf("keep-alive") < 0
						&& connectionHdr.indexOf("close") < 0)
					connectionHdr = null;
			}

			// force host
			if (hostHeader != null)
				exchange.setRequestHeader("Host", hostHeader);

			boolean hasContent = false;
			long contentLength = -1;
			Enumeration<?> headerNamesEnum = request.getHeaderNames();
			while (headerNamesEnum.hasMoreElements()) {
				String headerStr = (String) headerNamesEnum.nextElement();
				String headerLowerCaseStr = headerStr.toLowerCase();
				if (connectionHdr != null
						&& connectionHdr.indexOf(headerLowerCaseStr) >= 0)
					continue;
				if (hostHeader != null && "host".equals(headerLowerCaseStr))
					continue;

				if ("content-type".equals(headerLowerCaseStr))
					hasContent = true;
				else if ("content-length".equals(headerLowerCaseStr)) {
					contentLength = request.getContentLength();
					exchange.setRequestHeader(HttpHeaders.CONTENT_LENGTH,
							Long.toString(contentLength));
					if (contentLength > 0)
						hasContent = true;
				}
				Enumeration<?> requestHeaderValuesEnum = request.getHeaders(headerStr);
				while (requestHeaderValuesEnum.hasMoreElements()) {
					String valueStr = (String) requestHeaderValuesEnum.nextElement();
					if (valueStr != null) {
						exchange.setRequestHeader(headerStr, valueStr);
					}
				}
			}

			// Put the proxy headers
			exchange.setRequestHeader("Via", "1.1 (JetWaf Proxy!)");
			if (hasContent)
				exchange.setRequestContentSource(in);

			// This code makes sure that the proxy uses the same timeout
			// values as the original client.
			// Otherwise some timeout can occur in some web applications.
			
			long ctimeout = (proxyClient.getTimeout() >= exchange.getTimeout()) ? proxyClient
					.getTimeout() : exchange.getTimeout();

			if (ctimeout == 0) {
				continuation.setTimeout(0);
			} else {
				continuation.setTimeout(ctimeout + 1000);
			}
			continuation.suspend(response);
			// Forward the HTTP exchange to the client, this does the actual
			// sending.
			try {
			proxyClient.send(exchange);}
			catch (Exception e) {
				logger.error("proxy exception:" + e);
				exchange.cancel();
			}
			
		}
	}

	private HttpURI proxyHttpURI(String schemeName, String hostName,
			int serverPort, String path) throws MalformedURLException {
		return new HttpURI(schemeName + "://" + hostName + ":" + serverPort
				+ path);
	}

	private void handleOnConnectionFailed(Throwable ex,
			HttpServletRequest request, HttpServletResponse response) {
		handleOnException(ex, request, response);
	}

	private void handleOnException(Throwable ex, HttpServletRequest request,
			HttpServletResponse response) {
		Log.warn(ex.toString());
		Log.debug(ex);
		if (!response.isCommitted()) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	public void destroy() {
		try {
			proxyClient.stop();
		} catch (Exception x) {
			logger.debug("error", x);
		}
	}

	public ServletConfig getServletConfig() {
		return config;
	}

	public String getHostHeader() {
		return hostHeader;
	}

	public void setHostHeader(String hostHeader) {
		this.hostHeader = hostHeader;
	}

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}

package coolman;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSSFilter implements Filter {

	private Logger logger = LoggerFactory.getLogger(XSSFilter.class
			.getSimpleName());
	private Boolean denyMatching;


	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
	
		try {
		XSSDetectHttpServletRequest httpServletRequest = new XSSDetectHttpServletRequest(
				(HttpServletRequest) request, denyMatching);
		if (httpServletRequest.isMalformed() && denyMatching) {
			logger.info("Blocking request" + request);
			blockResponse(response);
			return;
		} else {
			chain.doFilter(httpServletRequest, response);
		}
		} catch (Throwable t ) {
			logger.error("error:" + t);
		}

	}

	private void blockResponse(ServletResponse response) {
		PrintWriter out = null;

		response.setContentType("text/html");
		try {
			out = response.getWriter();
		} catch (IOException e) {
			logger.error("Error generating block message", e);
		}
		out.println("<html><head><title>Request blocked</title></head><body>");
		out.println("<h2>Out you go.</h2>");
		out.println("</body></html>");
		out.close();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		denyMatching = Boolean.valueOf(filterConfig.getInitParameter("denyMatching"));
		logger.debug("deny matching:" +  denyMatching);		
	}

}

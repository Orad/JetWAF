package coolman;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter of the HttpServletRequest that allows a filter to read the stream
 * safely.
 * 
 * @author oz
 * 
 */
public class XSSDetectHttpServletRequest extends HttpServletRequestWrapper {
	private Logger logger = LoggerFactory.getLogger(XSSDetectHttpServletRequest.class
			.getSimpleName());

	private byte[] body;
	private boolean malformed;
	private String enc;
	private  final Pattern AMP_RE = Pattern.compile("&");
	private  final Pattern GT_RE = Pattern.compile(">");
	private  final Pattern LT_RE = Pattern.compile("<");
	private  final Pattern SQUOT_RE = Pattern.compile("\'");
	private  final Pattern QUOT_RE = Pattern.compile("\"");
	private  final String HTML_ENTITY_REGEX = "[a-z]+|#[0-9]+|#x[0-9a-fA-F]+";

	public XSSDetectHttpServletRequest(HttpServletRequest httpServletRequest, boolean denyMatching)
			throws IOException {
		super(httpServletRequest);
		// Read the request body and save it as a byte array
		InputStream is = super.getInputStream();
		enc = detectEncoding(); // TODO: not secured!
		body = toByteArray(is);
		String strbody = new String(body, enc);
		// Since the browser sends the post data URL-encoded, we must decode
		// them first.
		// The attacker can trick this by not url-encoding the post data,
		// needs further checking.
		strbody = URLDecoder.decode(strbody, enc);
		if (denyMatching) { 
			strbody = scan(strbody);
		}
		else {
		strbody = sanitizeByPatterns(strbody);
		}
		body = strbody.getBytes();
		//body = htmlEscapeAllowEntities(strbody).getBytes();
	}

	/**
	 * This does the actual work of matching against the patterns. It will raise
	 * the malformed flag, indicating that this http request is malformed.
	 * 
	 * @param str
	 *            The string to be checked.
	 * @return A sanitized string.
	 * @throws UnsupportedEncodingException
	 */
	public String scan(String str) throws UnsupportedEncodingException {

		if (str == null || str.isEmpty()) {
			return str; // nothing to do here
		}
		for (Pattern scriptPattern : Patterns.patterns) {
			Matcher matcher = scriptPattern.matcher(str);
			boolean find = matcher.find(); // or matcher.replaceAll()
			if (find) {
				logger.debug("Pattern matched:" + scriptPattern);
				this.malformed = true;
				break; // NOTE: If you want to sanitize and not just detect,
						// remove the break to continue to the next pattern.
			}
		}
		return str;
	}

	/**
	 * Another method for checking the stream without converting to string. This
	 * can also be used to replace several chars.
	 * 
	 * @param inputStream
	 * @return
	 */
	public String sanitizeByPatterns(String str) throws UnsupportedEncodingException {

		if (str == null || str.isEmpty()) {
			return str;
		}
		for (Pattern scriptPattern : Patterns.patterns) {
			Matcher matcher = scriptPattern.matcher(str);
			str= matcher.replaceAll("FORBIDDEN");
		}
		return str;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new ServletInputStreamImpl(new ByteArrayInputStream(body));
	}

	@Override
	public BufferedReader getReader() throws IOException {
		enc = detectEncoding();
		return new BufferedReader(new InputStreamReader(getInputStream(), enc));
	}

	/**
	 * Detect the encoding of the stream based on the encoding header. This is
	 * not recommended since the attacker can modify the header and lie about
	 * the actual encoding (encoding attack). TODO: use ESAPI.encoder()
	 * 
	 * @return
	 */
	private String detectEncoding() {
		enc = getCharacterEncoding();
		if (enc == null)
			enc = "UTF-8";
		return enc;
	}

	/**
	 * A input stream adapter that allows the filter to read the stream before
	 * passing it forward on the chain. This is required because servlet streams
	 * are only readble once. This class overrides the markSupported method,
	 * forcing the servlet to accept the stream again.
	 * 
	 * @author oz
	 * 
	 */
	private class ServletInputStreamImpl extends ServletInputStream {

		private InputStream is;

		public ServletInputStreamImpl(InputStream is) {
			this.is = is;
		}

		public int read() throws IOException {
			return is.read();
		}

		public boolean markSupported() {
			return false;
		}

		public synchronized void mark(int i) {
			throw new RuntimeException(new IOException(
					"mark/reset not supported"));
		}

		public synchronized void reset() throws IOException {
			throw new IOException("mark/reset not supported");
		}
	}

	public byte[] getBody() {
		return body;
	}

	public boolean isMalformed() {
		return malformed;
	}

	/**
	 * Utility method to convert InputStream to ByteArray
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public byte[] toByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int l;
		byte[] data = new byte[1024];
		while ((l = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, l);
		}
		buffer.flush();
		return buffer.toByteArray();
	}

	public String htmlEscape(String s) {
		// if (s.indexOf("&") != -1) {
		// s = AMP_RE.matcher(s).replaceFirst("&amp;");
		// }
		//
		if (s.indexOf("<") != -1) {
			s = LT_RE.matcher(s).replaceAll("&lt;");
		}
		if (s.indexOf(">") != -1) {
			s = GT_RE.matcher(s).replaceAll("&gt;");
		}

		// if (s.indexOf("\"") != -1) {
		// s = QUOT_RE.matcher(s).replaceAll("&quot;");
		// }
		// if (s.indexOf("\'") != -1) {
		// s = SQUOT_RE.matcher(s).replaceAll("&#39;");
		// }
		return s;
	}

	public String htmlEscapeAllowEntities(String text) {
		StringBuilder escaped = new StringBuilder();

		boolean firstSegment = true;
		for (String segment : text.split("&", -1)) {
			if (firstSegment) {
				/*
				 * The first segment is never part of an entity reference, so we
				 * always escape it. Note that if the input starts with an
				 * ampersand, we will get an empty segment before that.
				 */
				firstSegment = false;
				escaped.append(htmlEscape(segment));
				continue;
			}

			int entityEnd = segment.indexOf(';');
			if (entityEnd > 0
					&& segment.substring(0, entityEnd).matches(
							HTML_ENTITY_REGEX)) {
				// Append the entity without escaping.
				escaped.append("&").append(segment.substring(0, entityEnd + 1));

				// Append the rest of the segment, escaped.
				escaped.append(htmlEscape(segment.substring(entityEnd + 1)));
			} else {
				// The segment did not start with an entity reference, so escape
				// the
				// whole segment.
				escaped.append("&amp;").append(htmlEscape(segment));
			}
		}

		return escaped.toString();
	}

}
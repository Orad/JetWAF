package coolman;

import java.util.regex.Pattern;
/**
 * List of patterns to be checked.
 * Please explain the pattern you add.
 * @author oz
 *
 */
public class Patterns {
	 final static Pattern[] patterns = new Pattern[] {		 
		 		 
		 	//<script>...</script>
			Pattern.compile("<[\\s]*s[\\s]*c[\\s]*r[\\s]*i[\\s]*p[\\s]*t[\\s]*>(.*?)<[\\s]*/[\\s]*s[\\s]*c[\\s]*r[\\s]*i[\\s]*p[\\s]*t[\\s]*>", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//<...script>...
			Pattern.compile("<[\\s]*(.*?)s[\\s]*c[\\s]*r[\\s]*i[\\s]*p[\\s]*t[\\s]*>(.*?)", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
			
			//<...script...>
			Pattern.compile("<(.*?)s[\\s]*c[\\s]*r[\\s]*i[\\s]*p[\\s]*t(.*?)>(.*?)", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
			
			
			//src='...'
			Pattern.compile("s[\\s]*r[\\s]*c[\\s]*[\r\n]*[\\s]*=[\\s]*[\r\n]*\\\'(.*?)\\\'",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
							| Pattern.DOTALL),
							
			//src="..."				
			Pattern.compile("s[\\s]*r[\\s]*c[\\s]*[\r\n]*[\\s]*=[\\s]*[\r\n]*\\\"(.*?)\\\"",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
							| Pattern.DOTALL),
							
							
			//lonely script tags
			Pattern.compile("<[\\s]*/[\\s]*s[\\s]*c[\\s]*r[\\s]*i[\\s]*p[\\s]*t>", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
			
			//<script...>
			Pattern.compile("<[\\s]*s[\\s]*c[\\s]*r[\\s]*i[\\s]*p[\\s]*t(.*?)>", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
					
			// eval(...)
			Pattern.compile("e[\\s]*v[\\s]*a[\\s]*l[\\s]*\\((.*?)\\)", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
					
			// expression(...)
			Pattern.compile("e[\\s]*x[\\s]*p[\\s]*r[\\s]*e[\\s]*s[\\s]*s[\\s]*i[\\s]*o[\\s]*n[\\s]*\\((.*?)\\)", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
					
			// javascript:...
			Pattern.compile("j[\\s]*a[\\s]*v[\\s]*a[\\s]*s[\\s]*c[\\s]*r[\\s]*i[\\s]*p[\\s]*t[\\s]*:", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
			
			
			// vbscript:...
			Pattern.compile("v[\\s]*b[\\s]*s[\\s]*c[\\s]*r[\\s]*i[\\s]*p[\\s]*t[\\s]*:", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
			
			
			//----------------------javascript events----------------------//
			
			//onload=
			Pattern.compile("o[\\s]*n[\\s]*l[\\s]*o[\\s]*a[\\s]*d(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//onload=
			Pattern.compile("o[\\s]*n[\\s]*u[\\s]*n[\\s]*l[\\s]*o[\\s]*a[\\s]*d(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//onfocus=
			Pattern.compile("o[\\s]*n[\\s]*f[\\s]*o[\\s]*c[\\s]*u[\\s]*s(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//onblur=
			Pattern.compile("o[\\s]*n[\\s]*b[\\s]*l[\\s]*u[\\s]*r(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//onchange=
			Pattern.compile("o[\\s]*n[\\s]*c[\\s]*h[\\s]*a[\\s]*n[\\s]*g[\\s]*e(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//onsubmit=
			Pattern.compile("o[\\s]*n[\\s]*s[\\s]*u[\\s]*b[\\s]*m[\\s]*i[\\s]*t(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//onmouseover=
			Pattern.compile("o[\\s]*n[\\s]*m[\\s]*o[\\s]*u[\\s]*s[\\s]*e[\\s]*o[\\s]*v[\\s]*e[\\s]*r(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//onmouseout=
			Pattern.compile("o[\\s]*n[\\s]*m[\\s]*o[\\s]*u[\\s]*s[\\s]*e[\\s]*o[\\s]*u[\\s]*t(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//onmousedown=
			Pattern.compile("o[\\s]*n[\\s]*m[\\s]*o[\\s]*u[\\s]*s[\\s]*e[\\s]*d[\\s]*o[\\s]*w[\\s]*n(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//onmouseup=
			Pattern.compile("o[\\s]*n[\\s]*m[\\s]*o[\\s]*u[\\s]*s[\\s]*e[\\s]*u[\\s]*p(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//onclick=
			Pattern.compile("o[\\s]*n[\\s]*c[\\s]*l[\\s]*i[\\s]*c[\\s]*k(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
					
			//----------------------wierdo cases----------------------//
			
			//<...img src..=&
			Pattern.compile("<(.*?)I[\\s]*M[\\s]*G[\\s]*S[\\s]*R[\\s]*C(.*?)=[\\s]*&", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//<...a href...=
			/*Pattern.compile("<(.*?)A[\\s]*H[\\s]*R[\\s]*E[\\s]*F(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
			*/		
			//<...iframe...=
			Pattern.compile("<(.*?)i[\\s]*f[\\s]*r[\\s]*a[\\s]*m[\\s]*e(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
							
			//<meta http-equaiv...=
			Pattern.compile("<[\\s]*M[\\s]*E[\\s]*T[\\s]*A[\\s]*H[\\s]*T[\\s]*T[\\s]*P[\\s]*-[\\s]*E[\\s]*Q[\\s]*U[\\s]*I[\\s]*V(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//<...style
			Pattern.compile("(.*?)<(.*?)S[\\s]*T[\\s]*Y[\\s]*L[\\s]*E", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//...style...=
			Pattern.compile("(.*?)S[\\s]*T[\\s]*Y[\\s]*L[\\s]*E(.*?)=", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//\\";...;//
			Pattern.compile("\\\";(.*?);//", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL),
					
			//&{
			Pattern.compile("\\&[\\s]*\\{", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL)

	};
}

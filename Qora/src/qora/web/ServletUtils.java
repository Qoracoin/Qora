package qora.web;

import javax.servlet.http.HttpServletRequest;

public class ServletUtils {
	
	
	public static boolean isRemoteRequest(HttpServletRequest servletRequestOpt)
	{
		if(servletRequestOpt != null)
		{
			String ipAdress = getRemoteAddress(servletRequestOpt);
			
			if (!ipAdress.equals("127.0.0.1")) {
				return true;
			}
		}
		
		return false;
	}

	public static String getRemoteAddress(
			HttpServletRequest servletRequest) {
		String ipAdress = servletRequest.getHeader("X-FORWARDED-FOR");
		
		if (ipAdress == null) {
			ipAdress = servletRequest.getRemoteAddr();
		}
		return ipAdress;
	}
	
}

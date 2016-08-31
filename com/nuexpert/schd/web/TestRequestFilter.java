package com.nuexpert.schd.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

public class TestRequestFilter implements Filter{
	private static final Logger log = Logger.getLogger(TestRequestFilter.class);

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		StringBuilder s = new StringBuilder();
		
		for (String key: request.getParameterMap().keySet()){
			String value = request.getParameter(key);
			
			s.append(key).append(":=").append(value).append("\n");
		}
		
		log.debug("==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ");
		log.debug("====>" + s.toString());
		log.debug("==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ");
		
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterChain) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}

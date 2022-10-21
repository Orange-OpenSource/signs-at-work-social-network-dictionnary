package com.orange.signsatwork.biz.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A servlet filter to log request and response
 * The logging implementation is pretty native and for demonstration only
 * @author hemant
 *
 */
@Component
public class RequestResponseLoggingFilter implements Filter {

  private final static Logger LOG = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    LOG.info("Initializing filter :{}", this);
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    LOG.info("Logging Request  {} : {}", req.getMethod(), req.getRequestURI());
    long startTime = System.currentTimeMillis();
    chain.doFilter(request, response);
    long endTime = System.currentTimeMillis();
    long duration = (endTime - startTime);
    LOG.info("Logging Response, Duration :{} {} ", res.getContentType(), millisToShortDHMS(duration));
  }

  @Override
  public void destroy() {
    LOG.warn("Destructing filter :{}", this);
  }

  public static String millisToShortDHMS(long duration) {
    String res = "";    // java.util.concurrent.TimeUnit;
    long days       = TimeUnit.MILLISECONDS.toDays(duration);
    long hours      = TimeUnit.MILLISECONDS.toHours(duration) -
      TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
    long minutes    = TimeUnit.MILLISECONDS.toMinutes(duration) -
      TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
    long seconds    = TimeUnit.MILLISECONDS.toSeconds(duration) -
      TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
    long millis     = TimeUnit.MILLISECONDS.toMillis(duration) -
      TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(duration));

    if (days == 0)      res = String.format("%02d:%02d:%02d.%04d", hours, minutes, seconds, millis);
    else                res = String.format("%dd %02d:%02d:%02d.%04d", days, hours, minutes, seconds, millis);
    return res;
  }
}

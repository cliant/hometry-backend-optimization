package homeTry.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SlowApiInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SlowApiInterceptor.class);
    private static final long SLOW_API_THRESHOLD_MS = 300;
    private static final String START_TIME_ATTR = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime == null) return;

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > SLOW_API_THRESHOLD_MS) {
            log.warn("SLOW API [{} {}] {} ms", request.getMethod(), request.getRequestURI(), elapsed);
        }
    }
}
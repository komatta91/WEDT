package pl.edu.pw.elka.studia.wedt.interceptors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by User on 2016-08-09.
 */
public class VersionInterceptor extends HandlerInterceptorAdapter {
    @Value("${app.version}")
    String version;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            modelAndView.addObject("version", version);
        }
    }
}



/**
 *
 */
package eu.researchalps.api.util;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author gibs
 * @date 10 juin 2015
 */
public class UserLocaleResolver implements HandlerMethodArgumentResolver {

    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    /**
     * @see HandlerMethodArgumentResolver#resolveArgument(MethodParameter, ModelAndViewContainer, NativeWebRequest, WebDataBinderFactory)
     */
    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer arg1, NativeWebRequest webRequest, WebDataBinderFactory arg3) throws Exception {
        if (this.supportsParameter(methodParameter)) {
            final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            final boolean onlyOne = methodParameter.getParameterType().equals(Locale.class);

            List<Locale> locales = new LinkedList<>();
            final Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("locale")) {
                        final Locale locale = Locale.forLanguageTag(cookie.getValue());
                        if (onlyOne) {
                            return locale;
                        }
                        locales.add(locale);
                    }
                }
            }
            final Enumeration<Locale> l = request.getLocales();
            if (l != null) {
                if (onlyOne) {
                    if (l.hasMoreElements())
                        return l.nextElement();
                }
                while (l.hasMoreElements()) {
                    locales.add(l.nextElement());
                }
            }
            if (locales.isEmpty()) {
                if (onlyOne) {
                    return DEFAULT_LOCALE;
                }
                return Collections.singletonList(DEFAULT_LOCALE);
            }
            return locales;
        }
        return WebArgumentResolver.UNRESOLVED;
    }

    /**
     * @see HandlerMethodArgumentResolver#supportsParameter(MethodParameter)
     */
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(UserLocale.class) != null &&
                (methodParameter.getParameterType().equals(Locale.class) || methodParameter.getParameterType().equals(List.class));
    }


}

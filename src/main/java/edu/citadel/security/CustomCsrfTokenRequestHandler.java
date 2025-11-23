package edu.citadel.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.function.Supplier;

public class CustomCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final XorCsrfTokenRequestAttributeHandler delegate =
            new XorCsrfTokenRequestAttributeHandler();
    private String[] csrfRequestHeader = {"X-XSRF-TOKEN", "X-CSRF-TOKEN"};

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        this.delegate.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        Assert.notNull(csrfToken, "CsrfToken must not be null");

        String actualToken = Arrays.stream(csrfRequestHeader)
                .map(request::getHeader)
                .filter(val -> val != null && !val.isEmpty())
                .filter(headerValue ->
                        headerValue.equals(csrfToken.getToken()))
                .findFirst()
                .orElse(null);
        if (actualToken != null) {
            return actualToken;
        }

        actualToken = request.getParameter(csrfToken.getParameterName());
        return actualToken;
    }

}

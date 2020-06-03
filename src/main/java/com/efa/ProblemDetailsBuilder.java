package com.efa;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.web.context.request.WebRequest;

/**
 * Analogous to {@link org.springframework.boot.web.servlet.error.ErrorAttributes ErrorAttributes}, but
 * returns {@link Problem Problem Details}.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7807">Problem Details for HTTP APIs</a>
 */
public interface ProblemDetailsBuilder {
    
    Problem getProblemDetails(WebRequest webRequest, ErrorAttributeOptions options);
    
}

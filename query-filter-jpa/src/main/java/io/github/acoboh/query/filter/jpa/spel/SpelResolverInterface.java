package io.github.acoboh.query.filter.jpa.spel;

import org.springframework.util.MultiValueMap;

public interface SpelResolverInterface {

	public Object evaluate(String securityExpression, MultiValueMap<String, Object> contextValues);
}

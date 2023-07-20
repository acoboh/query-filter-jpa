package org.query.filter.jpa.openapi.data.jpa.config;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.github.acoboh.query.filter.jpa.processor.QFParamType;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;

class QFEndpoint {

	private final String endpoint;
//	private final Class<?> queryFilterClass; // TODO Check usage
	private final Parameter parameter;
	private final QFProcessor<?, ?> processor;
	private RequestMethod requestMethod;
	private final String parameterName;
	private final QFParamType paramType;

	public QFEndpoint(Class<?> controller, Method method, Parameter param, QFProcessor<?, ?> processor,
			String parameterName, QFParamType paramType) {

//		this.queryFilterClass = param.getAnnotation(QueryFilterParam.class).value();
		this.processor = processor;

		StringBuilder path = new StringBuilder();

		if (controller.isAnnotationPresent(RequestMapping.class)) {
			RequestMapping rm = controller.getAnnotation(RequestMapping.class);
			if (rm.value().length > 0)
				path.append(rm.value()[0]);
		}

		path.append(pathMethod(method));

		endpoint = path.toString();
		this.parameter = param;
		this.parameterName = parameterName;
		this.paramType = paramType;

	}

	private String pathMethod(Method method) {
		if (method.isAnnotationPresent(RequestMapping.class)) {

			RequestMapping rm = method.getAnnotation(RequestMapping.class);
			if (rm.value().length > 0) {
				requestMethod = rm.method()[0];
				return rm.value()[0];
			}

		}

		if (method.isAnnotationPresent(GetMapping.class)) {
			GetMapping get = method.getAnnotation(GetMapping.class);
			requestMethod = RequestMethod.GET;
			if (get.value().length > 0) {
				return get.value()[0];
			}
		}

		return "";

	}

	public String getEndpoint() {
		return endpoint;
	}

	public RequestMethod getRequestMethod() {
		return requestMethod;
	}

	public Parameter getParameter() {
		return parameter;
	}

	public QFProcessor<?, ?> getProcessor() {
		return processor;
	}

	public String getParameterName() {
		return parameterName;
	}

	public QFParamType getParamType() {
		return paramType;
	}

}

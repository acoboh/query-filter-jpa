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

	/**
	 * <p>
	 * Constructor for QFEndpoint.
	 * </p>
	 *
	 * @param controller    a {@link java.lang.Class} object
	 * @param method        a {@link java.lang.reflect.Method} object
	 * @param param         a {@link java.lang.reflect.Parameter} object
	 * @param processor     a {@link io.github.acoboh.query.filter.jpa.processor.QFProcessor} object
	 * @param parameterName a {@link java.lang.String} object
	 * @param paramType     a {@link io.github.acoboh.query.filter.jpa.processor.QFParamType} object
	 */
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

	/**
	 * <p>
	 * Getter for the field <code>endpoint</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * <p>
	 * Getter for the field <code>requestMethod</code>.
	 * </p>
	 *
	 * @return a {@link org.springframework.web.bind.annotation.RequestMethod} object
	 */
	public RequestMethod getRequestMethod() {
		return requestMethod;
	}

	/**
	 * <p>
	 * Getter for the field <code>parameter</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.reflect.Parameter} object
	 */
	public Parameter getParameter() {
		return parameter;
	}

	/**
	 * <p>
	 * Getter for the field <code>processor</code>.
	 * </p>
	 *
	 * @return a {@link io.github.acoboh.query.filter.jpa.processor.QFProcessor} object
	 */
	public QFProcessor<?, ?> getProcessor() {
		return processor;
	}

	/**
	 * <p>
	 * Getter for the field <code>parameterName</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getParameterName() {
		return parameterName;
	}

	/**
	 * <p>
	 * Getter for the field <code>paramType</code>.
	 * </p>
	 *
	 * @return a {@link io.github.acoboh.query.filter.jpa.processor.QFParamType} object
	 */
	public QFParamType getParamType() {
		return paramType;
	}

}

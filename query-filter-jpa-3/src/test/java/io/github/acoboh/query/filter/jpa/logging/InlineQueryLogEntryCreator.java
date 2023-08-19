package io.github.acoboh.query.filter.jpa.logging;

import java.util.Arrays;
import java.util.List;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

/**
 * @author Vlad Mihalcea
 */
public class InlineQueryLogEntryCreator extends DefaultQueryLogEntryCreator {

	@Override
	protected void writeParamsEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
		sb.append("Parameters:[");

		for (QueryInfo queryInfo : queryInfoList) {

			String prefixArg = "";
			for (List<ParameterSetOperation> params : queryInfo.getParametersList()) {

				sb.append(prefixArg);
				prefixArg = ", ";
				sb.append("(");

				String prefixParam = "";
				for (ParameterSetOperation param : params) {
					sb.append(prefixParam);
					prefixParam = ", ";

					Object parameter = param.getArgs()[1];
					if (parameter != null && parameter.getClass().isArray()) {
						sb.append(arrayToString(parameter));
					} else {
						sb.append(parameter);
					}
				}
				sb.append(")");
			}

		}
		sb.append("]");

	}

	private String arrayToString(Object object) {
		if (object.getClass().isArray()) {
			if (object instanceof byte[]) {
				return Arrays.toString((byte[]) object);
			}
			if (object instanceof short[]) {
				return Arrays.toString((short[]) object);
			}
			if (object instanceof char[]) {
				return Arrays.toString((char[]) object);
			}
			if (object instanceof int[]) {
				return Arrays.toString((int[]) object);
			}
			if (object instanceof long[]) {
				return Arrays.toString((long[]) object);
			}
			if (object instanceof float[]) {
				return Arrays.toString((float[]) object);
			}
			if (object instanceof double[]) {
				return Arrays.toString((double[]) object);
			}
			if (object instanceof boolean[]) {
				return Arrays.toString((boolean[]) object);
			}
			if (object instanceof Object[]) {
				return Arrays.toString((Object[]) object);
			}
		}
		throw new UnsupportedOperationException("Array type not supported: " + object.getClass());
	}

}
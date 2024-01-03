package io.github.acoboh.query.filter.jpa.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.Pair;

import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

class QueryUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryUtils.class);

	private QueryUtils() {

	}

	public static Path<?> getObject(Root<?> root, List<QFPath> paths, Map<String, Path<?>> pathsMap,
			boolean isCollection) {
		String fullPath = getFullPath(paths, isCollection);

		Path<?> ret = pathsMap.get(fullPath);
		if (ret != null) {
			return ret;
		}

		if (paths.size() == 1 && paths.get(0).isFinal()) {
			ret = root.get(paths.get(0).getPath());
		} else {
			ret = getJoinObject(root, paths, pathsMap);
		}

		pathsMap.put(fullPath, ret);
		return ret;

	}

	public static Path<?> getJoinObject(Root<?> root, List<QFPath> paths, Map<String, Path<?>> pathsMap) {

		From<?, ?> join = root;

		StringBuilder base = new StringBuilder();
		String prefix = "";

		for (int i = 0; i < paths.size(); i++) {

			base.append(prefix).append(paths.get(i).getPath());
			prefix = ".";

			Path<?> pathRet = pathsMap.get(base.toString());
			if (pathRet != null) {
				join = (From<?, ?>) pathRet;
				continue;
			}

			if (i + 1 == paths.size() && paths.get(i).isFinal()) { // if last element and final
				return join.get(paths.get(i).getPath());
			}

			QFPath elem = paths.get(i);
			switch (elem.getType()) {
			case LIST:
				join = join.joinList(elem.getPath());
				break;

			case SET:
				join = join.joinSet(elem.getPath());
				break;
			case PROPERTY:
			case ENUM:
			default:
				join = join.join(elem.getPath());
				break;
			}

			// Add to pathsMap

			pathsMap.put(base.toString(), join);

		}

		return join;

	}

	public static List<Order> parseOrders(List<Pair<IDefinitionSortable, Direction>> sortDefinitionList,
			CriteriaBuilder cb, Root<?> root, Map<String, Path<?>> pathsMap) {
		ArrayList<Order> orderList = new ArrayList<>();

		for (Pair<IDefinitionSortable, Direction> pair : sortDefinitionList) {
			LOGGER.trace("Adding sort operation for {}", pair);
			if (pair.getSecond() == Direction.ASC) {
				orderList.add(cb.asc(getObject(root, pair.getFirst().getSortPaths(), pathsMap, false)));
			} else {
				orderList.add(cb.desc(getObject(root, pair.getFirst().getSortPaths(), pathsMap, false)));
			}

		}

		return orderList;
	}

	public static String getFullPath(List<QFPath> paths, boolean isCollection) {
		String path = paths.stream().map(QFPath::getPath).collect(Collectors.joining("."));
		return isCollection ? path + ".*" : path;
	}
}

package io.github.acoboh.query.filter.jpa.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.Pair;

import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;

class QueryUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryUtils.class);

	private QueryUtils() {

	}

	public static Path<?> getObject(Root<?> root, List<QFPath> paths, Map<String, Path<?>> pathsMap,
			boolean isCollection, boolean tryFetch) {
		String fullPath = getFullPath(paths, isCollection);

		Path<?> ret = pathsMap.get(fullPath);
		if (ret != null) {
			return ret;
		}

		if (paths.size() == 1 && paths.get(0).isFinal()) {
			ret = root.get(paths.get(0).getPath());
		} else {
			ret = getJoinObject(root, paths, pathsMap, tryFetch);
		}

		pathsMap.put(fullPath, ret);
		return ret;

	}

	public static Path<?> getJoinObject(Root<?> root, List<QFPath> paths, Map<String, Path<?>> pathsMap,
			boolean tryFetch) {

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

			if (tryFetch) {
				join = (From<?, ?>) root.fetch(elem.getPath());
			} else {
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

			int index = 0;
			for (List<QFPath> paths : pair.getFirst().getSortPaths()) {
				boolean autoFetch = pair.getFirst().isAutoFetch(index++);
				LOGGER.trace("Autofetch is enabled on sort");
				Path<?> path = getObject(root, paths, pathsMap, false, autoFetch);
				Order order = pair.getSecond() == Direction.ASC ? cb.asc(path) : cb.desc(path);
				orderList.add(order);
			}

		}

		return orderList;
	}

	public static String getFullPath(List<QFPath> paths, boolean isCollection) {
		String path = paths.stream().map(QFPath::getPath).collect(Collectors.joining("."));
		return isCollection ? path + ".*" : path;
	}
}

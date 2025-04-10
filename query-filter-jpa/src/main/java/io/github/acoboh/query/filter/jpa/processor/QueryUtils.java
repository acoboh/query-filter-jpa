package io.github.acoboh.query.filter.jpa.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.Pair;

import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;

/**
 * Class with query utilities
 */
public class QueryUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryUtils.class);

	private QueryUtils() {

	}

	/**
	 * Get the object join
	 *
	 * @param root
	 *            root entity
	 * @param paths
	 *            paths to travel
	 * @param pathsMap
	 *            map of older paths
	 * @param isCollection
	 *            if the final join is part of a collection or object
	 * @param tryFetch
	 *            try to use fetch instead of join
	 * @param cb
	 *            criteria builder
	 * @return return the final path of the object
	 */
	public static Path<?> getObject(Root<?> root, List<QFPath> paths, Map<String, Path<?>> pathsMap,
			boolean isCollection, boolean tryFetch, CriteriaBuilder cb) {
		String fullPath = getFullPath(paths, isCollection);

		Path<?> ret = pathsMap.get(fullPath);
		if (ret != null) {
			return ret;
		}

		if (paths.size() == 1 && paths.get(0).isFinal()) {

			QFPath firstPath = paths.get(0);

			if (firstPath.getTreatClass() != null) {
				root = getTreatCast(root, firstPath.getTreatClass(), cb);
			}

			ret = root.get(paths.get(0).getPath());
		} else {
			ret = getJoinObject(root, paths, pathsMap, tryFetch, cb);
		}

		pathsMap.put(fullPath, ret);
		return ret;

	}

	private static Path<?> getJoinObject(Root<?> root, List<QFPath> paths, Map<String, Path<?>> pathsMap,
			boolean tryFetch, CriteriaBuilder cb) {

		From<?, ?> join = root;

		StringBuilder base = new StringBuilder();
		String prefix = "";

		for (int i = 0; i < paths.size(); i++) {

			base.append(prefix).append(paths.get(i).getPathName());
			prefix = ".";

			Path<?> pathRet = pathsMap.get(base.toString());
			if (pathRet != null) {
				join = (From<?, ?>) pathRet;
				continue;
			}

			Class<?> asTreat = paths.get(i).getTreatClass();

			if (asTreat != null && !Void.class.equals(asTreat)) {
				join = getTreatCast(join, asTreat, cb);
			}

			if (i + 1 == paths.size() && paths.get(i).isFinal()) { // if last element and final
				return join.get(paths.get(i).getPath());
			}

			QFPath elem = paths.get(i);

			if (tryFetch) {
				join = (From<?, ?>) join.fetch(elem.getPath());
			} else {
				switch (elem.getType()) {
					case LIST :
						join = join.joinList(elem.getPath());
						break;

					case SET :
						join = join.joinSet(elem.getPath());
						break;
					case PROPERTY :
					case ENUM :
					default :
						join = join.join(elem.getPath());
						break;
				}
			}

			// Add to pathsMap

			pathsMap.put(base.toString(), join);

		}

		return join;

	}

	/**
	 * Parse orders with the criteria builder
	 *
	 * @param sortDefinitionList
	 *            list of sort definitions
	 * @param cb
	 *            criteria builder
	 * @param root
	 *            root entity
	 * @param pathsMap
	 *            older paths
	 * @return the final order list
	 */
	public static List<Order> parseOrders(List<Pair<IDefinitionSortable, Direction>> sortDefinitionList,
			CriteriaBuilder cb, Root<?> root, Map<String, Path<?>> pathsMap) {
		ArrayList<Order> orderList = new ArrayList<>();

		for (Pair<IDefinitionSortable, Direction> pair : sortDefinitionList) {
			LOGGER.trace("Adding sort operation for {}", pair);

			int index = 0;
			for (List<QFPath> paths : pair.getFirst().getPaths()) {
				boolean autoFetch = pair.getFirst().isAutoFetch(index++);
				LOGGER.trace("Autofetch is enabled on sort");
				Path<?> path = getObject(root, paths, pathsMap, false, autoFetch, cb);
				Order order = pair.getSecond() == Direction.ASC ? cb.asc(path) : cb.desc(path);
				orderList.add(order);
			}

		}

		return orderList;
	}

	private static String getFullPath(List<QFPath> paths, boolean isCollection) {
		String path = paths.stream().map(QFPath::getPathName).collect(Collectors.joining("."));
		return isCollection ? path + ".*" : path;
	}

	@SuppressWarnings("unchecked")
	private static <X, T extends X> Root<T> getTreatCast(Root<?> root, Class<?> type, CriteriaBuilder cb) {
		Root<X> castedRoot = (Root<X>) root;
		Class<T> castedType = (Class<T>) type;
		return cb.treat(castedRoot, castedType);
	}

	@SuppressWarnings("unchecked")
	private static <X, T, V extends T> From<?, ?> getTreatCast(From<?, ?> join, Class<?> type, CriteriaBuilder cb) {

		if (join instanceof Root) {
			Root<T> castedRoot = (Root<T>) join;
			Class<V> castedType = (Class<V>) type;
			return cb.treat(castedRoot, castedType);
		} else if (join instanceof Join) {
			Join<X, T> castedJoin = (Join<X, T>) join;
			Class<V> castedType = (Class<V>) type;
			return cb.treat(castedJoin, castedType);
		} else {
			throw new IllegalArgumentException("Join type not supported");
		}

	}

}

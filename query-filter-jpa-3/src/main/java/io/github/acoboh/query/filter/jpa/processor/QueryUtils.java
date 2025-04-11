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
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.CollectionAttribute;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

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
	 *            Criteria Builder
	 * @return return the final path of the object
	 */
	@SuppressWarnings("unchecked")
	public static Path<?> getObject(Root<?> root, List<QFAttribute> paths, Map<String, Path<?>> pathsMap,
			boolean isCollection, boolean tryFetch, CriteriaBuilder cb) {
		String fullPath = getFullPath(paths, isCollection);

		if (pathsMap.containsKey(fullPath)) {
			return pathsMap.get(fullPath);
		}

		From<?, ?> join = root;

		int index = -1;

		StringBuilder base = new StringBuilder();
		String prefix = "";
		for (var path : paths) {
			LOGGER.trace("Processing path {}", path);
			index++;

			base.append(prefix).append(path.getPathName());
			prefix = ".";

			Path<?> pathRet = pathsMap.get(base.toString());
			if (pathRet != null) {
				join = (From<?, ?>) pathRet;
				continue;
			}

			if (path.getTreatClass() != null) {
				if (index == 0) {
					root = getTreatCast(root, paths.get(0).getTreatClass(), cb);
				} else {
					join = getTreatCast(join, path.getTreatClass(), cb);
				}
			}

			var att = path.getAttribute();
			if ((att instanceof SingularAttribute<?, ?> singularAttribute) && (index + 1 == paths.size())) {
				return join.get((SingularAttribute) singularAttribute);
			}

			join = getNextJoin(join, att, tryFetch);

			pathsMap.put(base.toString(), join);

		}

		pathsMap.put(fullPath, join);
		return join;

	}

	private static From<?, ?> getNextJoin(From<?, ?> join, Attribute<?, ?> att, boolean tryFetch) {
		if (tryFetch) {
			return (From<?, ?>) join.fetch(att.getName());
		} else {
			if (att instanceof SingularAttribute<?, ?> singular) {
				return join.join((SingularAttribute) singular);
			} else if (att instanceof CollectionAttribute<?, ?> collectionAtt) {
				return join.join((CollectionAttribute) collectionAtt);
			} else if (att instanceof ListAttribute<?, ?> listAtt) {
				return join.join((ListAttribute) listAtt);
			} else if (att instanceof SetAttribute<?, ?> setAtt) {
				return join.join((SetAttribute) setAtt);
			}
		}

		throw new IllegalArgumentException("Attribute " + att + " is not a singular or plural attribute");

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
				join = switch (elem.getType()) {
					case LIST -> join.joinList(elem.getPath());
					case SET -> join.joinSet(elem.getPath());
					case PROPERTY, ENUM -> join.join(elem.getPath());
				};
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
			for (var paths : pair.getFirst().getPaths()) {
				boolean autoFetch = pair.getFirst().isAutoFetch(index++);
				LOGGER.trace("Autofetch is enabled on sort");
				Path<?> path = getObject(root, paths, pathsMap, false, autoFetch, cb);
				Order order = pair.getSecond() == Direction.ASC ? cb.asc(path) : cb.desc(path);
				orderList.add(order);
			}

		}

		return orderList;
	}

	private static String getFullPath(List<QFAttribute> paths, boolean isCollection) {
		String path = paths.stream().map(QFAttribute::getPathName).collect(Collectors.joining("."));
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

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
import jakarta.persistence.criteria.JoinType;
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
 *
 * @author Adrián Cobo
 */
public class QueryUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryUtils.class);

	private QueryUtils() {

	}

	/**
	 * Get the object join
	 *
	 * @param paths
	 *            paths to travel
	 * @param pathsMap
	 *            map of older paths
	 * @param isCollection
	 *            if the final join is part of a collection or object
	 * @param tryFetch
	 *            try to use fetch instead of join
	 * @return return the final path of the object
	 * @param queryInfo
	 *            info and data about the query
	 * @param joinTypes
	 *            a {@link java.util.List} object
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static Path<?> getObject(QueryInfo<?> queryInfo, List<QFAttribute> paths, List<JoinType> joinTypes,
			Map<String, Path<?>> pathsMap, boolean isCollection, boolean tryFetch) {
		String fullPath = getFullPath(paths, isCollection);

		if (pathsMap.containsKey(fullPath)) {
			return pathsMap.get(fullPath);
		}

		Root<?> root = queryInfo.root();

		From<?, ?> join = root;

		int index = -1;

		StringBuilder base = new StringBuilder();
		String prefix = "";
		for (var path : paths) {
			LOGGER.trace("Processing path {}", path);
			index++;

			JoinType joinType = getJoinType(joinTypes, path, index);

			base.append(prefix).append(path.getPathName()).append("##").append(joinType);
			prefix = ".";

			Path<?> pathRet = pathsMap.get(base.toString());
			if (pathRet != null) {
				join = (From<?, ?>) pathRet;
				continue;
			}

			if (path.getTreatClass() != null) {
				if (index == 0) {
					root = getTreatCast(root, paths.get(0).getTreatClass(), queryInfo.cb());
				} else {
					join = getTreatCast(join, path.getTreatClass(), queryInfo.cb());
				}
			}

			var att = path.getAttribute();
			if ((att instanceof SingularAttribute<?, ?> singularAttribute) && (index + 1 == paths.size())) {
				return join.get((SingularAttribute) singularAttribute);
			}

			join = getNextJoin(join, att, tryFetch, queryInfo.isCount(), joinType);

			pathsMap.put(base.toString(), join);

		}

		pathsMap.put(fullPath, join);
		return join;

	}

	private static JoinType getJoinType(List<JoinType> joinTypes, QFAttribute path, int index) {
		int jIndex = Math.min(index, joinTypes.size() - 1);
		JoinType joinType;
		if (jIndex < 0) {
			LOGGER.trace("Join type not defined for path {}. Using default join type", path);
			joinType = JoinType.INNER;
		} else {
			LOGGER.trace("Using join type {} for path {}", joinTypes.get(jIndex), path);
			joinType = joinTypes.get(jIndex);
		}
		return joinType;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static From<?, ?> getNextJoin(From<?, ?> join, Attribute<?, ?> att, boolean tryFetch, boolean isCount,
			JoinType joinType) {
		if (tryFetch && !isCount) {
			return (From<?, ?>) join.fetch(att.getName(), joinType);
		} else {
			if (att instanceof SingularAttribute<?, ?> singular) {
				return join.join((SingularAttribute) singular, joinType);
			} else if (att instanceof CollectionAttribute<?, ?> collectionAtt) {
				return join.join((CollectionAttribute) collectionAtt, joinType);
			} else if (att instanceof ListAttribute<?, ?> listAtt) {
				return join.join((ListAttribute) listAtt, joinType);
			} else if (att instanceof SetAttribute<?, ?> setAtt) {
				return join.join((SetAttribute) setAtt, joinType);
			}
		}

		throw new IllegalArgumentException("Attribute " + att + " is not a singular or plural attribute");

	}

	/**
	 * Parse orders with the criteria builder
	 *
	 * @param sortDefinitionList
	 *            list of sort definitions
	 * @param pathsMap
	 *            older paths
	 * @return the final order list
	 * @param queryInfo
	 *            info and data about the query
	 */
	public static List<Order> parseOrders(QueryInfo<?> queryInfo,
			List<Pair<IDefinitionSortable, Direction>> sortDefinitionList, Map<String, Path<?>> pathsMap) {
		ArrayList<Order> orderList = new ArrayList<>();

		for (Pair<IDefinitionSortable, Direction> pair : sortDefinitionList) {
			LOGGER.trace("Adding sort operation for {}", pair);
			int index = 0;
			for (var paths : pair.getFirst().getPaths()) {
				boolean autoFetch = pair.getFirst().isAutoFetch(index);
				LOGGER.trace("Autofetch is enabled on sort");
				Path<?> path = getObject(queryInfo, paths, pair.getFirst().getJoinTypes(index), pathsMap, false,
						autoFetch);
				Order order = pair.getSecond() == Direction.ASC ? queryInfo.cb().asc(path) : queryInfo.cb().desc(path);
				orderList.add(order);
				index++;
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

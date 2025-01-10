package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.ParentEntity.TypeEnum;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.RelatedParent;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.SubclassAEntity;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.SubclassBEntity;

/**
 * Parent entity discriminators filter class
 */
@QFDefinitionClass(RelatedParent.class)
public class FilterRelatedParentEntityDef {

	@QFElement("parent.type")
	private TypeEnum type;

	@QFDiscriminator(path = "parent", value = {@QFDiscriminator.Value(name = "subclassA", type = SubclassAEntity.class),
			@QFDiscriminator.Value(name = "subclassB", type = SubclassBEntity.class)})
	private String discriminatorType;

	@QFElement(value = "parent.subClassField", subClassMapping = SubclassAEntity.class, subClassMappingPath = "parent")
	private String subClassAField;

}

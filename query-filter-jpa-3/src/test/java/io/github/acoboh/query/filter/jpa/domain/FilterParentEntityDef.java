package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.ParentEntity;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.ParentEntity.TypeEnum;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.SubclassAEntity;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.SubclassBEntity;

/**
 * Parent entity discriminators filter class
 */
@QFDefinitionClass(ParentEntity.class)
public class FilterParentEntityDef {

	@QFElement("type")
	private TypeEnum type;

	@QFDiscriminator({ @QFDiscriminator.Value(name = "subclassA", type = SubclassAEntity.class),
			@QFDiscriminator.Value(name = "subclassB", type = SubclassBEntity.class) })
	private String discriminatorType;

	@QFElement(value = "subClassField", subClassMapping = SubclassAEntity.class)
	private String subClassAField;

}

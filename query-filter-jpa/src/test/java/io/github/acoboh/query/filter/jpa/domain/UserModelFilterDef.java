package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.model.subquery.UserModel;

@QFDefinitionClass(UserModel.class)
public class UserModelFilterDef {

	@QFElement("username")
	private String username;

	@QFElement(value = "roles.name", subquery = true)
	private String role;
	
	@QFElement(value = "roles.name")
	private String roleNotSub;

}

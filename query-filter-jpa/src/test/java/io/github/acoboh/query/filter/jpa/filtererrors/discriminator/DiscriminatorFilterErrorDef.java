package io.github.acoboh.query.filter.jpa.filtererrors.discriminator;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.model.discriminators.Announcement;
import io.github.acoboh.query.filter.jpa.model.discriminators.Topic;

@QFDefinitionClass(Topic.class)
public class DiscriminatorFilterErrorDef {

	@QFDiscriminator({ @QFDiscriminator.Value(name = "ANNOUNCEMENT", type = Announcement.class),
			@QFDiscriminator.Value(name = "POST", type = PostBlog.class) })
	private String type;

}

package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.model.discriminators.Announcement;
import io.github.acoboh.query.filter.jpa.model.discriminators.Post;
import io.github.acoboh.query.filter.jpa.model.discriminators.Topic;

/**
 * Basic discriminator query filter definition
 *
 * @author Adrián Cobo
 */
@QFDefinitionClass(Topic.class)
public class DiscriminatorFilterDef {

    @QFDiscriminator({@QFDiscriminator.Value(name = "ANNOUNCEMENT", type = Announcement.class),
            @QFDiscriminator.Value(name = "POST", type = Post.class)})
    private String type;

}

package io.github.acoboh.query.filter.jpa.model.discriminators;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity(name = "Topic")
@Table(name = "topic")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.INTEGER, name = "topic_type_id", columnDefinition = "SMALLINT")
public class Topic {

	@Id
	@GeneratedValue
	private Long id;

	private String title;

	private String owner;

	private Timestamp created = Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));

	public Topic() {

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Long getId() {
		return id;
	}

	public Date getCreated() {
		return created;
	}

	@Override
	public int hashCode() {
		return Objects.hash(created, id, owner, title);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!obj.getClass().isAssignableFrom(getClass()))
			return false;
		Topic other = (Topic) obj;
		return Objects.equals(created, other.created) && Objects.equals(id, other.id)
				&& Objects.equals(owner, other.owner) && Objects.equals(title, other.title);
	}

}

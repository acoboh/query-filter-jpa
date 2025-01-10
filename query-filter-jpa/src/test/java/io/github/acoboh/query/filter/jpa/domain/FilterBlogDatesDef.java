package io.github.acoboh.query.filter.jpa.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import io.github.acoboh.query.filter.jpa.annotations.QFDate;
import io.github.acoboh.query.filter.jpa.annotations.QFDate.QFDateDefault;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.model.PostBlog;

/**
 * Basic dates query filter definition
 *
 * @author Adrián Cobo
 */
@QFDefinitionClass(PostBlog.class)
public class FilterBlogDatesDef {

	@QFDate
	@QFElement("createDate")
	private LocalDateTime createDateDefault;

	@QFDate(timeFormat = "yyyy-MM-dd HH:mm:ss")
	@QFElement("createDate")
	private LocalDateTime createDateCustomFormat;

	@QFSortable("lastTimestamp")
	private Timestamp lastTimestampSortable;

	@QFDate(timeFormat = "yyyy/MM/dd HH:mm")
	@QFElement("createDate")
	private LocalDateTime lastTimestampCustomFormat;

	@QFDate(timeFormat = "yyyy/MM/dd", parseDefaulting = {
			@QFDateDefault(chronoField = ChronoField.HOUR_OF_DAY, value = 12),
			@QFDateDefault(chronoField = ChronoField.MINUTE_OF_HOUR, value = 30),
			@QFDateDefault(chronoField = ChronoField.SECOND_OF_MINUTE, value = 0)})
	@QFElement("createDate")
	private LocalDateTime withDefaults;

}

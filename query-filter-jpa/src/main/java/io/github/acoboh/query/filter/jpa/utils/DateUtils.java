package io.github.acoboh.query.filter.jpa.utils;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import io.github.acoboh.query.filter.jpa.annotations.QFDate;
import io.github.acoboh.query.filter.jpa.annotations.QFDate.QFDateDefault;

public class DateUtils {

	private DateUtils() {

	}

	public static DateTimeFormatter getFormatter(QFDate dateAnnotation) {
		DateTimeFormatter formatter;

		if (dateAnnotation.parseDefaulting() != null & dateAnnotation.parseDefaulting().length > 0) {
			DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
					.appendPattern(dateAnnotation.timeFormat());

			for (QFDateDefault def : dateAnnotation.parseDefaulting()) {
				builder.parseDefaulting(def.chronoField(), def.value());
			}

			formatter = builder.toFormatter();

		} else {
			formatter = DateTimeFormatter.ofPattern(dateAnnotation.timeFormat());
		}

		ZoneId zone = ZoneId.of(dateAnnotation.zoneOffset());
		formatter.withZone(zone);
		return formatter;
	}

	public static Object parseDate(DateTimeFormatter formatter, String value, Class<?> finalClass,
			QFDate dateAnnotation) {
		if (Timestamp.class.isAssignableFrom(finalClass)) {
			LocalDateTime dt = LocalDateTime.parse(value, formatter);
			return Timestamp.valueOf(dt);
		} else if (LocalDateTime.class.isAssignableFrom(finalClass)) {
			return LocalDateTime.parse(value, formatter);
		} else if (LocalDate.class.isAssignableFrom(finalClass)) {
			return LocalDate.parse(value, formatter);
		} else if (ZonedDateTime.class.isAssignableFrom(finalClass)) {
			return ZonedDateTime.parse(value, formatter);
		} else if (Date.class.isAssignableFrom(finalClass)) {
			LocalDate ld = LocalDate.parse(value, formatter);
			return Date.valueOf(ld);
		} else if (java.util.Date.class.isAssignableFrom(finalClass)) {
			LocalDateTime dt = LocalDateTime.parse(value, formatter);
			return java.util.Date.from(dt.toInstant(ZoneOffset.of(dateAnnotation.zoneOffset())));
		} else if (java.sql.Date.class.isAssignableFrom(finalClass)) {
			LocalDate ld = LocalDate.parse(value, formatter);
			return Date.valueOf(ld);
		}

		return null;

	}
}

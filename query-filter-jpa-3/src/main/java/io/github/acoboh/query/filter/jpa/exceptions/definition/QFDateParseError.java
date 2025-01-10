package io.github.acoboh.query.filter.jpa.exceptions.definition;

import java.io.Serial;
import java.time.format.DateTimeParseException;

/**
 * Exception throw when any date parsing error occurs
 *
 * @author Adrián Cobo
 * 
 */
public class QFDateParseError extends QueryFilterDefinitionException {

	@Serial
    private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "The format '{}' is not valid for class '{}'. Please check logs";

	private final String format;
	private final Class<?> dateClass;

	/**
	 * Default constructor
	 *
	 * @param format    format applied on the date
	 * @param dateClass date class
	 * @param e         original exception thrown
	 */
	public QFDateParseError(String format, Class<?> dateClass, DateTimeParseException e) {
		super(MESSAGE, e, format, dateClass);
		this.format = format;
		this.dateClass = dateClass;
	}

	/**
	 * Get original format
	 *
	 * @return format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Get date class
	 *
	 * @return date class
	 */
	public Class<?> getDateClass() {
		return dateClass;
	}

}

package io.github.acoboh.query.filter.jpa.properties;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

/**
 * Limits applied to the filters parsed from the client input
 * ({@code query-filter.limits.*}). The programmatic API of
 * {@code QueryFilter} is not limited.
 *
 * @author Adrián Cobo
 */
@Validated
public class LimitsProperties {

    /**
     * Default maximum input length
     */
    public static final int DEFAULT_MAX_INPUT_LENGTH = 8192;

    /**
     * Default maximum number of filter parts
     */
    public static final int DEFAULT_MAX_FILTERS = 100;

    /**
     * Default maximum number of values of one filter part
     */
    public static final int DEFAULT_MAX_VALUES_PER_FIELD = 500;

    /**
     * Default maximum number of sort fields
     */
    public static final int DEFAULT_MAX_SORT_FIELDS = 10;

    @Positive
    private int maxInputLength = DEFAULT_MAX_INPUT_LENGTH;

    @Positive
    private int maxFilters = DEFAULT_MAX_FILTERS;

    @Positive
    private int maxValuesPerField = DEFAULT_MAX_VALUES_PER_FIELD;

    @Positive
    private int maxSortFields = DEFAULT_MAX_SORT_FIELDS;

    /**
     * Default constructor
     */
    public LimitsProperties() {
        // Default values
    }

    /**
     * Maximum length of the filter input (single param) or the sum of keys and
     * values (multi param)
     *
     * @return maximum length
     */
    public int getMaxInputLength() {
        return maxInputLength;
    }

    /**
     * Set the maximum length of the filter input
     *
     * @param maxInputLength new value
     */
    public void setMaxInputLength(int maxInputLength) {
        this.maxInputLength = maxInputLength;
    }

    /**
     * Maximum number of filter parts on one input
     *
     * @return maximum number of filters
     */
    public int getMaxFilters() {
        return maxFilters;
    }

    /**
     * Set the maximum number of filter parts
     *
     * @param maxFilters new value
     */
    public void setMaxFilters(int maxFilters) {
        this.maxFilters = maxFilters;
    }

    /**
     * Maximum number of values of one filter part (e.g. {@code in} lists)
     *
     * @return maximum number of values
     */
    public int getMaxValuesPerField() {
        return maxValuesPerField;
    }

    /**
     * Set the maximum number of values of one filter part
     *
     * @param maxValuesPerField new value
     */
    public void setMaxValuesPerField(int maxValuesPerField) {
        this.maxValuesPerField = maxValuesPerField;
    }

    /**
     * Maximum number of sort fields
     *
     * @return maximum number of sort fields
     */
    public int getMaxSortFields() {
        return maxSortFields;
    }

    /**
     * Set the maximum number of sort fields
     *
     * @param maxSortFields new value
     */
    public void setMaxSortFields(int maxSortFields) {
        this.maxSortFields = maxSortFields;
    }
}

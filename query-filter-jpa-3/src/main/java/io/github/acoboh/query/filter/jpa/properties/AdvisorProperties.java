package io.github.acoboh.query.filter.jpa.properties;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Advisor configuration properties
 *
 * @author Adrián Cobo
 */
@Validated
public class AdvisorProperties {

	private boolean enabled = true;

	@NotBlank
	private String messageSourceBaseName = "queryfilter-messages/messages";

	@NotBlank
	private String messageSourceDefaultEncoding = "UTF-8";

	private boolean messageSourceUseCodeAsDefaultMessage = true;

	private boolean extendErrorMessage = false;

	/**
	 * Get if the advisor is enabled
	 *
	 * @return true if enabled, false otherwise
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Modify status of advisor
	 *
	 * @param enabled
	 *            new status
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Get message source base name path
	 *
	 * @return message source base name path
	 */
	public String getMessageSourceBaseName() {
		return messageSourceBaseName;
	}

	/**
	 * Set new message source base name
	 *
	 * @param messageSourceBaseName
	 *            new message source base name
	 */
	public void setMessageSourceBaseName(String messageSourceBaseName) {
		this.messageSourceBaseName = messageSourceBaseName;
	}

	/**
	 * Get the message source encoding
	 *
	 * @return message source encoding
	 */
	public String getMessageSourceDefaultEncoding() {
		return messageSourceDefaultEncoding;
	}

	/**
	 * Set new message source encoding
	 *
	 * @param messageSourceDefaultEncoding
	 *            new message source encoding
	 */
	public void setMessageSourceDefaultEncoding(String messageSourceDefaultEncoding) {
		this.messageSourceDefaultEncoding = messageSourceDefaultEncoding;
	}

	/**
	 * Get if the message source use code as default message
	 *
	 * @return if the message source use code as default message
	 */
	public boolean isMessageSourceUseCodeAsDefaultMessage() {
		return messageSourceUseCodeAsDefaultMessage;
	}

	/**
	 * Set if the message source use code as default message
	 *
	 * @param messageSourceUseCodeAsDefaultMessage
	 *            if the message source use code as default message
	 */
	public void setMessageSourceUseCodeAsDefaultMessage(boolean messageSourceUseCodeAsDefaultMessage) {
		this.messageSourceUseCodeAsDefaultMessage = messageSourceUseCodeAsDefaultMessage;
	}

	/**
	 * Get if the error message should be extended
	 *
	 * @return if the error message should be extended
	 */
	public boolean isExtendErrorMessage() {
		return extendErrorMessage;
	}

	/**
	 * Set if the error message should be extended
	 *
	 * @param extendErrorMessage
	 *            if the error message should be extended
	 */
	public void setExtendErrorMessage(boolean extendErrorMessage) {
		this.extendErrorMessage = extendErrorMessage;
	}

}

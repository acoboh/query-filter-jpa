package io.github.acoboh.query.filter.jpa.properties;

import javax.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

@Validated
public class AdvisorProperties {

	private boolean enabled = true;

	@NotBlank
	private String messageSourceBaseName = "queryfilter-messages/messages";

	@NotBlank
	private String messageSourceDefaultEncoding = "UTF-8";

	private boolean messageSourceUseCodeAsDefaultMessage = true;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getMessageSourceBaseName() {
		return messageSourceBaseName;
	}

	public void setMessageSourceBaseName(String messageSourceBaseName) {
		this.messageSourceBaseName = messageSourceBaseName;
	}

	public String getMessageSourceDefaultEncoding() {
		return messageSourceDefaultEncoding;
	}

	public void setMessageSourceDefaultEncoding(String messageSourceDefaultEncoding) {
		this.messageSourceDefaultEncoding = messageSourceDefaultEncoding;
	}

	public boolean isMessageSourceUseCodeAsDefaultMessage() {
		return messageSourceUseCodeAsDefaultMessage;
	}

	public void setMessageSourceUseCodeAsDefaultMessage(boolean messageSourceUseCodeAsDefaultMessage) {
		this.messageSourceUseCodeAsDefaultMessage = messageSourceUseCodeAsDefaultMessage;
	}

}

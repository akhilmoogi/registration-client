package io.mosip.authentication.service.integration;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.constant.RestServicesConstants;
import io.mosip.authentication.core.exception.IDDataValidationException;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.authentication.core.util.dto.RestRequestDTO;
import io.mosip.authentication.service.factory.RestRequestFactory;
import io.mosip.authentication.service.helper.RestHelper;
import io.mosip.authentication.service.integration.dto.MailRequestDto;
import io.mosip.authentication.service.integration.dto.SmsRequestDto;
import io.mosip.authentication.service.integration.dto.SmsResponseDto;
import io.mosip.kernel.core.logger.spi.Logger;

/**
 * 
 * @author Dinesh Karuppiah.T
 */

@Component
public class NotificationManager {

	/** ID Template manager */
	@Autowired
	IdTemplateManager idTemplateManager;

	/** Environment */
	@Autowired
	private Environment environment;

	/** Rest Helper */
	@Autowired
	private RestHelper restHelper;

	/** Rest Request Factory */
	@Autowired
	private RestRequestFactory restRequestFactory;

	/** Logger to log the actions */
	private static Logger logger = IdaLogger.getLogger(NotificationManager.class);

	/** Constant to specify sender Auth Type */
	private static final String SENDER_AUTH = SenderType.AUTH.getName();

	/** Constant to specify sender OTP Type */
	private static final String SENDER_OTP = SenderType.OTP.getName();

	/** Property Name for Auth SMS Template */
	private static final String AUTH_SMS_TEMPLATE = "mosip.auth.sms.template";

	/** Property Name for OTP SMS Template */
	private static final String OTP_SMS_TEMPLATE = "mosip.otp.sms.template";

	/** Property Name for Auth Email Subject Template */
	private static final String AUTH_EMAIL_SUBJECT_TEMPLATE = "mosip.auth.mail.subject.template";

	/** Property Name for Auth Email Content Template */
	private static final String AUTH_EMAIL_CONTENT_TEMPLATE = "mosip.auth.mail.content.template";

	/** Property Name for OTP Subject Template */
	private static final String OTP_SUBJECT_TEMPLATE = "mosip.otp.mail.subject.template";

	/** Property Name for OTP Content Template */
	private static final String OTP_CONTENT_TEMPLATE = "mosip.otp.mail.content.template";

	/**
	 * Method to Send Notification to the Individual via SMS / E-Mail
	 * 
	 * @param notificationtype - specifies notification type
	 * @param values           - list of values to send notification
	 * @param emailId          - sender E-Mail ID
	 * @param phoneNumber      - sender Phone Number
	 * @param sender           - to specify the sender type
	 * @throws IdAuthenticationBusinessException
	 */
	public void sendNotification(Set<NotificationType> notificationtype, Map<String, Object> values, String emailId,
			String phoneNumber, String sender) throws IdAuthenticationBusinessException {
		String contentTemplate = null;
		String subjectTemplate = null;

		if (notificationtype.contains(NotificationType.SMS)) {

			if (SENDER_AUTH.equals(sender)) {
				contentTemplate = environment.getProperty(AUTH_SMS_TEMPLATE);
			} else if (SENDER_OTP.equals(sender)) {
				contentTemplate = environment.getProperty(OTP_SMS_TEMPLATE);
			}

			try {
				String smsTemplate = applyTemplate(values, contentTemplate);
				SmsRequestDto smsRequestDto = new SmsRequestDto();
				smsRequestDto.setMessage(smsTemplate);
				smsRequestDto.setNumber(phoneNumber);
				RestRequestDTO restRequestDTO = null;
				restRequestDTO = restRequestFactory.buildRequest(RestServicesConstants.NOTIFICATION_SERVICE,
						smsRequestDto, SmsResponseDto.class);
				restHelper.requestAsync(restRequestDTO);
			} catch (IDDataValidationException e) {
				logger.error("NA", "Inside SMS Notification >>>>>", e.getErrorCode(), e.getErrorText());
				throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.NOTIFICATION_FAILED, e);
			}

		}
		if (notificationtype.contains(NotificationType.EMAIL)) {

			if (SENDER_AUTH.equals(sender)) {
				subjectTemplate = environment.getProperty(AUTH_EMAIL_SUBJECT_TEMPLATE);
				contentTemplate = environment.getProperty(AUTH_EMAIL_CONTENT_TEMPLATE);
			} else if (SENDER_OTP.equals(sender)) {
				subjectTemplate = environment.getProperty(OTP_SUBJECT_TEMPLATE);
				contentTemplate = environment.getProperty(OTP_CONTENT_TEMPLATE);
			}

			try {
				String mailSubject = applyTemplate(values, subjectTemplate);
				String mailContent = applyTemplate(values, contentTemplate);
				MailRequestDto mailRequestDto = new MailRequestDto();
				mailRequestDto.setMailContent(mailContent);
				mailRequestDto.setMailSubject(mailSubject);
				mailRequestDto.setMailTo(new String[] { emailId });
				RestRequestDTO restRequestDTO = null;
				restRequestDTO = restRequestFactory.buildRequest(RestServicesConstants.NOTIFICATION_SERVICE,
						mailRequestDto, null);
				restHelper.requestAsync(restRequestDTO);
			} catch (IDDataValidationException e) {
				logger.error("NA", "Inside Mail Notification >>>>>", e.getErrorCode(), e.getErrorText());
				throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.NOTIFICATION_FAILED, e);
			}

		}

	}

	/**
	 * To apply Templates for Email or SMS Notifications
	 * 
	 * @param values       - content for Template
	 * @param templateName - Template name to fetch
	 * @return
	 * @throws IdAuthenticationBusinessException
	 */
	private String applyTemplate(Map<String, Object> values, String templateName)
			throws IdAuthenticationBusinessException {
		try {
			Objects.requireNonNull(templateName);
			return idTemplateManager.applyTemplate(templateName, values);
		} catch (IOException e) {
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.NOTIFICATION_FAILED, e);
		}
	}

}

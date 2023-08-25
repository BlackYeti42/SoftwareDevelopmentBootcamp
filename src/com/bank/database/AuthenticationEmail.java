package com.bank.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import jakarta.activation.DataHandler;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

public class AuthenticationEmail {

	private static final String SENDER;
	private static final String HOST;
	private static final String EMAIL_USERNAME;
	private static final String EMAIL_PASSWORD;
	private static final String PORT;
	private static final Properties PROPS = new Properties();

	static {

		// TODO encrypt the file email_details.txt.
		File emailDetails = new File(FilePath.get("SetterFiles", "email_details.txt"));
		Scanner fileInput = null;
		try {
			fileInput = new Scanner(emailDetails);
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		SENDER = EMAIL_USERNAME = fileInput.nextLine();
		HOST = fileInput.nextLine();
		EMAIL_PASSWORD = fileInput.nextLine();
		PORT = fileInput.nextLine();
		fileInput.close();

		PROPS.setProperty("mail.smtp.host", HOST);
		PROPS.setProperty("mail.smtp.port", PORT);
		PROPS.setProperty("mail.smtp.auth", "true");
		PROPS.setProperty("mail.smtp.starttls.enable", "true");
//		PROPS.setProperty("mail.debug", "true");		
	}

	public static void sendAuthenticationEmail(String username, TemporaryPassword tempPassword, DatabaseConnect dbConnection) {

		final String USER_EMAIL_QUERY = buildEmailQuery();
		final String RECIPIENT = findEmailByUsername(USER_EMAIL_QUERY, username, dbConnection);
		final String EMAIL_SUBJECT = "Your temporary password for the Bank of Phil.";
		final String DIRECTORY_NAME = "Assets";
		final String HTML_FILE_PATH = FilePath.get(DIRECTORY_NAME, "table.html");
		final String PNG1_FILE_PATH = FilePath.get(DIRECTORY_NAME, "BankofPhil.png");
		final String PNG2_FILE_PATH = FilePath.get(DIRECTORY_NAME, "TemporaryPassword.png");

		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
			}
		};
		Session session = Session.getInstance(PROPS, auth);
//		session.setDebug(true);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(SENDER));
			InternetAddress[] address = { new InternetAddress(RECIPIENT) };
			message.setRecipients(Message.RecipientType.TO, address);
			message.setSubject(EMAIL_SUBJECT);
			message.setSentDate(new Date());

			Multipart mp = new MimeMultipart("alternative");
			MimeBodyPart htmlPart = new MimeBodyPart();

			String htmlCode = ReadFile.readFile(HTML_FILE_PATH);
			htmlCode = htmlCode.replace("{{passwordVariable}}", tempPassword.getPassword());

			htmlPart.setContent(htmlCode, "text/html; charset=UTF-8");

			mp.addBodyPart(prepareImage(PNG1_FILE_PATH, "<image1>"));
			mp.addBodyPart(prepareImage(PNG2_FILE_PATH, "<image2>"));
			mp.addBodyPart(htmlPart);

			message.setContent(mp);

			Transport.send(message);
			System.out.print("Please check your email for a temporary password to complete your login.\n");

		} catch (MessagingException mex) {
			mex.printStackTrace();
			Exception ex = null;
			if ((ex = mex.getNextException()) != null) {
				ex.printStackTrace();
			}
			System.exit(1);
		}
	}

	private static MimeBodyPart prepareImage(final String FILE_PATH, final String VALUE) throws MessagingException {

		MimeBodyPart imagePart = new MimeBodyPart();
		byte[] fileBytes = null;

		try {
			fileBytes = Files.readAllBytes(Path.of(FILE_PATH));
		} catch (IOException e) {

			e.printStackTrace();
		}

		ByteArrayDataSource dataSource = new ByteArrayDataSource(fileBytes, "image/png");

		imagePart.setDataHandler(new DataHandler(dataSource));
		imagePart.setHeader("Content-ID", VALUE);
		imagePart.setDisposition(MimeBodyPart.INLINE);

		return imagePart;
	}

	private static String buildEmailQuery() {

		StringBuilder query = new StringBuilder();

		query.append("SELECT addresses.email ");
		query.append("FROM users ");
		query.append("JOIN addresses ON users.id = addresses.user_id ");
		query.append("WHERE users.username = ?");

		return query.toString();
	}

	private static String findEmailByUsername(String query, String username, DatabaseConnect dbConnection) {

		String recipient = null;
		try {
			ResultSet result = dbConnection.retrieve(query, username);
			if (result.next()) {
				recipient = result.getString(1);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return recipient;
	}
}

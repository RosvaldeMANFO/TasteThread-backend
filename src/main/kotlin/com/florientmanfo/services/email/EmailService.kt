package com.florientmanfo.com.florientmanfo.services.email

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.Message
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.common.io.BaseEncoding
import io.ktor.server.config.*
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.util.Properties

class EmailService(private val config: ApplicationConfig) {

    private lateinit var gmail: Gmail
    private val senderEmail: String = config.property("ktor.email.address").getString()
    private val senderName: String = config.property("ktor.email.name").getString()
    private val environment: String = config.property("ktor.environment").getString()


    fun configureGmailService() {
        val credentialsStream = when (environment) {
            "prod" -> javaClass.classLoader.getResourceAsStream("service-account-key.json")
            else -> {
                val keyFilePath = this::class.java.getResource("/service-account-key.json")
                    ?.path ?: throw Exception("Gmail service account key file not found")
                FileInputStream(keyFilePath)
            }
        }

        val googleCredentials = GoogleCredentials.fromStream(credentialsStream)
            .createScoped(listOf(GmailScopes.GMAIL_SEND))
            .createDelegated(senderEmail)

        gmail = Gmail.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(googleCredentials)
        )
            .setApplicationName("Cook")
            .build()
    }

    suspend fun sendActivationEmail(
        userEmail: String,
        userName: String,
        activationLink: String
    ): Result<Unit> {
        return try {
            configureGmailService()
            val htmlBodyContent = loadTemplate(
                "ActivateAccount.html",
                userName, activationLink
            ).trimIndent()

            val subject = "Activate your account - Cook"

            val message = createGmailMessage(
                recipientEmail = userEmail,
                senderEmail = senderEmail,
                senderName = senderName,
                subject = subject,
                htmlBody = htmlBodyContent
            )

            gmail.users().messages().send(senderEmail, message).execute()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(
        userEmail: String,
        userName: String,
        resetLink: String
    ): Result<Unit> {
        return try {
            configureGmailService()
            val htmlBodyContent = loadTemplate(
                "ChangePassword.html",
                userName, resetLink
            ).trimIndent()

            val subject = "Password Reset Request - Cook"

            val message = createGmailMessage(
                recipientEmail = userEmail,
                senderEmail = senderEmail,
                senderName = senderName,
                subject = subject,
                htmlBody = htmlBodyContent
            )

            gmail.users().messages().send(userEmail, message).execute()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun loadTemplate(templatePath: String, vararg args: String): String {
        val path = "templates/$templatePath"
        val templateStream = when(environment) {
            "prod" -> javaClass.classLoader.getResourceAsStream(path)
            else -> {
                val templateFilePath = this::class.java.getResource("/$path")
                    ?.path ?: throw Exception("Template file not found: /$path")
                FileInputStream(templateFilePath)
            }
        }
        if (templateStream == null) throw Exception("Template file not found: $path")
        return String.format(templateStream.bufferedReader().readText(), *args)
    }

    private fun createGmailMessage(
        recipientEmail: String,
        senderEmail: String,
        senderName: String,
        subject: String,
        htmlBody: String
    ): Message {
        val props = Properties()
        val session = Session.getDefaultInstance(props, null)

        val email = MimeMessage(session)

        email.setFrom(InternetAddress(senderEmail, senderName))
        email.addRecipient(MimeMessage.RecipientType.TO, InternetAddress(recipientEmail))
        email.subject = subject

        val multipart = MimeMultipart("alternative")

        val plainTextBodyPart = MimeBodyPart()
        plainTextBodyPart.setText("Please check the HTML version of this email for details.")
        multipart.addBodyPart(plainTextBodyPart)

        val htmlBodyPart = MimeBodyPart()
        htmlBodyPart.setContent(htmlBody, "text/html; charset=utf-8")
        multipart.addBodyPart(htmlBodyPart)

        email.setContent(multipart)

        val buffer = ByteArrayOutputStream()
        email.writeTo(buffer)
        val rawEmailBytes = buffer.toByteArray()

        val encodedEmail = BaseEncoding.base64Url()
            .omitPadding()
            .encode(rawEmailBytes)

        return Message().setRaw(encodedEmail)
    }
}
package com.todoapp.services

import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailService {

    private val env = dotenv()

    // Email configuration with fallbacks
    private val smtpHost = env["SMTP_HOST"] ?: "smtp.gmail.com"
    private val smtpPort = env["SMTP_PORT"]?.toIntOrNull() ?: 587
    private val smtpUsername = env["SMTP_USERNAME"] ?: ""
    private val smtpPassword = env["SMTP_PASSWORD"] ?: ""
    private val smtpFromEmail = env["SMTP_FROM_EMAIL"] ?: "noreply@todoapp.com"
    private val smtpFromName = env["SMTP_FROM_NAME"] ?: "Todo App"

    private val isEmailEnabled = smtpUsername.isNotBlank() && smtpPassword.isNotBlank()

    private val properties: Properties by lazy {
        Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", smtpHost)
            put("mail.smtp.port", smtpPort.toString())
            put("mail.smtp.ssl.trust", smtpHost)
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
            put("mail.smtp.writetimeout", "10000")
        }
    }

    private val session: Session by lazy {
        Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(smtpUsername, smtpPassword)
            }
        })
    }

    suspend fun sendShareNotification(
        toEmail: String,
        taskTitle: String,
        sharedBy: String,
        shareLink: String
    ): Boolean {
        if (!isEmailEnabled) {
            println("📧 Email disabled. Would send share notification to: $toEmail")
            return false
        }

        val subject = "$sharedBy shared a task with you"
        val htmlContent = createShareNotificationHtml(taskTitle, sharedBy, shareLink)

        return sendEmail(toEmail, subject, htmlContent)
    }

    suspend fun sendWelcomeEmail(toEmail: String, username: String): Boolean {
        if (!isEmailEnabled) {
            println("📧 Email disabled. Would send welcome email to: $toEmail")
            return false
        }

        val subject = "Welcome to Todo App!"
        val htmlContent = createWelcomeEmailHtml(username)

        return sendEmail(toEmail, subject, htmlContent)
    }

    suspend fun sendPasswordResetEmail(toEmail: String, resetToken: String): Boolean {
        if (!isEmailEnabled) {
            println("📧 Email disabled. Would send password reset email to: $toEmail")
            return false
        }

        val subject = "Reset Your Todo App Password"
        val resetLink = "https://your-app.com/reset-password?token=$resetToken"
        val htmlContent = createPasswordResetHtml(resetLink)

        return sendEmail(toEmail, subject, htmlContent)
    }

    suspend fun sendTaskDueReminder(
        toEmail: String,
        taskTitle: String,
        dueDate: String,
        taskLink: String
    ): Boolean {
        if (!isEmailEnabled) {
            println("📧 Email disabled. Would send task reminder to: $toEmail")
            return false
        }

        val subject = "Reminder: Task \"$taskTitle\" is due soon"
        val htmlContent = createTaskReminderHtml(taskTitle, dueDate, taskLink)

        return sendEmail(toEmail, subject, htmlContent)
    }

    private suspend fun sendEmail(
        toEmail: String,
        subject: String,
        htmlContent: String
    ): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(smtpFromEmail, smtpFromName))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                    this.subject = subject

                    val multipart = MimeMultipart().apply {
                        val htmlPart = MimeBodyPart().apply {
                            setContent(htmlContent, "text/html; charset=utf-8")
                        }
                        addBodyPart(htmlPart)
                    }

                    setContent(multipart)
                    sentDate = Date()
                }

                Transport.send(message)
                println("✅ Email sent successfully to: $toEmail")
                true
            }
        } catch (e: MessagingException) {
            println("❌ Failed to send email to $toEmail: ${e.message}")
            false
        } catch (e: Exception) {
            println("❌ Error sending email to $toEmail: ${e.message}")
            false
        }
    }

    private fun createShareNotificationHtml(
        taskTitle: String,
        sharedBy: String,
        shareLink: String
    ): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; }
                    .header { background: #4f46e5; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background: #f9fafb; }
                    .task-card { background: white; border-radius: 8px; padding: 20px; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .button { display: inline-block; background: #4f46e5; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; }
                    .footer { text-align: center; color: #6b7280; font-size: 14px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>📋 Task Shared With You</h1>
                </div>
                <div class="content">
                    <p>Hello,</p>
                    <p><strong>$sharedBy</strong> has shared a task with you on Todo App:</p>
                    
                    <div class="task-card">
                        <h3 style="margin-top: 0; color: #1f2937;">$taskTitle</h3>
                        <p>You can now view and collaborate on this task.</p>
                    </div>
                    
                    <p style="text-align: center; margin: 30px 0;">
                        <a href="$shareLink" class="button">View Task</a>
                    </p>
                    
                    <p>If the button doesn't work, copy and paste this link into your browser:</p>
                    <p style="background: #f3f4f6; padding: 10px; border-radius: 4px; word-break: break-all;">
                        $shareLink
                    </p>
                </div>
                <div class="footer">
                    <p>This email was sent by Todo App.</p>
                    <p>If you didn't expect this email, you can safely ignore it.</p>
                    <p>© ${Calendar.getInstance().get(Calendar.YEAR)} Todo App. All rights reserved.</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun createWelcomeEmailHtml(username: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; background: #f9fafb; }
                    .features { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; margin: 30px 0; }
                    .feature { background: white; padding: 15px; border-radius: 8px; text-align: center; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .button { display: inline-block; background: #667eea; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; }
                    .footer { text-align: center; color: #6b7280; font-size: 14px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1 style="margin: 0;">🎉 Welcome to Todo App!</h1>
                </div>
                <div class="content">
                    <h2>Hello, $username!</h2>
                    <p>Thank you for joining Todo App. We're excited to help you stay organized and productive.</p>
                    
                    <div class="features">
                        <div class="feature">
                            <h3 style="color: #667eea;">📝 Create Tasks</h3>
                            <p>Organize your work with tasks and subtasks</p>
                        </div>
                        <div class="feature">
                            <h3 style="color: #667eea;">👥 Share & Collaborate</h3>
                            <p>Share tasks with teammates and work together</p>
                        </div>
                        <div class="feature">
                            <h3 style="color: #667eea;">⏰ Set Reminders</h3>
                            <p>Never miss a deadline with smart reminders</p>
                        </div>
                        <div class="feature">
                            <h3 style="color: #667eea;">📱 Sync Everywhere</h3>
                            <p>Access your tasks on all your devices</p>
                        </div>
                    </div>
                    
                    <p style="text-align: center; margin: 30px 0;">
                        <a href="https://your-app.com/dashboard" class="button">Get Started</a>
                    </p>
                    
                    <p>If you have any questions or need help getting started, please don't hesitate to contact our support team.</p>
                </div>
                <div class="footer">
                    <p>© ${Calendar.getInstance().get(Calendar.YEAR)} Todo App. All rights reserved.</p>
                    <p><a href="https://your-app.com/unsubscribe" style="color: #6b7280;">Unsubscribe</a> | <a href="https://your-app.com/support" style="color: #6b7280;">Help Center</a></p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun createPasswordResetHtml(resetLink: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; }
                    .header { background: #dc2626; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background: #fef2f2; }
                    .warning { background: #fecaca; border-left: 4px solid #dc2626; padding: 15px; margin: 20px 0; }
                    .button { display: inline-block; background: #dc2626; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; }
                    .footer { text-align: center; color: #6b7280; font-size: 14px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🔐 Reset Your Password</h1>
                </div>
                <div class="content">
                    <p>Hello,</p>
                    <p>We received a request to reset your password for your Todo App account.</p>
                    
                    <div class="warning">
                        <p><strong>⚠️ Important:</strong> This link will expire in 1 hour.</p>
                        <p>If you didn't request this password reset, please ignore this email.</p>
                    </div>
                    
                    <p style="text-align: center; margin: 30px 0;">
                        <a href="$resetLink" class="button">Reset Password</a>
                    </p>
                    
                    <p>Or copy and paste this link into your browser:</p>
                    <p style="background: #f3f4f6; padding: 10px; border-radius: 4px; word-break: break-all;">
                        $resetLink
                    </p>
                    
                    <p>For security reasons, this link can only be used once and will expire in 1 hour.</p>
                </div>
                <div class="footer">
                    <p>This email was sent by Todo App.</p>
                    <p>If you didn't request this password reset, no action is needed.</p>
                    <p>© ${Calendar.getInstance().get(Calendar.YEAR)} Todo App. All rights reserved.</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun createTaskReminderHtml(
        taskTitle: String,
        dueDate: String,
        taskLink: String
    ): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; }
                    .header { background: #f59e0b; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background: #fffbeb; }
                    .reminder-card { background: white; border: 2px solid #f59e0b; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .due-date { color: #dc2626; font-weight: bold; }
                    .button { display: inline-block; background: #f59e0b; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; }
                    .footer { text-align: center; color: #6b7280; font-size: 14px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>⏰ Task Due Soon</h1>
                </div>
                <div class="content">
                    <p>Hello,</p>
                    <p>This is a friendly reminder about an upcoming task:</p>
                    
                    <div class="reminder-card">
                        <h3 style="margin-top: 0; color: #1f2937;">$taskTitle</h3>
                        <p><strong>Due Date:</strong> <span class="due-date">$dueDate</span></p>
                        <p>Don't forget to complete this task before the deadline!</p>
                    </div>
                    
                    <p style="text-align: center; margin: 30px 0;">
                        <a href="$taskLink" class="button">View Task</a>
                    </p>
                    
                    <p>If the button doesn't work, copy and paste this link into your browser:</p>
                    <p style="background: #f3f4f6; padding: 10px; border-radius: 4px; word-break: break-all;">
                        $taskLink
                    </p>
                </div>
                <div class="footer">
                    <p>This email was sent by Todo App.</p>
                    <p>To adjust your notification settings, visit your account preferences.</p>
                    <p>© ${Calendar.getInstance().get(Calendar.YEAR)} Todo App. All rights reserved.</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun testConnection(): Boolean {
        return try {
            val transport = session.transport
            transport.connect(smtpHost, smtpPort, smtpUsername, smtpPassword)
            transport.close()
            println("✅ Email service connection test successful")
            true
        } catch (e: Exception) {
            println("❌ Email service connection test failed: ${e.message}")
            false
        }
    }

    fun getEmailStatus(): Map<String, Any> {
        return mapOf(
            "enabled" to isEmailEnabled,
            "host" to smtpHost,
            "port" to smtpPort,
            "fromEmail" to smtpFromEmail,
            "fromName" to smtpFromName,
            "username" to (smtpUsername.takeIf { it.isNotBlank() }?.let {
                "${it.take(3)}***@${it.substringAfter('@').substringBeforeLast('.')}.***"
            } ?: "not configured")
        )
    }
}
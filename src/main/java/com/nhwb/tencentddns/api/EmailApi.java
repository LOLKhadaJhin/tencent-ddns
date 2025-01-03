package com.nhwb.tencentddns.api;

import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Date;

@Slf4j
public class EmailApi {
    private final JavaMailSender javaMailSender;

//private final Optional<JavaMailSender> javaMailSender;
//public EmailService(ApplicationContext applicationContext) {
//JavaMailSender javaMailSender = applicationContext.getBean(JavaMailSender.class, () -> null);

    public EmailApi(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendSimpleMessage(String from, String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setCc();
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送失败:{}", e.getMessage());
        }
    }

    public void sendMimeMessage(String from, String to, String subject, String text) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            InternetAddress internetAddress = new InternetAddress(from);
            internetAddress.setPersonal("DDNS");
            message.setFrom(internetAddress);     //设置发件人
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to, "用户", "utf-8"));      //设置收件人
            message.setSubject(subject, "utf-8");      //设置主题
            message.setSentDate(new Date());
            Multipart mul = new MimeMultipart();  //新建一个MimeMultipart对象来存放多个BodyPart对象
            BodyPart mdp = new MimeBodyPart();  //新建一个存放信件内容的BodyPart对象
            mdp.setContent(text, "text/html;charset=utf-8");
            mul.addBodyPart(mdp);  //将含有信件内容的BodyPart加入到MimeMultipart对象中
            message.setContent(mul); //把mul作为消息内容
            message.saveChanges();
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送失败:{}", e.getMessage());
        }
    }

    public void sendIpMessage(String emailAddress, String message) {
        sendSimpleMessage(emailAddress, emailAddress, "IP地址", message);
    }
    public void sendFailMessage(String emailAddress, String message) {
        sendSimpleMessage(emailAddress, emailAddress, "IP地址解析失败", message);
    }

}

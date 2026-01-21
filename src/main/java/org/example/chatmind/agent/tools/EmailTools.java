package org.example.chatmind.agent.tools;

import lombok.extern.slf4j.Slf4j;
import org.example.chatmind.service.EmailService;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class EmailTools implements  Tool{

    private final EmailService emailService;

    public EmailTools(EmailService emailService) {
        this.emailService = emailService;
    }
    @Override
    public String getName() {
        return "emailTool";
    }

    @Override
    public String getDescription() {
        return "一个用于发送邮件的工具，可以通过QQ邮箱发送邮件给指定的收件人。邮件发送采用异步方式，不会阻塞工具调用。";
    }

    @Override
    public ToolType getType() {
        return ToolType.OPTIONAL;
    }

    /**
     * 发送邮件（异步执行）
     *
     * @param to      收件人邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 发送结果信息
     */
    @org.springframework.ai.tool.annotation.Tool(name = "sendEmail",description = "发送邮件到指定的收件人。参数包括：to（收件人邮箱地址，必填）、subject（邮件主题，必填）、content（邮件正文内容，必填）。邮件采用异步方式发送，工具调用会立即返回，实际发送在后台执行。")
    public String sendEmail(String to, String subject, String content) {

        if (to == null || to.isEmpty()) {
            return "收件人邮箱地址不能为空";
        }
        if (subject == null || subject.isEmpty()) {
            return "邮件主题不能为空";
        }
        if (content == null || content.isEmpty()) {
            return "邮件内容不能为空";
        }
        //验证邮箱格式
        if (!to.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            return "收件人邮箱地址格式不正确";
        }
        emailService.sendEmail(to.trim(), subject.trim(), content.trim());
        log.info("邮件已提交异步发送，收件人: {}, 主题: {}", to, subject);
        return String.format("邮件已提交异步发送\n 收件人: %s \n主题: %s \n邮件正在后台异步发送中...", to, subject);
    }
}

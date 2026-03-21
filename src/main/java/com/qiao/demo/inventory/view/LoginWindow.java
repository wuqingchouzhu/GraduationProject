package com.qiao.demo.inventory.view;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiao.demo.inventory.dao.UserMapper;
import com.qiao.demo.inventory.model.User;
import com.qiao.demo.inventory.config.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class LoginWindow extends JFrame {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MainWindow mainWindow;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginWindow() {
        setTitle("系统登录");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.add(new JLabel("账 号:", SwingConstants.RIGHT));
        usernameField = new JTextField();
        panel.add(usernameField);
        panel.add(new JLabel("密 码:", SwingConstants.RIGHT));
        passwordField = new JPasswordField();
        panel.add(passwordField);
        panel.add(new JLabel("")); 
        loginButton = new JButton("登  录");
        panel.add(loginButton);
        add(panel, BorderLayout.CENTER);
        loginButton.addActionListener(e -> performLogin());
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "账号和密码不能为空！");
            return;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "用户名不存在！", "登录失败", JOptionPane.ERROR_MESSAGE);
        } else if (!user.getPassword().equals(password)) {
            JOptionPane.showMessageDialog(this, "登录密码错误！", "登录失败", JOptionPane.ERROR_MESSAGE);
            passwordField.setText(""); 
        } else {
            // 1. 设置全局上下文
            UserContext.setCurrentUser(user);
            
            // 2. 核心：必须在显示主窗口前，根据当前用户刷新按钮状态
            mainWindow.initPermissionControl();
            
            JOptionPane.showMessageDialog(this, "登录成功！");
            this.dispose(); 
            mainWindow.setVisible(true); 
        }
    }
}
package com.qiao.demo.inventory.view;

import com.qiao.demo.inventory.dao.UserMapper;
import com.qiao.demo.inventory.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * 新增系统账号弹窗
 */
public class AddUserDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton saveButton, cancelButton;
    private UserMapper userMapper;

    public AddUserDialog(JFrame parent, UserMapper userMapper) {
        super(parent, "新增系统账号", true);
        this.userMapper = userMapper;
        setSize(320, 250);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 表单区域
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        formPanel.add(new JLabel("登录账号:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("登录密码:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        formPanel.add(new JLabel("账号角色:"));
        // 下拉框选择角色，防止手动输入出错
        roleComboBox = new JComboBox<>(new String[]{"USER", "ADMIN"});
        formPanel.add(roleComboBox);

        add(formPanel, BorderLayout.CENTER);

        // 按钮区域
        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("保存账号");
        cancelButton = new JButton("取消");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 绑定事件
        saveButton.addActionListener(e -> saveUser());
        cancelButton.addActionListener(e -> dispose());
    }

    private void saveUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "账号和密码不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setRole(role);
            
            // 直接使用 MyBatis-Plus 的 Mapper 插入数据库
            userMapper.insert(newUser);
            
            JOptionPane.showMessageDialog(this, "账号 [" + username + "] 创建成功！");
            dispose(); // 关闭弹窗
        } catch (Exception ex) {
            // 捕获异常，通常是因为违反了 username 的 UNIQUE 唯一约束
            JOptionPane.showMessageDialog(this, "创建失败！账号可能已存在。", "数据库冲突", JOptionPane.ERROR_MESSAGE);
        }
    }
}
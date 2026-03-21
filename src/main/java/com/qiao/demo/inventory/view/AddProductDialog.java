package com.qiao.demo.inventory.view;

import com.qiao.demo.inventory.model.Product;
import com.qiao.demo.inventory.service.ProductService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * 新增商品弹窗
 */
public class AddProductDialog extends JDialog {

    private JTextField codeField, nameField, stockField, priceField;
    private JButton saveButton, cancelButton;
    private ProductService productService;
    private Runnable onSuccess; // 保存成功后的回调（用于刷新主界面表格）

    public AddProductDialog(JFrame parent, ProductService productService, Runnable onSuccess) {
        super(parent, "新增商品", true); // true 表示模态窗口（不关掉不能点主界面）
        this.productService = productService;
        this.onSuccess = onSuccess;

        setSize(300, 250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initForm();
    }

    private void initForm() {
        // 表单区域
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("商品编号:"));
        codeField = new JTextField();
        formPanel.add(codeField);

        formPanel.add(new JLabel("商品名称:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("初始库存:"));
        stockField = new JTextField("0");
        formPanel.add(stockField);

        formPanel.add(new JLabel("商品单价:"));
        priceField = new JTextField("0.00");
        formPanel.add(priceField);

        add(formPanel, BorderLayout.CENTER);

        // 按钮区域
        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("保存");
        cancelButton = new JButton("取消");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 绑定保存事件
        saveButton.addActionListener(e -> saveProduct());
        cancelButton.addActionListener(e -> dispose());
    }

    private void saveProduct() {
        try {
            Product product = new Product();
            product.setProductCode(codeField.getText().trim());
            product.setProductName(nameField.getText().trim());
            product.setStockLevel(Integer.parseInt(stockField.getText().trim()));
            product.setUnitPrice(new BigDecimal(priceField.getText().trim()));

            boolean success = productService.save(product);
            if (success) {
                JOptionPane.showMessageDialog(this, "添加成功！");
                onSuccess.run(); // 触发主界面的刷新
                dispose();       // 关闭弹窗
            } else {
                JOptionPane.showMessageDialog(this, "添加失败，请检查编号是否重复。");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "输入格式有误，请检查库存(整数)和单价(数字)。");
        }
    }
}
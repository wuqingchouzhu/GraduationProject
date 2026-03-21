package com.qiao.demo.inventory.view;

import com.qiao.demo.inventory.dao.UserMapper;
import com.qiao.demo.inventory.model.Product;
import com.qiao.demo.inventory.service.ProductService;
import com.qiao.demo.inventory.config.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

@Component
public class MainWindow extends JFrame {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserMapper userMapper; // 注入 UserMapper 用于开通账号

    private JTable productTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton, addButton, deleteButton, stockInButton, stockOutButton;
    private JButton addUserAccountButton; // 新增：添加账号按钮

    public MainWindow() {
        setTitle("极简进销存管理系统 v1.0");
        setSize(950, 600); // 稍微加宽一点窗口，放得下新按钮
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        refreshButton = new JButton("刷新列表");
        addButton = new JButton("新增商品");
        deleteButton = new JButton("删除商品");
        stockInButton = new JButton("入库操作");
        stockOutButton = new JButton("出库操作");
        addUserAccountButton = new JButton("新增系统账号"); // 初始化新按钮

        // 默认禁用/隐藏敏感操作
        addButton.setEnabled(false);
        deleteButton.setVisible(false);
        addUserAccountButton.setVisible(false); // 默认隐藏新增账号按钮

        stockInButton.setForeground(new Color(0, 128, 0));
        stockOutButton.setForeground(Color.RED);
        addUserAccountButton.setForeground(Color.BLUE); // 设为蓝色区分一下

        toolBar.add(refreshButton);
        toolBar.add(new JSeparator(JSeparator.VERTICAL));
        toolBar.add(addButton);
        toolBar.add(deleteButton);
        toolBar.add(new JSeparator(JSeparator.VERTICAL));
        toolBar.add(stockInButton);
        toolBar.add(stockOutButton);
        toolBar.add(new JSeparator(JSeparator.VERTICAL));
        toolBar.add(addUserAccountButton); // 把按钮加到工具栏
        add(toolBar, BorderLayout.NORTH);

        String[] columnNames = {"ID", "商品编号", "商品名称", "当前库存", "单价(元)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setRowHeight(25);
        add(new JScrollPane(productTable), BorderLayout.CENTER);

        // 事件绑定
        refreshButton.addActionListener(e -> refreshProductTable());
        addButton.addActionListener(e -> {
            AddProductDialog dialog = new AddProductDialog(this, productService, this::refreshProductTable);
            dialog.setVisible(true);
        });
        deleteButton.addActionListener(e -> deleteSelectedProduct());
        stockInButton.addActionListener(e -> handleStockOperation(true));
        stockOutButton.addActionListener(e -> handleStockOperation(false));
        
        // 绑定新增账号按钮事件
        addUserAccountButton.addActionListener(e -> {
            AddUserDialog userDialog = new AddUserDialog(this, userMapper);
            userDialog.setVisible(true);
        });
    }

    public void initPermissionControl() {
        boolean isAdmin = UserContext.isAdmin();
        
        deleteButton.setVisible(isAdmin);
        addButton.setEnabled(isAdmin);
        addUserAccountButton.setVisible(isAdmin); // 只有管理员能看到开账号的按钮

        if (isAdmin) {
            setTitle("极简进销存管理系统 [管理员模式]");
        } else {
            setTitle("极简进销存管理系统 [普通员工 - 仅限出入库]");
        }
        
        refreshProductTable(); 
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void refreshProductTable() {
        List<Product> products = productService.list(); 
        tableModel.setRowCount(0);
        for (Product p : products) {
            Vector<Object> row = new Vector<>();
            row.add(p.getId());
            row.add(p.getProductCode());
            row.add(p.getProductName());
            row.add(p.getStockLevel());
            row.add(p.getUnitPrice());
            tableModel.addRow(row);
        }
    }

    private void deleteSelectedProduct() {
        int row = productTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中要删除的商品！");
            return;
        }
        Integer id = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);
        int confirm = JOptionPane.showConfirmDialog(this, "确定删除 [" + name + "] 吗？", "警告", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            productService.removeById(id);
            refreshProductTable();
        }
    }

    private void handleStockOperation(boolean isIn) {
        int row = productTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一款商品！");
            return;
        }
        Integer id = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);
        Integer currentStock = (Integer) tableModel.getValueAt(row, 3);
        String opName = isIn ? "入库" : "出库";
        String input = JOptionPane.showInputDialog(this, "请输入 [" + name + "] 的" + opName + "数量：\n(当前库存: " + currentStock + ")");
        if (input == null || input.trim().isEmpty()) return;
        try {
            int quantity = Integer.parseInt(input.trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "数量必须大于0！");
                return;
            }
            try {
                if (isIn) productService.stockIn(id, quantity);
                else productService.stockOut(id, quantity);
                JOptionPane.showMessageDialog(this, opName + "成功！");
                refreshProductTable();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "操作拦截", JOptionPane.WARNING_MESSAGE);
                refreshProductTable();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "请输入有效的整数数字！");
        }
    }
}
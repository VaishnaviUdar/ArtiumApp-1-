package com.artium;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;

public class ArtiumApp extends JFrame {

    CardLayout cl;
    JPanel mainPanel, galleryPanel;

    Connection con = DBConnection.getConnection();
    String selectedImagePath = "";

    public ArtiumApp() {
        setTitle("Artium");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cl = new CardLayout();
        mainPanel = new JPanel(cl);

        mainPanel.add(homePage(), "HOME");
        mainPanel.add(addArtworkPage(), "ADD");
        mainPanel.add(galleryPage(), "GALLERY");

        add(mainPanel);
        setVisible(true);
    }

    private JPanel homePage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel topBar = new JPanel(new GridLayout(1, 3));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        topBar.setBackground(Color.WHITE);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(Color.WHITE);
        JButton addBtn = new JButton("+ Add Artwork");
        leftPanel.add(addBtn);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setBackground(Color.WHITE);
        JButton searchIcon = new JButton("🔍");
        centerPanel.add(searchIcon);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(Color.WHITE);
        JButton menuBtn = new JButton("≡");
        rightPanel.add(menuBtn);

        topBar.add(leftPanel);
        topBar.add(centerPanel);
        topBar.add(rightPanel);
        panel.add(topBar, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);

        center.add(Box.createVerticalStrut(40));

        ImageIcon icon = new ImageIcon("src/images/Artium.png");
        Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(img));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(logo);
        center.add(Box.createVerticalStrut(20));

        JLabel title = new JLabel("ARTIUM");
        title.setFont(new Font("Serif", Font.BOLD, 90));
        title.setForeground(new Color(230, 60, 30));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(title);
        center.add(Box.createVerticalStrut(30));

        JButton galleryBtn = new JButton("View Gallery →");
        galleryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(galleryBtn);

        panel.add(center, BorderLayout.CENTER);

        JPopupMenu menu = new JPopupMenu();
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        menuBtn.addActionListener(e -> {
            loadMenuTypes(menuPanel);
            menu.removeAll();
            menu.add(menuPanel);
            menu.show(menuBtn, 0, menuBtn.getHeight());
        });

        searchIcon.addActionListener(e -> {
            String text = JOptionPane.showInputDialog(this, "Search artwork / artist / type:");
            if (text != null && !text.isEmpty()) {
                refreshGallery(text);
                cl.show(mainPanel, "GALLERY");
            }
        });

        addBtn.addActionListener(e -> cl.show(mainPanel, "ADD"));

        galleryBtn.addActionListener(e -> {
            refreshGallery("");
            cl.show(mainPanel, "GALLERY");
        });

        return panel;
    }

    private JPanel addArtworkPage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Add Artwork");
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel nameLbl = new JLabel("Artwork Name:");
        JLabel typeLbl = new JLabel("Type:");
        JLabel artistLbl = new JLabel("Artist:");
        JLabel priceLbl = new JLabel("Price:");
        JLabel imgLbl = new JLabel("Image:");

        JTextField name = new JTextField(20);
        JTextField type = new JTextField(20);
        JTextField artist = new JTextField(20);
        JTextField price = new JTextField(20);

        JButton upload = new JButton("Upload");
        JLabel imageName = new JLabel("No file selected");

        JButton submit = new JButton("Submit");
        JButton back = new JButton("← Back");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(nameLbl, gbc);
        gbc.gridx = 1;
        panel.add(name, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(typeLbl, gbc);
        gbc.gridx = 1;
        panel.add(type, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(artistLbl, gbc);
        gbc.gridx = 1;
        panel.add(artist, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(priceLbl, gbc);
        gbc.gridx = 1;
        panel.add(price, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(imgLbl, gbc);
        gbc.gridx = 1;
        panel.add(upload, gbc);

        gbc.gridy = 6;
        panel.add(imageName, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        panel.add(submit, gbc);

        gbc.gridy = 8;
        panel.add(back, gbc);

        upload.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                selectedImagePath = file.getAbsolutePath();
                imageName.setText(file.getName());
            }
        });

        submit.addActionListener(e -> {
            try {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO artworks(name,type,artist,price,image_path) VALUES(?,?,?,?,?)"
                );

                ps.setString(1, name.getText());
                ps.setString(2, type.getText());
                ps.setString(3, artist.getText());
                ps.setDouble(4, Double.parseDouble(price.getText()));
                ps.setString(5, selectedImagePath);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Saved Successfully!");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        back.addActionListener(e -> cl.show(mainPanel, "HOME"));

        return panel;
    }

    private JPanel galleryPage() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
        top.setBackground(Color.WHITE);

        JButton backBtn = new JButton("← Back");
        top.add(backBtn);
        top.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        main.add(top, BorderLayout.NORTH);

        galleryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
        galleryPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(galleryPanel);
        scroll.setBorder(null);

        main.add(scroll, BorderLayout.CENTER);

        refreshGallery("");

        backBtn.addActionListener(e -> cl.show(mainPanel, "HOME"));

        return main;
    }

    private void refreshGallery(String keyword) {
        galleryPanel.removeAll();

        try {
            PreparedStatement ps;

            if (keyword == null || keyword.trim().isEmpty()) {
                ps = con.prepareStatement("SELECT * FROM artworks");
            } else {
                String sql = "SELECT * FROM artworks WHERE LOWER(name) LIKE ? OR LOWER(type) LIKE ? OR LOWER(artist) LIKE ?";
                ps = con.prepareStatement(sql);
                String k = "%" + keyword.toLowerCase() + "%";
                ps.setString(1, k);
                ps.setString(2, k);
                ps.setString(3, k);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                String artist = rs.getString("artist");
                String price = rs.getString("price");
                String path = rs.getString("image_path");

                ImageIcon icon;
                File imgFile = new File(path);

                if (imgFile.exists()) {
                    icon = new ImageIcon(path);
                } else {
                    icon = new ImageIcon();
                }

                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                JLabel imgLabel = new JLabel(new ImageIcon(img));

                JPanel card = new JPanel();
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBackground(Color.WHITE);
                card.setPreferredSize(new Dimension(180, 350));

                JLabel nameLbl = new JLabel(name);
                JLabel typeLbl = new JLabel(type);
                JLabel artistLbl = new JLabel(artist);
                JLabel priceLbl = new JLabel("₹" + price);

                JButton updateBtn = new JButton("Update");
                JButton deleteBtn = new JButton("Delete");

                card.add(imgLabel);
                card.add(nameLbl);
                card.add(typeLbl);
                card.add(artistLbl);
                card.add(priceLbl);
                card.add(updateBtn);
                card.add(deleteBtn);

                galleryPanel.add(card);
            }

            galleryPanel.revalidate();
            galleryPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMenuTypes(JPanel menuPanel) {
        menuPanel.removeAll();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT DISTINCT type FROM artworks");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type");

                JButton btn = new JButton(type);

                btn.addActionListener(e -> {
                    refreshGallery(type);
                    cl.show(mainPanel, "GALLERY");
                });

                menuPanel.add(btn);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ArtiumApp();
    }
}





























































































































































































































































































































































































































































































































































































































































































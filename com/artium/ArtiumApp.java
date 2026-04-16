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

    // ================= HOME =================
    private JPanel homePage() {
        JPanel panel = new JPanel(null);

        // LEFT: ADD ARTWORK
        JButton addBtn = new JButton("+ Add Artwork");
        addBtn.setBounds(30, 20, 180, 40);
        panel.add(addBtn);

        // CENTER: SEARCH ICON
        JButton searchIcon = new JButton("🔍");
        searchIcon.setBounds(460, 20, 60, 40);
        panel.add(searchIcon);

        // RIGHT: MENU
        JButton menuBtn = new JButton("≡");
        menuBtn.setBounds(900, 20, 50, 40);
        panel.add(menuBtn);

        // SEARCH FIELD (hidden initially)
        JTextField searchField = new JTextField();
        searchField.setBounds(350, 100, 300, 40);
        searchField.setVisible(false);
        panel.add(searchField);

        // TITLE
        JLabel title = new JLabel("ARTIUM");
        title.setBounds(200, 180, 700, 100);
        title.setFont(new Font("Serif", Font.BOLD, 100));
        title.setForeground(new Color(230, 60, 30));
        panel.add(title);

        // VIEW GALLERY
        JButton galleryBtn = new JButton("View Gallery →");
        galleryBtn.setBounds(350, 350, 300, 60);
        panel.add(galleryBtn);

        // MENU
        JPopupMenu menu = new JPopupMenu();
        menu.add("Paintings");
        menu.add("Sketches");
        menu.add("Digital Art");

        menuBtn.addActionListener(e -> menu.show(menuBtn, 0, menuBtn.getHeight()));

        // SHOW SEARCH BAR
        searchIcon.addActionListener(e -> {
            searchField.setVisible(true);
        });

        // SEARCH ACTION
        searchField.addActionListener(e -> {
            refreshGallery(searchField.getText());
            cl.show(mainPanel, "GALLERY");
        });

        // NAVIGATION
        addBtn.addActionListener(e -> cl.show(mainPanel, "ADD"));

        galleryBtn.addActionListener(e -> {
            refreshGallery("");
            cl.show(mainPanel, "GALLERY");
        });

        return panel;
    }

    // ================= ADD ARTWORK =================
    private JPanel addArtworkPage() {

        JPanel panel = new JPanel(null);

        JLabel title = new JLabel("Add Artwork");
        title.setBounds(400, 20, 200, 30);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title);

        // ===== LABELS =====
        JLabel nameLbl = new JLabel("Artwork Name:");
        nameLbl.setBounds(250, 80, 150, 25);
        panel.add(nameLbl);

        JLabel typeLbl = new JLabel("Type:");
        typeLbl.setBounds(250, 130, 150, 25);
        panel.add(typeLbl);

        JLabel artistLbl = new JLabel("Artist:");
        artistLbl.setBounds(250, 180, 150, 25);
        panel.add(artistLbl);

        JLabel priceLbl = new JLabel("Price:");
        priceLbl.setBounds(250, 230, 150, 25);
        panel.add(priceLbl);

        JLabel imgLbl = new JLabel("Image:");
        imgLbl.setBounds(250, 280, 150, 25);
        panel.add(imgLbl);

        // ===== FIELDS =====
        JTextField name = new JTextField();
        name.setBounds(400, 80, 250, 30);
        panel.add(name);

        JTextField type = new JTextField();
        type.setBounds(400, 130, 250, 30);
        panel.add(type);

        JTextField artist = new JTextField();
        artist.setBounds(400, 180, 250, 30);
        panel.add(artist);

        JTextField price = new JTextField();
        price.setBounds(400, 230, 250, 30);
        panel.add(price);

        // ===== IMAGE UPLOAD =====
        JButton upload = new JButton("Upload");
        upload.setBounds(400, 280, 120, 30);
        panel.add(upload);

        JLabel imageName = new JLabel("No file selected");
        imageName.setBounds(530, 280, 200, 30);
        panel.add(imageName);

        // ===== BUTTONS =====
        JButton submit = new JButton("Submit");
        submit.setBounds(400, 350, 120, 40);
        panel.add(submit);

        JButton back = new JButton("← Back");
        back.setBounds(30, 20, 100, 30);
        panel.add(back);

        // ===== IMAGE UPLOAD LOGIC =====
        upload.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);

            if (res == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                selectedImagePath = file.getAbsolutePath();
                imageName.setText(file.getName());
            }
        });

        // ===== SAVE TO DATABASE =====
        submit.addActionListener(e -> {
            try {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO artworks(name,type,artist,price,image_path) VALUES(?,?,?,?,?)");

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
    
    // ================= GALLERY =================
    private JPanel galleryPage() {
        JPanel panel = new JPanel(new BorderLayout());

        galleryPanel = new JPanel(new FlowLayout());

        JScrollPane scroll = new JScrollPane(galleryPanel);

        JButton back = new JButton("← Back");

        panel.add(back, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        back.addActionListener(e -> cl.show(mainPanel, "HOME"));

        return panel;
    }

    // ================= LOAD / SEARCH =================
    private void refreshGallery(String keyword) {

        galleryPanel.removeAll();

        try {
            PreparedStatement ps;

            if (keyword == null || keyword.trim().isEmpty()) {
                ps = con.prepareStatement("SELECT * FROM artworks");
            } else {
                ps = con.prepareStatement(
                        "SELECT * FROM artworks WHERE name LIKE ? OR type LIKE ? OR artist LIKE ?");
                String k = "%" + keyword + "%";
                ps.setString(1, k);
                ps.setString(2, k);
                ps.setString(3, k);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String name = rs.getString("name");
                String type = rs.getString("type");
                String artist = rs.getString("artist");
                String price = rs.getString("price");
                String path = rs.getString("image_path");

                ImageIcon icon = new ImageIcon(path);
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);

                JLabel imgLabel = new JLabel(new ImageIcon(img));

                JLabel info = new JLabel(
                        "<html>" + name + "<br>" + type + "<br>" + artist + "<br>₹" + price + "</html>");

                JPanel item = new JPanel(new BorderLayout());
                item.add(imgLabel, BorderLayout.CENTER);
                item.add(info, BorderLayout.SOUTH);

                // CLICK → ENLARGE IMAGE
                imgLabel.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {

                        JFrame popup = new JFrame("Artwork");
                        popup.setSize(500, 500);

                        JLabel big = new JLabel(new ImageIcon(path));
                        popup.add(new JScrollPane(big));

                        popup.setVisible(true);
                    }
                });

                galleryPanel.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        galleryPanel.revalidate();
        galleryPanel.repaint();
    }

    public static void main(String[] args) {
        new ArtiumApp();
    }
}
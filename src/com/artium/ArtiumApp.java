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

        // ================= TOP BAR =================
        JPanel topBar = new JPanel(new GridLayout(1, 3));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        topBar.setBackground(Color.WHITE);

        // LEFT
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(Color.WHITE);
        JButton addBtn = new JButton("+ Add Artwork");
        leftPanel.add(addBtn);

        // CENTER
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setBackground(Color.WHITE);
        JButton searchIcon = new JButton("🔍");
        centerPanel.add(searchIcon);

        // RIGHT
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(Color.WHITE);
        JButton menuBtn = new JButton("≡");
        rightPanel.add(menuBtn);

        topBar.add(leftPanel);
        topBar.add(centerPanel);
        topBar.add(rightPanel);

        panel.add(topBar, BorderLayout.NORTH);

        // ================= CENTER CONTENT =================
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);

        center.add(Box.createVerticalStrut(40));

        // ===== LOGO =====
        ImageIcon icon = new ImageIcon("src/images/Artium.png");
        Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(img));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(logo);

        center.add(Box.createVerticalStrut(20));

        // ===== TITLE =====
        JLabel title = new JLabel("ARTIUM");
        title.setFont(new Font("Serif", Font.BOLD, 90));
        title.setForeground(new Color(230, 60, 30));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(title);

        center.add(Box.createVerticalStrut(30));

        // ===== VIEW GALLERY BUTTON =====
        JButton galleryBtn = new JButton("View Gallery →");
        galleryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(galleryBtn);

        panel.add(center, BorderLayout.CENTER);

        // ================= MENU =================
        JPopupMenu menu = new JPopupMenu();
        menu.add("Paintings");
        menu.add("Sketches");
        menu.add("Digital Art");

        menuBtn.addActionListener(e -> menu.show(menuBtn, 0, menuBtn.getHeight()));

        // ================= SEARCH =================
        searchIcon.addActionListener(e -> {
            String text = JOptionPane.showInputDialog(this, "Search artwork / artist / type:");

            if (text != null && !text.isEmpty()) {
                refreshGallery(text);
                cl.show(mainPanel, "GALLERY");
            }
        });

        // ================= NAVIGATION =================
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

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        // ===== TOP =====
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
        top.setBackground(Color.WHITE);

        JButton backBtn = new JButton("← Back");
        top.add(backBtn);

        top.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        main.add(top, BorderLayout.NORTH);

        // ===== GALLERY =====
        galleryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
        galleryPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(galleryPanel);
        scroll.setBorder(null);

        main.add(scroll, BorderLayout.CENTER);

        // Load data from DB
        refreshGallery("");

        // Back button
        backBtn.addActionListener(e -> cl.show(mainPanel, "HOME"));

        return main;
    }
    
    

    // ================= LOAD / SEARCH =================
    private void refreshGallery(String keyword) {

        galleryPanel.removeAll();

        try {

            PreparedStatement ps;

            if (keyword == null || keyword.trim().isEmpty()) {

                ps = con.prepareStatement("SELECT * FROM artworks");

            } else {

                String sql = "SELECT * FROM artworks WHERE name LIKE ? OR type LIKE ? OR artist LIKE ?";
                ps = con.prepareStatement(sql);

                String k = "%" + keyword + "%";
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

                ImageIcon icon = new ImageIcon(path);
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);

                JLabel imgLabel = new JLabel(new ImageIcon(img));
                
             // Make cursor like clickable hand
                imgLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

                // Click event
                imgLabel.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {

                        // Create dialog (popup inside same app)
                        JDialog dialog = new JDialog();
                        dialog.setTitle("Artwork View");
                        dialog.setSize(500, 500);
                        dialog.setLocationRelativeTo(null);
                        dialog.setModal(true); // optional but good

                        // Load big image
                        ImageIcon bigIcon = new ImageIcon(path);
                        Image bigImg = bigIcon.getImage().getScaledInstance(450, 450, Image.SCALE_SMOOTH);

                        JLabel bigImageLabel = new JLabel(new ImageIcon(bigImg));
                        bigImageLabel.setHorizontalAlignment(JLabel.CENTER);

                        dialog.add(bigImageLabel);
                        dialog.setVisible(true);
                    }
                });

                JPanel card = new JPanel();
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBackground(Color.WHITE);
                card.setPreferredSize(new Dimension(180, 350));

                JLabel nameLbl = new JLabel(name);
                JLabel typeLbl = new JLabel(type);
                JLabel artistLbl = new JLabel(artist);
                JLabel priceLbl = new JLabel("₹" + price);
                
                JButton deleteBtn = new JButton("Delete");
                deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                deleteBtn.addActionListener(e -> {

                    int confirm = JOptionPane.showConfirmDialog(
                            null,
                            "Are you sure you want to delete this artwork?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            PreparedStatement psDel = con.prepareStatement(
                                    "DELETE FROM artworks WHERE id=?"
                            );

                            psDel.setInt(1, id);   // using id from DB
                            psDel.executeUpdate();

                            JOptionPane.showMessageDialog(null, "Artwork Deleted!");

                            refreshGallery("");   // reload gallery

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                // Center all text
                nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                typeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                artistLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                priceLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                // Add spacing + styling
                nameLbl.setFont(new Font("Arial", Font.BOLD, 14));
                typeLbl.setFont(new Font("Arial", Font.PLAIN, 12));
                artistLbl.setFont(new Font("Arial", Font.PLAIN, 12));
                priceLbl.setFont(new Font("Arial", Font.BOLD, 13));

                card.add(Box.createVerticalStrut(5));
                card.add(imgLabel);
                card.add(Box.createVerticalStrut(8));
                card.add(nameLbl);
                card.add(Box.createVerticalStrut(4));
                card.add(typeLbl);
                card.add(artistLbl);
                card.add(Box.createVerticalStrut(4));
                card.add(priceLbl);
                
                card.add(Box.createVerticalStrut(6));
                card.add(deleteBtn);

                

                galleryPanel.add(card);
            }

            galleryPanel.revalidate();
            galleryPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    
    
    //testing branch
     // experiment change
    
        galleryPanel.revalidate();
        galleryPanel.repaint();
    }

    public static void main(String[] args) {
        new ArtiumApp();
    }
}
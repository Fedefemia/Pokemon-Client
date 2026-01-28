import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;

public class PokemonDarkDB extends JFrame {

    // Componenti UI
    private JTextField searchField;
    private JLabel imageLabel, nameLabel, infoLabel, statsLabel;
    private JButton prevBtn, nextBtn, searchBtn;
    private int currentId = 1;

    // Costanti Limiti
    private final int MIN_ID = 1;
    private final int MAX_ID = 1025; // Ultimo Pokémon attuale

    // Colori Dark Mode
    private final Color DARK_BG = new Color(30, 30, 30);
    private final Color DARK_ACCENT = new Color(50, 50, 50);
    private final Color TEXT_COLOR = new Color(230, 230, 230);
    private final Color DISABLED_COLOR = new Color(70, 70, 70);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PokemonDarkDB().setVisible(true));
    }

    public PokemonDarkDB() {
        // Setup Finestra
        setTitle("PokéDex");
        setSize(400, 720); // Leggermente più alta per le stats
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(DARK_BG);

        // Header
        JPanel top = new JPanel();
        top.setBackground(DARK_BG);
        
        JLabel lblSearch = new JLabel("Nome/ID:");
        lblSearch.setForeground(TEXT_COLOR);
        
        searchField = new JTextField(12);
        searchField.setBackground(DARK_ACCENT);
        searchField.setForeground(TEXT_COLOR);
        searchField.setCaretColor(Color.WHITE);
        
        searchBtn = createDarkButton("Cerca");
        
        top.add(lblSearch);
        top.add(searchField);
        top.add(searchBtn);
        add(top, BorderLayout.NORTH);

        // Centrale
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(DARK_BG);
        
        imageLabel = new JLabel("...", SwingConstants.CENTER);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setPreferredSize(new Dimension(180, 180));
        imageLabel.setForeground(TEXT_COLOR);

        nameLabel = new JLabel("---");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setForeground(TEXT_COLOR);

        infoLabel = new JLabel("---");
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoLabel.setForeground(Color.GRAY);
        
        statsLabel = new JLabel("---");
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Forza la centratura orizzontale del contenuto della label
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statsLabel.setForeground(TEXT_COLOR);
        // Font monospaziato leggermente più grande per allineamento migliore
        statsLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));

        center.add(Box.createVerticalStrut(20));
        center.add(imageLabel);
        center.add(Box.createVerticalStrut(15));
        center.add(nameLabel);
        center.add(infoLabel);
        center.add(Box.createVerticalStrut(15));
        center.add(statsLabel);
        center.add(Box.createVerticalStrut(20));
        add(center, BorderLayout.CENTER);

        // Footer
        JPanel bottom = new JPanel();
        bottom.setBackground(DARK_BG);
        prevBtn = createDarkButton("< Prev");
        nextBtn = createDarkButton("Next >");
        bottom.add(prevBtn);
        bottom.add(nextBtn);
        add(bottom, BorderLayout.SOUTH);

        // Eventi Navigazione con Limiti
        searchBtn.addActionListener(e -> startLoad(searchField.getText().trim()));
        
        prevBtn.addActionListener(e -> {
            if (currentId > MIN_ID) {
                startLoad(String.valueOf(--currentId));
            }
        });
        
        nextBtn.addActionListener(e -> {
            if (currentId < MAX_ID) {
                startLoad(String.valueOf(++currentId));
            }
        });

        // Avvio
        startLoad("1"); 
    }

    // Helper per creare bottoni scuri che gestiscono lo stato disabilitato
    private JButton createDarkButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            public void setEnabled(boolean b) {
                super.setEnabled(b);
                setBackground(b ? DARK_ACCENT : DISABLED_COLOR);
                setForeground(b ? TEXT_COLOR : Color.GRAY);
            }
        };
        btn.setBackground(DARK_ACCENT);
        btn.setForeground(TEXT_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        return btn;
    }

    private void startLoad(String query) {
        new Thread(() -> {
            try {
                // 1. Download
                String urlStr = "https://pokeapi.co/api/v2/pokemon/" + query.toLowerCase();
                String json = download(urlStr);

                // 2. Parsing Dati Base
                int formsIdx = json.indexOf("\"forms\":");
                String safeJson = (formsIdx != -1) ? json.substring(formsIdx) : json;
                String name = parseVal(safeJson, "name");
                
                int id = Integer.parseInt(parseVal(json, "id"));
                int w = Integer.parseInt(parseVal(json, "weight"));
                int h = Integer.parseInt(parseVal(json, "height"));

                // 3. Parsing Statistiche
                int hp = parseStat(json, "hp");
                int atk = parseStat(json, "attack");
                int def = parseStat(json, "defense");
                int spAtk = parseStat(json, "special-attack");
                int spDef = parseStat(json, "special-defense");
                // Aggiunta velocità per completezza, anche se non colorata
                int speed = parseStat(json, "speed"); 

                // 4. Logica Colori
                String hexAtk, hexSpAtk;
                int diff = Math.abs(atk - spAtk);
                
                String GREEN = "#00FF00";
                String RED = "#FF4444";
                String L_GREEN = "#90EE90";
                String L_YELLOW = "#FFFFE0";

                if (diff < 10) {
                    hexAtk = L_GREEN; hexSpAtk = L_GREEN;
                } else if (diff <= 30) {
                    if (atk > spAtk) { hexAtk = L_GREEN; hexSpAtk = L_YELLOW; }
                    else { hexAtk = L_YELLOW; hexSpAtk = L_GREEN; }
                } else {
                    if (atk > spAtk) { hexAtk = GREEN; hexSpAtk = RED; }
                    else { hexAtk = RED; hexSpAtk = GREEN; }
                }

                // HTML Centrato
                String statsHtml = "<html><div style='text-align: center;'>"
                        + "HP: " + hp + "<br>"
                        + "<font color='" + hexAtk + "'>ATK: " + atk + "</font><br>"
                        + "DEF: " + def + "<br>"
                        + "<font color='" + hexSpAtk + "'>SP. ATK: " + spAtk + "</font><br>"
                        + "SP. DEF: " + spDef + "<br>"
                        + "SPEED: " + speed
                        + "</div></html>";

                // 5. Sprite
                String spriteBlock = extractBlock(json, "sprites");
                String imgUrl = parseVal(spriteBlock, "front_default");
                
                Image img = null;
                try {
                    if (!imgUrl.equals("0") && !imgUrl.contains("null")) {
                        img = ImageIO.read(new URL(imgUrl)).getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                    }
                } catch (Exception e) {}

                // 6. Aggiorna UI
                final Image fImg = img;
                currentId = id;
                
                SwingUtilities.invokeLater(() -> {
                    nameLabel.setText(name.toUpperCase() + " #" + id);
                    infoLabel.setText("H: " + h + " | W: " + w);
                    statsLabel.setText(statsHtml);
                    
                    if (fImg != null) imageLabel.setIcon(new ImageIcon(fImg));
                    else imageLabel.setText("No Image");
                    imageLabel.setText("");

                    // Aggiorna stato bottoni (Disabilita se ai limiti)
                    prevBtn.setEnabled(currentId > MIN_ID);
                    nextBtn.setEnabled(currentId < MAX_ID);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, "Errore/Non trovato: " + query));
            }
        }).start();
    }

    // Metodi di parsing e rete invariati rispetto alla versione funzionante precedente
    private String download(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (con.getResponseCode() != 200) throw new RuntimeException("HTTP " + con.getResponseCode());
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) sb.append(line);
        in.close();
        return sb.toString();
    }

    private String parseVal(String json, String key) {
        String token = "\"" + key + "\":";
        int start = json.indexOf(token);
        if (start == -1) return "0";
        start += token.length();
        if (json.charAt(start) == '"') {
            start++; int end = json.indexOf("\"", start); return json.substring(start, end);
        } else {
            int end = json.indexOf(",", start); if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
    }

    private int parseStat(String json, String statName) {
        String key = "\"name\":\"" + statName + "\"";
        int loc = json.indexOf(key);
        if (loc == -1) return 0;
        String sub = json.substring(0, loc); 
        String statKey = "\"base_stat\":";
        int statLoc = sub.lastIndexOf(statKey);
        if (statLoc == -1) return 0;
        int start = statLoc + statKey.length();
        int end = sub.indexOf(",", start);
        return Integer.parseInt(sub.substring(start, end).trim());
    }

    private String extractBlock(String json, String key) {
        String token = "\"" + key + "\":{";
        int start = json.indexOf(token);
        if (start == -1) return "";
        start += token.length() - 1; 
        int brackets = 0; int end = start;
        do {
            char c = json.charAt(end); if (c == '{') brackets++; else if (c == '}') brackets--; end++;
        } while (brackets > 0 && end < json.length());
        return json.substring(start, end);
    }
}
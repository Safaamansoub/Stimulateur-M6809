
package dev.zinebmorad;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;


public class Interface extends JFrame {
    // backend
    private final RAM ram ;
    private final CPU cpu ;
    // UI Components for Registers (Global so we can update them)
    private JTextField txtPC, txtA, txtB, txtX, txtY, txtU, txtS, txtDP, txtCC;
    private JLabel statusLabel;
    private JTextField txtBreakpoint;
    private JButton btnRun;
    // NEW: Storage for previous register values (initialized to -1 so they highlight on first run)
    private int oldPC = -1;
    private int oldA = -1;
    private int oldB = -1;
    private int oldX = -1;
    private int oldY = -1;
    private int oldU = -1;
    private int oldS = -1;
    private int oldDP = -1;
    private int oldCC = -1;
    private javax.swing.Timer runTimer; // Timer for the RUN button
    // constructor
    public Interface(){
        // intialisation de backend
        this.ram = new RAM();
        this.cpu = new CPU(ram);
        // setup de la window
        setTitle("Motorola 6809 Simulator - Zineb & Safaa");
        setSize(1200, 800); // Big enough to see everything
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout(10, 10)); // Main layout with gaps
        //test to see if evrything working together
        // Add the Control Panel to the Top
        add(createControlPanel(), BorderLayout.NORTH);
        // Initial update to show starting 0000 values
        updateGUI();

        add(createCodePanel(), BorderLayout.CENTER);

        // Initial update
        updateGUI();
        // Add Memory Panel (Right) -- ADD THIS LINE
        add(createMemoryPanel(), BorderLayout.EAST);
        // Status Bar at the bottom
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        statusLabel = new JLabel("System Ready. Waiting for command...");
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
        // Initial update
        updateGUI();

    }

    // ---------------------------------------------------------
    // RIGHT PANEL: Memory Inspector
    // ---------------------------------------------------------
    private JPanel createMemoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Memory Inspector"));
        panel.setPreferredSize(new Dimension(300, 0)); // Set a fixed width

        // 1. The Output Area (Read Only)
        JTextArea memArea = new JTextArea();
        memArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        memArea.setEditable(false); // User cannot type here, only view
        memArea.setBackground(new Color(240, 240, 240)); // Light gray
        JScrollPane scrollPane = new JScrollPane(memArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 2. The Control Bar
        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtInspectAddr = new JTextField("0000", 4);
        JButton btnInspect = new JButton("Inspect");

        controlBar.add(new JLabel("Addr:"));
        controlBar.add(txtInspectAddr);
        controlBar.add(btnInspect);

        panel.add(controlBar, BorderLayout.NORTH);


        // --- ACTION: Read RAM and Show Hex Dump ---
        btnInspect.addActionListener(e -> {
            try {
                // 1. Get Address
                int startAddr = Integer.parseInt(txtInspectAddr.getText().trim(), 16);

                StringBuilder sb = new StringBuilder();
                sb.append("Addr  : 00 01 02 03 04 05 06 07\n");
                sb.append("-------------------------------\n");

                // 2. Loop to show 8 rows of 8 bytes (64 bytes total)
                for (int row = 0; row < 16; row++) {
                    int currentRowAddr = startAddr + (row * 8);

                    // Print Address Header (e.g., C000:)
                    sb.append(String.format("%04X  : ", currentRowAddr & 0xFFFF));
                    StringBuilder asciiLine = new StringBuilder();
                    // Print 8 Bytes
                    for (int col = 0; col < 8; col++) {
                        int val = ram.ReadFromMemory((currentRowAddr + col) & 0xFFFF);
                        sb.append(String.format("%02X ", val));
                        if (val >= 32 && val <= 126) {
                            asciiLine.append((char)val);
                        } else {
                            asciiLine.append(".");
                        }
                    }
                    sb.append(" |  ").append(asciiLine);
                    sb.append("\n");
                }

                memArea.setText(sb.toString());

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Address");
            }
        });

        return panel;
    }


    // ---------------------------------------------------------
    // CENTER PANEL: Code Entry & Loader
    // ---------------------------------------------------------
    private JPanel createCodePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Memory Loader (Code/Data)"));

        // 1. The Text Area (Where you type 86 55 A6 ...)
        JTextArea codeArea = new JTextArea();
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // Use fixed-width font
        codeArea.setToolTipText("Enter Hex Bytes separated by spaces (e.g. '86 55 4F')");
        JScrollPane scrollPane = new JScrollPane(codeArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 2. The Load Bar (Address input + Button)
        JPanel loadBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JTextField txtLoadAddr = new JTextField("0000", 6);
        JButton btnLoad = new JButton("Load to RAM");

        JButton btnHelp = new JButton("Show Opcodes 📋");
        btnHelp.setBackground(new Color(255, 250, 205)); // Light yellow
        btnHelp.addActionListener(e -> showOpcodeReference());


        loadBar.add(new JLabel("Start Address (Hex):"));
        loadBar.add(txtLoadAddr);
        loadBar.add(btnLoad);
        loadBar.add(btnHelp);

        panel.add(loadBar, BorderLayout.SOUTH);

        // --- ACTION: Parse Text and Write to RAM ---
        btnLoad.addActionListener(e -> {
            try {
                // 1. Get Start Address
                String addrStr = txtLoadAddr.getText().trim();
                int startAddr = Integer.parseInt(addrStr, 16);

                // 2. Get Code Text
                String rawCode = codeArea.getText();
                if (rawCode.isEmpty()) return;

                // 3. Split by whitespace (space, enter, tab)
                String[] tokens = rawCode.split("\\s+");

                int currentAddr = startAddr;
                int count = 0;

                // 4. Loop through every byte and write to RAM
                for (String token : tokens) {
                    if (token.isEmpty()) continue;
                    // Parse Hex Byte
                    int value = Integer.parseInt(token, 16);

                    // Safety check: Byte must be 00-FF
                    if (value < 0 || value > 255) {
                        throw new NumberFormatException("Value out of byte range: " + token);
                    }

                    // Write to Backend
                    ram.WriteInMemory(currentAddr, value);

                    currentAddr++;
                    count++;
                }

                JOptionPane.showMessageDialog(this,
                        "Successfully loaded " + count + " bytes at $" + addrStr.toUpperCase());

                // Optional: Update PC to match start address automatically?
                // Let's just leave it manual for now.

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error: Invalid Hex. " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error writing to RAM: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }



    private JPanel createControlPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("CPU Registers & Control"));

        // --- 1. Register Grid ---
        JPanel regPanel = new JPanel(new GridLayout(2, 9, 10, 5));

        // Initialize TextFields
        txtPC = new JTextField("0000"); txtA = new JTextField("00"); txtB = new JTextField("00");
        txtX = new JTextField("0000"); txtY = new JTextField("0000"); txtU = new JTextField("0000");
        txtS = new JTextField("0000"); txtDP = new JTextField("00"); txtCC = new JTextField("00");

        JTextField[] fields = {txtPC, txtA, txtB, txtX, txtY, txtU, txtS, txtDP, txtCC};
        for (JTextField f : fields) {
            f.setHorizontalAlignment(JTextField.CENTER);
            f.setFont(new Font("Monospaced", Font.BOLD, 14));
        }

        // Row 1: Labels
        String[] labels = {"PC", "A", "B", "X", "Y", "U", "S", "DP", "CC"};
        for(String lbl : labels) regPanel.add(new JLabel(lbl, SwingConstants.CENTER));

        // Row 2: Fields
        for (JTextField f : fields) regPanel.add(f);

        mainPanel.add(regPanel, BorderLayout.CENTER);

        // --- 2. Button Toolbar ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnStep = new JButton("STEP");

        // ============================================================
        // FIX IS HERE: We use the class variable (no 'JButton' type)
        // ============================================================
        btnRun = new JButton("RUN");

        JButton btnSet = new JButton("SET");
        JButton btnReset = new JButton("RESET");

        // === NEW: BREAKPOINT INPUT ===
        JPanel breakPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        breakPanel.setBorder(BorderFactory.createTitledBorder("Stop At"));
        txtBreakpoint = new JTextField(4);
        txtBreakpoint.setToolTipText("Enter Address (e.g. C010) to stop RUN");
        txtBreakpoint.setText("");
        breakPanel.add(new JLabel("Addr: $"));
        breakPanel.add(txtBreakpoint);

        // Styling
        btnStep.setBackground(new Color(144, 238, 144));
        btnReset.setBackground(new Color(255, 182, 193));
        btnSet.setBackground(new Color(173, 216, 230));

        // --- ACTIONS ---

        // 1. SET BUTTON
        btnSet.addActionListener(e -> {
            try {
                cpu.setPC(Integer.parseInt(txtPC.getText(), 16));
                cpu.setA(Integer.parseInt(txtA.getText(), 16)); // Fixed: use setter
                cpu.setB(Integer.parseInt(txtB.getText(), 16)); // Fixed: use setter
                cpu.setX(Integer.parseInt(txtX.getText(), 16));
                cpu.setY(Integer.parseInt(txtY.getText(), 16));
                cpu.setU(Integer.parseInt(txtU.getText(), 16));
                cpu.setS(Integer.parseInt(txtS.getText(), 16));
                cpu.setDP(Integer.parseInt(txtDP.getText(), 16));
                cpu.setCC(Integer.parseInt(txtCC.getText(), 16));

                updateGUI();
                JOptionPane.showMessageDialog(this, "Registers Updated Successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Hex Value in one of the fields.");
            }
        });

        // 2. STEP BUTTON
        btnStep.addActionListener(e -> performStep());

        // 3. RUN BUTTON
        runTimer = new javax.swing.Timer(100, ae -> performStep());

        btnRun.addActionListener(e -> {
            if (runTimer.isRunning()) {
                runTimer.stop();
                btnRun.setText("RUN");
                btnRun.setBackground(null);
            } else {
                runTimer.start();
                btnRun.setText("PAUSE");
                btnRun.setBackground(Color.YELLOW);
            }
        });

        // 4. RESET BUTTON
        btnReset.addActionListener(e -> {
            if(runTimer.isRunning()) runTimer.stop();
            btnRun.setText("RUN");
            btnRun.setBackground(null); // Ensure color resets on reset
            cpu.reset();
            updateGUI();
        });

        buttonPanel.add(btnStep);
        buttonPanel.add(btnRun);
        buttonPanel.add(btnSet);
        buttonPanel.add(btnReset);
        buttonPanel.add(breakPanel);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        return mainPanel;
    }
    // Helper to sync UI with CPU variables
// Helper to sync UI with CPU variables AND highlight changes
    private void updateGUI() {
        // 1. Get Current Values from CPU
        int newPC = cpu.getPC();
        int newA = cpu.getA();
        int newB = cpu.getB();
        int newX = cpu.getX();
        int newY = cpu.getY();
        int newU = cpu.getU();
        int newS = cpu.getS();
        int newDP = cpu.getDP();
        int newCC = cpu.getCC();

        // 2. Define Colors
        Color normalColor = Color.WHITE;
        Color changedColor = new Color(255, 255, 153); // Light Yellow for changes

        // 3. Update Text and Background for EACH register

        // PC
        txtPC.setText(String.format("%04X", newPC));
        txtPC.setBackground(newPC != oldPC ? changedColor : normalColor);

        // A
        txtA.setText(String.format("%02X", newA));
        txtA.setBackground(newA != oldA ? changedColor : normalColor);

        // B
        txtB.setText(String.format("%02X", newB));
        txtB.setBackground(newB != oldB ? changedColor : normalColor);

        // X
        txtX.setText(String.format("%04X", newX));
        txtX.setBackground(newX != oldX ? changedColor : normalColor);

        // Y
        txtY.setText(String.format("%04X", newY));
        txtY.setBackground(newY != oldY ? changedColor : normalColor);

        // U
        txtU.setText(String.format("%04X", newU));
        txtU.setBackground(newU != oldU ? changedColor : normalColor);

        // S
        txtS.setText(String.format("%04X", newS));
        txtS.setBackground(newS != oldS ? changedColor : normalColor);

        // DP
        txtDP.setText(String.format("%02X", newDP));
        txtDP.setBackground(newDP != oldDP ? changedColor : normalColor);

        // CC
        txtCC.setText(String.format("%02X", newCC));
        txtCC.setBackground(newCC != oldCC ? changedColor : normalColor);

        // 4. Update "Old" values for the next cycle
        oldPC = newPC;
        oldA = newA;
        oldB = newB;
        oldX = newX;
        oldY = newY;
        oldU = newU;
        oldS = newS;
        oldDP = newDP;
        oldCC = newCC;
    }

    private void performStep() {
        try {
            // 1. Run the CPU Step
            cpu.step();

            // 2. Update UI
            updateGUI();
            statusLabel.setText("PC: $" + String.format("%04X", cpu.getPC()));
            statusLabel.setForeground(new Color(0, 100, 0));

            // 3. Breakpoint Logic
            String targetHex = txtBreakpoint.getText().trim();
            if (!targetHex.isEmpty()) {
                try {
                    int targetAddr = Integer.parseInt(targetHex, 16);

                    // Check if PC matches Breakpoint
                    if (cpu.getPC() == targetAddr) {
                        // STOP THE SIMULATOR
                        if (runTimer.isRunning()) {
                            runTimer.stop();
                            btnRun.setText("RUN"); // Reset button text
                            btnRun.setBackground(null); // Reset button color
                            JOptionPane.showMessageDialog(this, "Breakpoint Reached at $" + targetHex.toUpperCase());
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid text in the breakpoint box (do nothing)
                }
            }

        } catch (Exception ex) {
            // Handle CPU Errors (Crash)
            if(runTimer != null && runTimer.isRunning()) {
                runTimer.stop();
                btnRun.setText("RUN");
                btnRun.setBackground(null);
            }
            JOptionPane.showMessageDialog(this, "CPU Halted: " + ex.getMessage());
            statusLabel.setText("HALTED: " + ex.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }
    // ---------------------------------------------------------
    // HELPER: Show Opcode Reference Table
    // ---------------------------------------------------------
    private void showOpcodeReference() {
        // 1. Create the Data
        String[] columnNames = {"Opcode (Hex)", "Mnemonic", "Addressing", "Description"};

        Object[][] data = {
                // --- LOAD & STORE ---
                {"86", "LDA #", "Immediate", "Load A with value"},
                {"96", "LDA $", "Direct", "Load A from direct address"},
                {"B6", "LDA $$", "Extended", "Load A from extended address"},
                {"A6", "LDA ,", "Indexed", "Load A (Indexed)"},
                {"C6", "LDB #", "Immediate", "Load B with value"},
                {"97", "STA $", "Direct", "Store A to memory"},
                {"B7", "STA $$", "Extended", "Store A to memory"},
                {"CC", "LDD #", "Immediate", "Load D (16-bit) with value"},

                // --- ARITHMETIC ---
                {"8B", "ADDA #", "Immediate", "Add value to A"},
                {"9B", "ADDA $", "Direct", "Add memory to A"},
                {"CB", "ADDB #", "Immediate", "Add value to B"},
                {"80", "SUBA #", "Immediate", "Subtract value from A"},
                {"C3", "ADDD #", "Immediate", "Add 16-bit value to D"},
                {"4C", "INCA", "Inherent", "Increment A"},
                {"4A", "DECA", "Inherent", "Decrement A"},

                // --- LOGIC ---
                {"84", "ANDA #", "Immediate", "Bitwise AND A"},
                {"8A", "ORA #", "Immediate", "Bitwise OR A"},
                {"88", "EORA #", "Immediate", "Exclusive OR A"},
                {"4F", "CLRA", "Inherent", "Set A to 00"},
                {"43", "COMA", "Inherent", "Invert bits of A"},
                {"48", "ASLA", "Inherent", "Arithmetic Shift Left A"},

                // --- BRANCH & JUMP ---
                {"20", "BRA", "Relative", "Branch Always"},
                {"26", "BNE", "Relative", "Branch if Not Equal (Z=0)"},
                {"27", "BEQ", "Relative", "Branch if Equal (Z=1)"},
                {"7E", "JMP", "Extended", "Jump to Address"},

                // --- STACK & MISC ---
                {"34", "PSHS", "Immediate", "Push registers to System Stack"},
                {"35", "PULS", "Immediate", "Pull registers from System Stack"},
                {"3F", "SWI", "Inherent", "Software Interrupt (Stop)"},
                {"12", "NOP", "Inherent", "No Operation"}
        };

        // 2. Create Table
        JTable table = new JTable(data, columnNames);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setAutoCreateRowSorter(true); // <--- PRO FEATURE: Makes table sortable!

        // 3. Create Window
        JDialog dialog = new JDialog(this, "Opcode Reference List", false); // false = non-modal (can keep open)
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.add(new JScrollPane(table));

        // 4. Add a search bar (Extra Pro)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Filter:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);

        // Search Logic
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        dialog.add(searchPanel, BorderLayout.NORTH);
        dialog.setVisible(true);
    }


    // main to lucn the gui
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignored) {}
        // swing thread safety best practise
        SwingUtilities.invokeLater(() -> {
            new Interface().setVisible(true);
        });

    }

}

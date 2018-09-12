package projecttool;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author TDHuy
 */
public class Tool extends javax.swing.JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private ArrayList<String> allFileProject;
    private ArrayList<String> destinationFolders;
    private File listFile;
    private File project;
    private int totalFile = 0;
    private DefaultTableModel table;
    private Desktop desktop;

    /**
     * Creates new form Tool
     */
    public Tool() {
        initComponents();
        allFileProject = new ArrayList<String>();
        destinationFolders = new ArrayList<String>();
        table = (DefaultTableModel) tblListFile.getModel();
        desktop = Desktop.getDesktop();
        setTitle("Project Tool");
        setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getWidth() / 2,
                Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getHeight() / 2);
        setResizable(false);
    }

    public void reset() {
        tfListFile.setText("");
        tfProject.setText("");
        tfSave.setText("");
        progressBar.setStringPainted(false);
        progressBar.setString("");
        progressBar.setValue(0);
        allFileProject = new ArrayList<>();
        destinationFolders = new ArrayList<>();
        int rows = table.getRowCount();
        for (int i = 0; i < rows; i++) {
            table.removeRow(0);
        }
    }

    public void chooseListFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select list file to read");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showSaveDialog(null);
        tfListFile.setText(fileChooser.getSelectedFile().toString());
        listFile = new File(tfListFile.getText());
    }

    public void chooseProject() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select project location");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showSaveDialog(null);
        tfProject.setText(fileChooser.getSelectedFile().toString());
        project = new File(tfProject.getText());
    }

    public void chooseSave() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select save location");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showSaveDialog(null);
        tfSave.setText(fileChooser.getSelectedFile().toString());
    }

    public void readListFile() {
        try {
            StringBuilder content = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(listFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line);
                    content.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void searchListFile() {
        try {
            totalFile = 0;
            progressBar.setStringPainted(false);
            progressBar.setString("");
            progressBar.setValue(0);
            allFileProject = new ArrayList<>();
            destinationFolders = new ArrayList<>();
            int rows = table.getRowCount();
            for (int i = 0; i < rows; i++) {
                table.removeRow(0);
            }
            readingFile(project);
            BufferedReader br = new BufferedReader(new FileReader(listFile));
            try {
                while ((br.readLine()) != null) {
                    totalFile++;
                }
                br.close();
                if (totalFile != 0) {
                    progressBar.setStringPainted(true);
                    BufferedReader brer = new BufferedReader(new FileReader(listFile));
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String line;
                            int count = 0;
                            boolean checkAll = false;
                            try {
                                while ((line = brer.readLine()) != null) {
                                    count++;
                                    boolean checkPerFile = false;
                                    for (String filePath : allFileProject) {
                                        if (line.contains("/")) {
                                            line = line.replaceAll("/", "\\\\");
                                        }
                                        if (line.contains("\\")) {
                                            if (filePath.contains(line)) {
                                                StringBuilder sb = new StringBuilder(line);
                                                sb.reverse();
                                                StringTokenizer st = new StringTokenizer(sb.toString());
                                                sb = new StringBuilder(st.nextToken("/\\"));
                                                String s = sb.reverse().toString();
                                                if (s.contains(".")) {
                                                    copyFile(filePath, tfSave.getText(), s);
                                                    checkAll = true;
                                                    checkPerFile = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (!checkPerFile) {
                                        table.addRow(new Object[] { line, "File not found" });
                                        destinationFolders.add("File not found");
                                    }
                                    progressBar.setString(((count * 100) / totalFile) + "%");
                                    progressBar.setValue((count * 100) / totalFile);
                                    if (count % totalFile == 0) {
                                        if (checkAll) {
                                            JOptionPane.showMessageDialog(null, "DONE!!! New copy was created!");
                                        } else {
                                            JOptionPane.showMessageDialog(null,
                                                    "DONE!!! Do not have any file match with project!");
                                        }
                                    }
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                } else {
                    JOptionPane.showMessageDialog(null, "Do not have any file in list file");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readingFile(File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                readingFile(fileEntry);
            } else {
                allFileProject.add(fileEntry.getAbsolutePath());
            }
        }
    }

    private void copyFile(String filePathSource, String filePathDest, String fileName) throws IOException {
        String folderPath = "";
        if (filePathDest.equals(project.getAbsolutePath())) {
            folderPath = filePathDest + "\\" + project.getName() + " Copy" + filePathSource
                    .substring(project.getAbsolutePath().length(), filePathSource.length() - fileName.length() - 1);
        } else {
            System.out.println(filePathSource);
            System.out.println(fileName);
            folderPath = filePathDest + "\\" + project.getName() + filePathSource
                    .substring(project.getAbsolutePath().length(), filePathSource.length() - fileName.length() - 1);
        }
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(filePathSource);
            os = new FileOutputStream(folder.getAbsolutePath() + "\\" + fileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            table.addRow(new Object[] { fileName, "Done" });
            destinationFolders.add(folder.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            is.close();
            os.close();
        }
    }

    public void moveToFolder() {
        if (tblListFile.getSelectedRow() != -1) {
            File file = new File(destinationFolders.get(tblListFile.getSelectedRow()));
            try {
                desktop.open(file);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Can not to open folder because file does not exist");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Row is not selected");
        }
    }

    @SuppressWarnings("serial")
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnChooseListFile = new javax.swing.JButton();
        btnChooseProject = new javax.swing.JButton();
        tfListFile = new javax.swing.JTextField();
        tfProject = new javax.swing.JTextField();
        btnStart = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        btnReset = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        tfSave = new javax.swing.JTextField();
        btnChooseSave = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblListFile = new javax.swing.JTable();
        btnMove = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("List File");

        jLabel2.setText("Project");

        btnChooseListFile.setText("Browse");
        btnChooseListFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseListFileActionPerformed(evt);
            }
        });

        btnChooseProject.setText("Browse");
        btnChooseProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseProjectActionPerformed(evt);
            }
        });

        btnStart.setText("Start");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        jLabel3.setText("Save");

        btnChooseSave.setText("Browse");
        btnChooseSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseSaveActionPerformed(evt);
            }
        });

        tblListFile.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] { "File", "Status" }) {
            boolean[] canEdit = new boolean[] { false, false };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblListFile);

        btnMove.setText("Move to folder");
        btnMove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout
                .setHorizontalGroup(
                        jPanel1Layout
                                .createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup().addGap(81, 81, 81)
                                        .addGroup(jPanel1Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel3).addComponent(jLabel1).addComponent(jLabel2))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(tfListFile, javax.swing.GroupLayout.DEFAULT_SIZE, 645,
                                                        Short.MAX_VALUE)
                                                .addComponent(tfProject)
                                                .addComponent(tfSave, javax.swing.GroupLayout.Alignment.TRAILING))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(btnChooseListFile,
                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(btnChooseProject,
                                                                javax.swing.GroupLayout.Alignment.TRAILING))
                                                .addComponent(btnChooseSave))
                                        .addGap(67, 67, 67))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                        jPanel1Layout.createSequentialGroup().addContainerGap()
                                                .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addContainerGap())
                                .addGroup(jPanel1Layout.createSequentialGroup().addGap(206, 206, 206)
                                        .addComponent(btnStart)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnReset).addGap(227, 227, 227))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                        jPanel1Layout.createSequentialGroup().addContainerGap()
                                                .addComponent(jScrollPane1).addContainerGap())
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                        jPanel1Layout.createSequentialGroup()
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(btnMove, javax.swing.GroupLayout.PREFERRED_SIZE, 132,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(24, 24, 24)));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout
                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup().addGap(44, 44, 44)
                                .addComponent(btnChooseListFile))
                        .addGroup(jPanel1Layout.createSequentialGroup().addGap(45, 45, 45)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(tfListFile, javax.swing.GroupLayout.PREFERRED_SIZE, 26,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel1))))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout
                                .createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel2)
                                                .addComponent(tfProject, javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(tfSave).addComponent(jLabel3).addComponent(btnChooseSave))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(btnStart).addComponent(btnReset))
                                        .addGap(10, 10, 10).addComponent(progressBar,
                                                javax.swing.GroupLayout.PREFERRED_SIZE, 33,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(btnChooseProject))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 312,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18).addComponent(btnMove).addGap(8, 8, 8)));

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnChooseListFileActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnChooseListFileActionPerformed
        chooseListFile();
    }// GEN-LAST:event_btnChooseListFileActionPerformed

    private void btnChooseProjectActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnChooseProjectActionPerformed
        chooseProject();
    }// GEN-LAST:event_btnChooseProjectActionPerformed

    private void btnChooseSaveActionPerformed(java.awt.event.ActionEvent evt) {
        chooseSave();
    }// GEN-LAST:event_btnChooseSaveActionPerformed

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnStartActionPerformed
        if (tfListFile.getText().equals("") || tfProject.getText().equals("") || tfSave.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Must to choose all");
        } else {
            if (project != null) {
                searchListFile();
            } else {
                JOptionPane.showMessageDialog(null, "Project folder not found");
            }
        }
    }

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {
        reset();
    }

    private void btnMoveActionPerformed(java.awt.event.ActionEvent evt) {
        moveToFolder();
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Tool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Tool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Tool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Tool().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChooseListFile;
    private javax.swing.JButton btnChooseProject;
    private javax.swing.JButton btnChooseSave;
    private javax.swing.JButton btnMove;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnStart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTable tblListFile;
    private javax.swing.JTextField tfListFile;
    private javax.swing.JTextField tfProject;
    private javax.swing.JTextField tfSave;

}

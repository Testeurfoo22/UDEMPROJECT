package projetfinaldb;

import java.sql.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ProjetFinalDB {

    public static void main(String[] args) throws SQLException {

        Connection myConn = null;
        Statement myStat = null;
        ResultSet myRs = null;

        String user = "postgres";
        String pass = "Test22oo";

        String mess1 = "";
        String mess2 = "";
        String mess3 = "";
        String mess4 = "";

        try {
            //get connection to db
            myConn = DriverManager.getConnection("jdbc:postgresql://"
                    + "localhost:5432/Databases/Foot", user, pass);
            //  
            myStat = myConn.createStatement();
            //this line is the request  
            //Question 1
            myRs = myStat.executeQuery("SELECT H.NOM, H.PRENOM\n"
                    + "FROM Humain H\n"
                    + "JOIN Apparition A on H.NPERS = A.NPERS\n"
                    + "JOIN Match M ON A.NMATH = M.NMATH\n"
                    + "join Equipe E on H.NTEAM = E.NTEAM\n"
                    + "WHERE M.RANG = 'Finale' AND A.ENTREE = 0 and "
                    + "E.CODE = 'FRA';");

            while (myRs.next()) {
                mess1 += myRs.getString("NOM") + ", " + 
                        myRs.getString("PRENOM") + ";\n";
            }
            //Question 2
            myRs = myStat.executeQuery("SELECT H.NOM, H.PRENOM, "
                    + "COUNT(CASE WHEN B.BUTEUR = H.NPERS THEN 1 END) as But, "
                    + "COUNT(CASE WHEN B.PASSEUR = H.NPERS THEN 1 END) as Passe\n"
                    + "FROM Humain H\n"
                    + "JOIN But B ON B.BUTEUR = H.NPERS OR B.PASSEUR = H.NPERS\n"
                    + "JOIN Match M on M.NMATH = B.NMATH\n"
                    + "WHERE M.RANG != 'Groupe' AND B.RANG != 'CSC'\n"
                    + "GROUP BY H.NOM, H.PRENOM\n"
                    + "HAVING COUNT(CASE WHEN B.BUTEUR = H.NPERS THEN 1 END) >= 1\n"
                    + "AND COUNT(CASE WHEN B.PASSEUR = H.NPERS THEN 1 END) >= 1\n"
                    + "ORDER BY But DESC;");

            while (myRs.next()) {
                mess2 += myRs.getString("NOM") + ", " + 
                        myRs.getString("PRENOM") + ", " + 
                        myRs.getString("BUT") + ", " + 
                        myRs.getString("PASSE") + ";\n";
            }

            //Question 3
            myRs = myStat.executeQuery("WITH counts AS (\n"
                    + "SELECT H.NOM, H.PRENOM, E.NOMEQ, COUNT(*) AS nB_Cartons,\n"
                    + "ROW_NUMBER() OVER (PARTITION BY H.NOM, "
                    + "H.PRENOM ORDER BY COUNT(*) DESC) AS rn\n"
                    + "FROM Humain H\n"
                    + "JOIN Arbitre A ON H.NPERS = A.NPRIN\n"
                    + "JOIN Match M ON M.NARBT = A.NARBT\n"
                    + "JOIN Carton C ON C.NMATH = M.NMATH\n"
                    + "JOIN Equipe E ON C.NTEAM = E.NTEAM\n"
                    + "GROUP BY H.NOM, H.PRENOM, E.NOMEQ\n"
                    + ")\n"
                    + "SELECT NOM, PRENOM, NOMEQ, nB_Cartons\n"
                    + "FROM counts\n"
                    + "WHERE rn = 1;");

            while (myRs.next()) {
                mess3 += myRs.getString("NOM") + ", "+ 
                        myRs.getString("PRENOM") + ", "+ 
                        myRs.getString("NOMEQ") + ", "+ 
                        myRs.getString("NB_CARTONS") + ";\n";
            }

            //Question 4
            myRs = myStat.executeQuery("SELECT E.NOMEQ, \n"
                    + "COUNT(CASE WHEN ((M.VAINQUEUR = 'Dom' AND M.NDOMI = "
                    + "E.NTEAM) OR (M.VAINQUEUR = 'Ext' AND M.NEXTE = E.NTEAM)) "
                    + "THEN 1 END) as Victoire, \n"
                    + "COUNT(CASE WHEN M.VAINQUEUR = 'Nul' THEN 1 END) as Nul, \n"
                    + "COUNT(CASE WHEN ((M.VAINQUEUR = 'Dom' AND M.NEXTE = "
                    + "E.NTEAM) OR (M.VAINQUEUR = 'Ext' AND M.NDOMI = E.NTEAM)) "
                    + "THEN 1 END) as Defaite,\n"
                    + "SUM(CASE WHEN M.NDOMI = E.NTEAM THEN split_part"
                    + "(M.RESULTAT, '-', 1)::int ELSE split_part"
                    + "(M.RESULTAT, '-', 2)::int END) as ButPour, \n"
                    + "SUM(CASE WHEN M.NEXTE = E.NTEAM THEN split_part"
                    + "(M.RESULTAT, '-', 1)::int ELSE split_part"
                    + "(M.RESULTAT, '-', 2)::int END) as ButContre\n"
                    + "FROM Equipe E\n"
                    + "JOIN Match M on M.NDOMI = E.NTEAM OR M.NEXTE = E.NTEAM\n"
                    + "GROUP by E.NOMEQ\n"
                    + "ORDER BY Victoire DESC;");

            while (myRs.next()) {
                mess4 += myRs.getString("NOMEQ") + ", "+ 
                        myRs.getString("VICTOIRE") + ", "+ 
                        myRs.getString("NUL") + ", "+ 
                        myRs.getString("DEFAITE") + ", "+ 
                        myRs.getString("BUTPOUR") + ", "+ 
                        myRs.getString("BUTCONTRE") + ";\n";
            }

        } catch (Exception exc) {
            exc.printStackTrace();

        } finally {
            if (myRs != null) {
                myRs.close();
            }
            if (myStat != null) {
                myStat.close();
            }
            if (myConn != null) {
                myConn.close();
            }

        }

        final String message1 = mess1;
        final String message2 = mess2;
        final String message3 = mess3;
        final String message4 = mess4;

        JFrame f = new JFrame();//creating instance of JFrame  

        JMenu menu = new JMenu("File");
        JMenu menu1 = new JMenu("Edit");
        JMenu menu2 = new JMenu("Help");

        JMenuBar m1 = new JMenuBar();
        m1.add(menu);
        m1.add(menu1);
        m1.add(menu2);
        f.setJMenuBar(m1);

        JPanel panel = new JPanel();
        JLabel label = new JLabel("Projet du cours");
        label.setFont(new Font("Arial", Font.PLAIN, 30));
        panel.add(Box.createRigidArea(new Dimension(10, 75)));
        panel.add(label);
        panel.setPreferredSize(new Dimension(75, 75));

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.PAGE_AXIS));
        JButton b1 = new JButton("Question 1");
        JButton b2 = new JButton("Question 2");
        JButton b3 = new JButton("Question 3");
        JButton b4 = new JButton("Question 4");
        panel1.add(Box.createRigidArea(new Dimension(25, 25)));
        panel1.add(b1);
        panel1.add(Box.createRigidArea(new Dimension(25, 25)));
        panel1.add(b2);
        panel1.add(Box.createRigidArea(new Dimension(25, 25)));
        panel1.add(b3);
        panel1.add(Box.createRigidArea(new Dimension(25, 25)));
        panel1.add(b4);

        JPanel panel2 = new JPanel();
        JTextArea text = new JTextArea();
        text.setPreferredSize(new Dimension(300, 300));
        text.setLineWrap(true);
        panel2.add(text);
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                text.setText(message1);
            }
        });
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                text.setText(message2);
            }
        });
        b3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                text.setText(message3);
            }
        });
        b4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                text.setText(message4);
            }
        });

        f.getContentPane().add(BorderLayout.NORTH, panel);
        f.getContentPane().add(BorderLayout.WEST, panel1);
        f.getContentPane().add(BorderLayout.CENTER, panel2);

        f.setSize(500, 500);//400 width and 500 height  
        f.setVisible(true);//making the frame visible  
    }

}

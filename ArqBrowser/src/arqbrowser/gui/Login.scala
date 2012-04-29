package arqbrowser.gui

import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.JPasswordField
import javax.swing.JFrame
import javax.swing.JButton
import javax.swing.JOptionPane
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import com.amazonaws.auth.BasicAWSCredentials
import arqbrowser.lib.ArqStore
import com.amazonaws.services.s3.AmazonS3Client
import javax.swing.JTree
import javax.swing.JScrollPane
import java.io.File

object Login {

  def create(): Unit = {
    val panel: JPanel = new JPanel();

    val awsPublic = new JTextField(30);
    val awsPrivate = new JPasswordField(40);
    val button = new JButton("Go");

    // awsPublic.setText(<put default here>);
    // awsPrivate.setText(<put default here>);

    panel.add(awsPublic);
    panel.add(awsPrivate);
    panel.add(button);

    button.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent): Unit = {
        val awsCred = new BasicAWSCredentials(
          awsPublic.getText(),
          new String(awsPrivate.getPassword()));

        val tree = new JTree(new S3BucketTreeNode(awsCred));
        val button = new JButton("GO!");
        button.addActionListener(new ActionListener {
          def actionPerformed(e: ActionEvent): Unit = {
            val obj = tree.getSelectionPath().getLastPathComponent();
            System.out.println(obj);

            obj match {
              case file: ArqTreeFileNode => {
                file.saveToDisk(new File("/tmp/" + file.toString));
              }
            }
          }
        });
        val panel = new JPanel();
        panel.add(new JScrollPane(tree));
        panel.add(button);
        val frame = new JFrame();
        frame.add(panel);
        frame.setSize(800, 600);
        frame.setVisible(true);
      }
    });

    val frame = new JFrame();
    frame.add(panel);
    frame.pack();
    frame.setVisible(true);
  }

}
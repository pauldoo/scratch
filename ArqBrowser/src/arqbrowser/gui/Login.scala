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
        val frame = new JFrame();
        frame.add(new JScrollPane(tree));
        frame.setSize(800, 600);
        frame.show();
      }
    });

    val frame = new JFrame();
    frame.add(panel);
    frame.pack();
    frame.show();
  }

}
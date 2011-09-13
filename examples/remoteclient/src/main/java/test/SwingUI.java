/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.openengsb.core.api.remote.MethodResult;

public class SwingUI {

    public static void main(String[] args) {
        // Create and set up the window.
        final JFrame frame = new JFrame("OpenEngSB Test Client");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout(FlowLayout.LEFT));

        JPanel inputPanel = new JPanel();
        GroupLayout groupLayout = new GroupLayout(inputPanel);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setAutoCreateContainerGaps(true);
        inputPanel.setLayout(groupLayout);
        frame.add(inputPanel);

        // JPanel usernamePanel = new JPanel(new FlowLayout());
        // inputPanel.add(usernamePanel, BoxLayout.Y_AXIS);

        JLabel usernameLabel = new JLabel("username");
        final JTextField username = new JTextField(50);
        //
        // username.setMaximumSize(new Dimension(50, (int) username.getPreferredSize().getHeight()));
        // username.setMinimumSize(new Dimension(500, (int) username.getPreferredSize().getHeight()));

        // inputPanel.add(usernameLabel);
        // inputPanel.add(username);

        JLabel passwordLabel = new JLabel("password");
        final JTextField password = new JTextField(50);
        // inputPanel.add(passwordLabel)
        // inputPanel.add(password);

        JLabel messageLabel = new JLabel("message");
        final JTextArea message = new JTextArea(5, 50);
        // inputPanel.add(messageLabel);
        // inputPanel.add(message);

        final JTextArea result = new JTextArea(" ");
        result.setOpaque(false);
        result.setBorder(null);
        result.setFocusable(false);
        result.setMaximumSize(new Dimension(490, 20));

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = username.getText();
                String pw = password.getText();
                String msg = message.getText();
                MethodResult methodResult;
                try {
                    methodResult = SecureSampleApp.callTestLog(user, pw, msg);
                } catch (Exception e1) {
                    throw new IllegalStateException(e1);
                }
                String resultString = methodResult.toString();
                if (resultString.contains("Exception")) {
                    resultString = StringUtils.substringBefore(resultString, "\n");
                }
                result.setText(resultString);

                result.repaint();
                frame.repaint();
            }
        });

        groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
            .addGroup(groupLayout.createParallelGroup()
                .addComponent(usernameLabel)
                .addComponent(username)
                )
            .addGroup(groupLayout.createParallelGroup()
                .addComponent(passwordLabel)
                .addComponent(password)
            )
            .addGroup(groupLayout.createParallelGroup()
                .addComponent(messageLabel)
                .addComponent(message)
            )
            .addComponent(sendButton)
            );

        groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
            .addGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, true)
                    .addComponent(usernameLabel)
                    .addComponent(passwordLabel)
                    .addComponent(messageLabel)
                )
                .addGroup(groupLayout.createParallelGroup()
                    .addComponent(username)
                    .addComponent(password)
                    .addComponent(message)
                    .addComponent(sendButton)
                )));

        frame.getContentPane().add(result);

        // Display the window.
        frame.pack();
        frame.setBounds(100, 100, 690, 260);
        frame.setVisible(true);
    }
}

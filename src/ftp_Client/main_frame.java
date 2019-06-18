package ftp_Client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JSeparator;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JFormattedTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JList;


public class main_frame {

	private JFrame frame;
	private JTextField server_address;
	private JTextField server_user;
	private JPasswordField server_psw;
	private JTextField temp_path;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					main_frame window = new main_frame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public main_frame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		FTPRead fr = new FTPRead();
		
		frame = new JFrame();
		frame.setBounds(100, 100, 600, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel label_svaddr = new JLabel("服务器地址：");
		label_svaddr.setBounds(6, 22, 78, 16);
		frame.getContentPane().add(label_svaddr);
		
		JLabel label_svusr = new JLabel("用户名：");
		label_svusr.setBounds(6, 50, 61, 16);
		frame.getContentPane().add(label_svusr);
		
		JLabel label_svpsw = new JLabel("密码：");
		label_svpsw.setBounds(6, 78, 61, 16);
		frame.getContentPane().add(label_svpsw);
		
		server_address = new JTextField();
		server_address.setBounds(96, 17, 130, 26);
		frame.getContentPane().add(server_address);
		server_address.setColumns(10);
		
		server_user = new JTextField();
		server_user.setBounds(96, 45, 130, 26);
		frame.getContentPane().add(server_user);
		server_user.setColumns(10);
		
		server_psw = new JPasswordField();
		server_psw.setBounds(96, 73, 130, 26);
		frame.getContentPane().add(server_psw);
		
		JLabel info = new JLabel("请登录");
		info.setBounds(77, 157, 149, 16);
		frame.getContentPane().add(info);
		
		JLabel label_tpsvaddr = new JLabel("当前服务器：");
		label_tpsvaddr.setBounds(6, 180, 78, 16);
		frame.getContentPane().add(label_tpsvaddr);
		
		JLabel temp_server_address = new JLabel("无");
		temp_server_address.setBounds(106, 180, 138, 16);
		frame.getContentPane().add(temp_server_address);
		
		JLabel label_tpsvusr = new JLabel("当前用户：");
		label_tpsvusr.setBounds(6, 202, 78, 16);
		frame.getContentPane().add(label_tpsvusr);
		
		JLabel temp_server_user = new JLabel("无");
		temp_server_user.setBounds(106, 202, 138, 16);
		frame.getContentPane().add(temp_server_user);
		
		JLabel label_tppath = new JLabel("当前路径");
		label_tppath.setBounds(256, 22, 61, 16);
		frame.getContentPane().add(label_tppath);
		
		JList<String> file_field = new JList<String>();
		file_field.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				 if(file_field.getSelectedIndex() != -1) {
					 if(e.getClickCount() == 2) {
						 try {
							fr.sendCommand("CWD "+file_field.getSelectedValue());
							String line = fr.reader.readLine();
							fr.sendCommand("PWD");
							line = fr.reader.readLine();
				            int closing = line.substring(5).indexOf('"');
				            temp_path.setText(line.substring(5,closing+5));
				            
				            DefaultListModel<String> filelist = new DefaultListModel<String>();
				            filelist.addElement(".");
				            filelist.addElement("..");
				            ArrayList<String> arr_filelist = fr.listFiles(temp_path.getText());
				            for(String filename: arr_filelist) {
				            	filelist.addElement(filename);
				            }
				            file_field.setModel(filelist);
						 }catch(Exception e1) {};
					 }
				 }
			}
		});
		file_field.setLayoutOrientation(JList.VERTICAL_WRAP);
		file_field.setBounds(256, 78, 338, 128);
		frame.getContentPane().add(file_field);
		
		JButton login = new JButton("登录");
		login.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					String result = null;
					String host = server_address.getText();
					String uname = server_user.getText();
					String pwd = new String(server_psw.getPassword());
					result = fr.connect(host, 21, uname, pwd);
					if (result.startsWith("230 ")) {
						info.setText("登录成功！");
						temp_server_address.setText(host);
						temp_server_user.setText(uname);
						
						//get current path
						fr.sendCommand("PWD");
						String line = fr.reader.readLine();
			            int closing = line.substring(5).indexOf('"');
			            temp_path.setText(line.substring(5,closing+5));
			            
			            //get current files list
			            DefaultListModel<String> filelist = new DefaultListModel<String>();
			            filelist.addElement(".");
			            filelist.addElement("..");
			            ArrayList<String> arr_filelist = fr.listFiles(temp_path.getText());
			            for(String filename: arr_filelist) {
			            	filelist.addElement(filename);
			            }
			            file_field.setModel(filelist);
						
					}else {
						info.setText("登录失败！");
					}
				}catch(Exception e1){
					info.setText("登录失败！");
					temp_server_address.setText("无");
					temp_server_user.setText("无");
				}
			}
		});
		login.setBounds(6, 106, 106, 29);
		frame.getContentPane().add(login);
		
		JButton reset = new JButton("重置");
		reset.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				server_address.setText("");
				server_user.setText("");
				server_psw.setText("");
			}
		});
		reset.setBounds(106, 106, 106, 29);
		frame.getContentPane().add(reset);
		
		temp_path = new JTextField();
		temp_path.setBounds(256, 45, 338, 26);
		frame.getContentPane().add(temp_path);
		temp_path.setColumns(10);
		
		JButton download = new JButton("下载");
		download.setBounds(256, 214, 117, 29);
		frame.getContentPane().add(download);
		
		JButton upload = new JButton("上传");
		upload.setBounds(477, 214, 117, 29);
		frame.getContentPane().add(upload);
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(256, 239, 338, 20);
		frame.getContentPane().add(progressBar);
		
		JScrollPane scrollPane = new JScrollPane(file_field);
		scrollPane.setBounds(256, 78, 338, 128);
		frame.getContentPane().add(scrollPane);
		
	}
}

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/*************************/
class MyServer
{
ArrayList al=new ArrayList();
ArrayList users=new ArrayList();
ServerSocket ss;
Socket s;

public final static int PORT=10;
public final static String UPDATE_USERS="updateuserslist:";
public final static String LOGOUT_MESSAGE="@@logoutme@@:";
public MyServer()
{
try{
	ss=new ServerSocket(PORT);
	System.out.println("Server Started "+ss);
	while(true)
	{
	s=ss.accept();
	Runnable r=new MyThread(s,al,users);
	Thread t=new Thread(r);
	t.start();
//	System.out.println("Total alive clients : "+ss.);
	}
   }
catch(Exception e){System.err.println("Server constructor"+e);}
}
/////////////////////////
public static void main(String [] args)
{
new MyServer();
}
/////////////////////////
}
/*************************/
class MyThread implements Runnable
{
Socket s;
ArrayList al;
ArrayList users;
String username;
///////////////////////
MyThread (Socket s, ArrayList al,ArrayList users)
{
this.s=s;
this.al=al;
this.users=users;
try{
	DataInputStream dis=new DataInputStream(s.getInputStream());
	username=dis.readUTF();
	al.add(s);
	users.add(username);
	tellEveryOne("****** "+ username+" Logged in at "+(new Date())+" ******");
	sendNewUserList();
    }
catch(Exception e){System.err.println("MyThread constructor  "+e);}
}
///////////////////////
public void run()
{
String s1;
try{
	DataInputStream dis=new DataInputStream(s.getInputStream());
	do
	{
	s1=dis.readUTF();
	if(s1.toLowerCase().equals(MyServer.LOGOUT_MESSAGE)) break;
//	System.out.println("received from "+s.getPort());
	tellEveryOne(username+" said: "+" : "+s1);
	}
	while(true);
	DataOutputStream tdos=new DataOutputStream(s.getOutputStream());
	tdos.writeUTF(MyServer.LOGOUT_MESSAGE);
	tdos.flush();
	users.remove(username);
	tellEveryOne("****** "+username+" Logged out at "+(new Date())+" ******");
	sendNewUserList();
	al.remove(s);
	s.close();

   }
catch(Exception e){System.out.println("MyThread Run"+e);}
}
////////////////////////
public void sendNewUserList()
{
	tellEveryOne(MyServer.UPDATE_USERS+users.toString());

}
////////////////////////
public void tellEveryOne(String s1)	
{
Iterator i=al.iterator();
while(i.hasNext())
{
try{
	Socket temp=(Socket)i.next();
	DataOutputStream dos=new DataOutputStream(temp.getOutputStream());
	dos.writeUTF(s1);
	dos.flush();
	//System.out.println("sent to : "+temp.getPort()+"  : "+ s1);
   }
catch(Exception e){System.err.println("TellEveryOne "+e);}
}
}
///////////////////////
}
/*********************************/
class MyClient implements ActionListener
{
Socket s;
DataInputStream dis;
DataOutputStream dos;

JButton sendButton, logoutButton,loginButton, exitButton;
JFrame chatWindow;
JTextArea txtBroadcast;
JTextArea txtMessage;
JList usersList;

//////////////////////////
public void displayGUI()
{
chatWindow=new JFrame();
txtBroadcast=new JTextArea(5,30);
txtBroadcast.setEditable(false);
txtMessage=new JTextArea(2,20);
usersList=new JList();

sendButton=new JButton("Send");
logoutButton=new JButton("Log out");
loginButton=new JButton("Log in");
exitButton=new JButton("Exit");

JPanel center1=new JPanel();
center1.setLayout(new BorderLayout());
center1.add(new JLabel("Broad Cast messages from all online users",JLabel.CENTER),"North");
center1.add(new JScrollPane(txtBroadcast),"Center");

JPanel south1=new JPanel();
south1.setLayout(new FlowLayout());
south1.add(new JScrollPane(txtMessage));
south1.add(sendButton);

JPanel south2=new JPanel();
south2.setLayout(new FlowLayout());
south2.add(loginButton);
south2.add(logoutButton);
south2.add(exitButton);

JPanel south=new JPanel();
south.setLayout(new GridLayout(2,1));
south.add(south1);
south.add(south2);

JPanel east=new JPanel();
east.setLayout(new BorderLayout());
east.add(new JLabel("Online Users",JLabel.CENTER),"East");
east.add(new JScrollPane(usersList),"South");

chatWindow.add(east,"East");

chatWindow.add(center1,"Center");
chatWindow.add(south,"South");

chatWindow.pack();
chatWindow.setTitle("Login for Chat");
chatWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
chatWindow.setVisible(true);
sendButton.addActionListener(this);
logoutButton.addActionListener(this);
loginButton.addActionListener(this);
exitButton.addActionListener(this);
logoutButton.setEnabled(false);
loginButton.setEnabled(true);
txtMessage.addFocusListener(new FocusAdapter()
{public void focusGained(FocusEvent fe){txtMessage.selectAll();}});

chatWindow.addWindowListener(new WindowAdapter()
{
public void windowClosing(WindowEvent ev)
{
if(s!=null)
{
JOptionPane.showMessageDialog(chatWindow,"u r logged out right now. ","Exit",JOptionPane.INFORMATION_MESSAGE);
logoutSession();
}
System.exit(0);
}
});
}
///////////////////////////
public void actionPerformed(ActionEvent ev)
{
JButton temp=(JButton)ev.getSource();
if(temp==sendButton)
{
if(s==null)
 	{JOptionPane.showMessageDialog(chatWindow,"u r not logged in. plz login first"); return;}
try{
	dos.writeUTF(txtMessage.getText());
	txtMessage.setText("");
     }
catch(Exception excp){txtBroadcast.append("\nsend button click :"+excp);}
}
if(temp==loginButton)
{
String uname=JOptionPane.showInputDialog(chatWindow,"Enter Your lovely nick name: ");
if(uname!=null)
	clientChat(uname); 
}
if(temp==logoutButton)
{
if(s!=null)
	logoutSession();
}
if(temp==exitButton)
{
if(s!=null)
{
JOptionPane.showMessageDialog(chatWindow,"u r logged out right now. ","Exit",JOptionPane.INFORMATION_MESSAGE);
logoutSession();
}
System.exit(0);
}
}
///////////////////////////
public void logoutSession()
{
if(s==null) return;
try{
dos.writeUTF(MyServer.LOGOUT_MESSAGE);
Thread.sleep(500);
s=null;
}
catch(Exception e){txtBroadcast.append("\n inside logoutSession Method"+e);}

logoutButton.setEnabled(false);
loginButton.setEnabled(true);
chatWindow.setTitle("Login for Chat");
}
//////////////////////////
public void clientChat(String uname)
{
try{
     s=new Socket(InetAddress.getLocalHost(),MyServer.PORT);
     dis=new DataInputStream(s.getInputStream());
     dos=new DataOutputStream(s.getOutputStream());
     ClientThread ct=new ClientThread(dis,this);
     Thread t1=new Thread(ct);
     t1.start();
     dos.writeUTF(uname);
     chatWindow.setTitle(uname+" Chat Window");
    }
catch(Exception e){txtBroadcast.append("\nClient Constructor " +e);}
logoutButton.setEnabled(true);
loginButton.setEnabled(false);
}
///////////////////////////////
public MyClient()
{
  	displayGUI();
//	clientChat();
}
///////////////////////////////
public static void main(String []args)
{
new MyClient();
}
//////////////////////////
}
/*********************************/
class ClientThread implements Runnable
{
DataInputStream dis;
MyClient client;

ClientThread(DataInputStream dis,MyClient client)
{
this.dis=dis;
this.client=client;
}
////////////////////////
public void run()
{
String s2="";
do
    {
	try{
		s2=dis.readUTF();
		if(s2.startsWith(MyServer.UPDATE_USERS))
			updateUsersList(s2);
		else if(s2.equals(MyServer.LOGOUT_MESSAGE))
			break;
		else
			client.txtBroadcast.append("\n"+s2);
		int lineOffset=client.txtBroadcast.getLineStartOffset(client.txtBroadcast.getLineCount()-1);
		client.txtBroadcast.setCaretPosition(lineOffset);
	     }
	catch(Exception e){client.txtBroadcast.append("\nClientThread run : "+e);}
   }
while(true);
}
//////////////////////////
public void updateUsersList(String ul)
{
Vector ulist=new Vector();

ul=ul.replace("[","");
ul=ul.replace("]","");
ul=ul.replace(MyServer.UPDATE_USERS,"");
StringTokenizer st=new StringTokenizer(ul,",");

while(st.hasMoreTokens())
{
String temp=st.nextToken();
ulist.add(temp);
}
client.usersList.setListData(ulist);
}
/////////////////////////
}
/*********************************/

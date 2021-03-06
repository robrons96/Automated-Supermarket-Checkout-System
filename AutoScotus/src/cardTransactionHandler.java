import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class cardTransactionHandler implements ActionListener {
    
	double total;  
	JPanel panel; 
	String number, pin; 
	JTextField cardNumber, pinNumber;
	LinkedList<ArrayList<String>> tempCheckout; 
	JButton exit;
	JavaDB instance;

	public cardTransactionHandler(double total, JPanel panel, JTextField cardNumber, LinkedList<ArrayList<String>> tempCheckout, JButton cancelcheckoutButton, JavaDB instance) {
		this.total = total; 
		this.panel = panel; 
		this.cardNumber = cardNumber; 
		this.tempCheckout = tempCheckout; 
		exit = cancelcheckoutButton; 
		this.instance = instance; 
		
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {

		this.number = cardNumber.getText();
	    Authorization authenticate = new Authorization(number);
	    
	    if(authenticate.isCredit()) {
	    	panel.remove(cardNumber);	    	
	    	panel.remove(1);
	    	panel.repaint();
	    	pinNumber = new JTextField();
	    	pinNumber.setText(" ENTER PIN");
	    	pinNumber.setBounds(10, 70, 80, 20);
	    	panel.add(pinNumber);
	    	
	    	//Clears default text when mouse is clicked on the JTextField
			
			pinNumber.addMouseListener(new MouseAdapter(){
	            @Override
	            public void mouseClicked(MouseEvent e){
	            	pinNumber.setText("");
	            }
	        });
			
			JButton pinEnterButton = new JButton(); 
			pinEnterButton.setText("Enter");
			pinEnterButton.setBounds(301, 70, 80, 19);
			panel.add(pinEnterButton);
			
			pinEnterButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					pin = pinNumber.getText(); 
			    	if(authenticate.isCorrectPin(pin)) {
			    		transact(); 
			    	}
				}});

			
	    } else if(authenticate.isValid()) {
	    	transact(); 
	    } else {

	    	JLabel text = new JLabel("Invalid Card. Please Enter a new card number."); 
		    
			text.setBounds(10, 150, 600, 30);
			
			text.setFont(new Font("Serif", Font.ITALIC, 24));
			
			panel.add(text);
			
			panel.repaint();
	    }


	}
	
	public void transact() {
		
		
	   panel.removeAll(); 
	   panel.revalidate();
	   panel.repaint();
	   	   

		JLabel message = new JLabel("Receipt"); 
		
		message.setBounds(10, 10, 600, 50);
		
		message.setFont(new Font("Serif", Font.BOLD, 24));
		
		panel.add(message);
		
		
		Iterator<ArrayList<String>> iter = tempCheckout.iterator(); 
		
		int offset = 50; 
		
		
		while(iter.hasNext()) {
			
			ArrayList<String> al = iter.next(); 
			
			JLabel text = new JLabel(al.get(1)); 
			
			instance.updateInventory("update inventory set quantity = quantity - 1 where ID=" + al.get(0));
           
			LinkedList<ArrayList<String>> output = new LinkedList<>();  
			
			try {
				output = instance.getLog("select *, sold*price from transactions where item=\""+al.get(1)+"\"");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(output.isEmpty()) {
				instance.updateInventory("insert into transactions values ('" + al.get(1) + "', 1, "+al.get(2)+")");
			} else {
				instance.updateInventory("update transactions set sold=sold+1 where item='" + al.get(1) + "'");

			}
			    
			text.setBounds(10, 20 + offset, 600, 30);
				
			text.setFont(new Font("Serif", Font.ITALIC, 24));
				
			panel.add(text);
			
			offset+= 30; 
		}
		
		JLabel textTotal = new JLabel("Total: $" + total); 
	    
		textTotal.setBounds(10, offset + 40, 600, 30);
			
		textTotal.setFont(new Font("Serif", Font.BOLD, 24));
			
		panel.add(textTotal);

		JLabel confCode = new JLabel("Your Authorization number is: " + getSaltString());
		confCode.setBounds(10, offset + 70, 650, 30);	
		confCode.setFont(new Font("Serif", Font.BOLD, 24));
		panel.add(confCode);
		
		JLabel cardInfo = new JLabel("Your card XXXX-XXXX-XXXX-" + number.substring(0, 4) + " has been charged $" + total);
		cardInfo.setBounds(10, offset + 100, 650, 30);	
		cardInfo.setFont(new Font("Serif", Font.PLAIN, 24));
		panel.add(cardInfo);
		
		LinkedList<ArrayList<String>> output = new LinkedList<>(); 
		try {
			output = instance.getCheckoutRestock("select * from inventory having quantity < 5");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		if(!output.isEmpty()) {
			
			Iterator<ArrayList<String>> it = output.iterator();
			
			while(it.hasNext()) {
				
			ArrayList<String> al = it.next();
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
		
			instance.updateInventory("insert into messages values('" + dateFormat.format(date) + " (Only " + al.get(4) + " " + al.get(1) + " remaining.)'"+")");

			
		}
		
		exit.setText("Exit");
		exit.setBounds(300, offset + 190, 60, 30);
		panel.add(exit);
		}
	}
	
	protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
}

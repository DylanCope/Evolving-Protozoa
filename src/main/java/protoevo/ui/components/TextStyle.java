package protoevo.ui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

public class TextStyle {

	private int m_size, m_style;
	private Color m_colour;
	private String m_font;

	public static String fontName = "Fira Code Retina";
	
	public static String numberToString(float d, int dp) {
		float ten = (float) Math.pow(10, dp);
		float v = ((int) (d * ten)) / ten;
		if ((int) v == v)
			return Integer.toString((int) v);
		else
			return Float.toString(v);
	}
	
	public static void loadFonts()
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
		     File f = new File("resources/fonts/FiraCode-Retina.ttf");
		     ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, f));
		} 
		catch (IOException|FontFormatException|NullPointerException e) {
			System.out.println("Could not load FiraCode-Retina font");
		}
	}

	public void setFont(String font)	 { m_font = font;     }
	public void setSize(int size)		 { m_size = size;     }
	public void setStyle(int style)		 { m_style = style;   }
	public void setColor(Color colour)   { m_colour = colour; }
	
	public String getFont()   { return m_font;   }
	public int getSize()	  { return m_size;   }
	public int getStyle()	  { return m_style;  }
	public Color getColor()   { return m_colour; }

}

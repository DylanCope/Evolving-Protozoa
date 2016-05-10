package utils;

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
	
	public static void loadFonts()
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
		     File f = new File("resources/fonts/bubble_sharp.ttf");
		     ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, f));
		} 
		catch (IOException|FontFormatException|NullPointerException e) {
			e.printStackTrace();
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

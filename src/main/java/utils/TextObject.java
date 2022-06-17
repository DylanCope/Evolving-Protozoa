package utils;

import java.awt.*;
import java.awt.font.FontRenderContext;


public class TextObject {
	
	private String m_font;
	private String m_text;
	private Vector2 m_position;
	
	private int m_size, m_style;
	private Color m_colour;

	public TextObject(String text, int size)
	{
		this(text, TextStyle.fontName, size, new Vector2(0f, 0f));
	}

	public TextObject(String text, int size, Vector2 position)
	{
		this(text, TextStyle.fontName, size, position);
	}

	public TextObject(String text, String font, int size, Vector2 position)
	{
		m_position = position;
		m_size = size;
		m_text = text;
		m_font = font;
		
		m_style = Font.PLAIN;
		m_colour = Color.BLACK;

	}
	
	public void render(Graphics2D g)
	{
		g.setFont(new Font(m_font, m_style, m_size));
		g.setColor(m_colour);
		g.drawString(m_text, (int) m_position.getX(), (int) m_position.getY());
	}
	
	public void setTextStyle(TextStyle textStyle)
	{
		m_size = textStyle.getSize();
		m_font = textStyle.getFont();
		m_colour = textStyle.getColor();
		m_style = textStyle.getStyle();
	}
	
	public int getWidth() 
	{
		return (int) 
				new Font(m_font, m_style, m_size)
					.getStringBounds(
						m_text, 
						new FontRenderContext(null, true, false)
					).getWidth();
	}
	
	public int getHeight() {
		return (int) 
				new Font(m_font, m_style, m_size)
					.getStringBounds(
						m_text, 
						new FontRenderContext(null, true, false)
					).getHeight();
	}
	
	public void setText(String text) {
		m_text = text;
	}
	
	public void setFont(String font) {
		m_font = font;
	}
	
	public void setStyle(int style) {
		m_style = style;
	}
	
	public void setSize(int size) {
		m_size = size;
	}
	
	public void setColor(Color colour) {
		m_colour = colour;
	}
	
	public void setPosition(Vector2 position) {
		m_position = position;
	}
	
	public String getText()	{
		return m_text;
	}
	
	public Font getFont() {
		return new Font(m_font, m_style, m_size);
	}
	
	public int getStyle() {
		return m_style;
	}
	
	public int getSize() {
		return m_size;
	}
	public Color getColor() {
		return m_colour;
	}
	
	public Vector2 getPosition() {
		return m_position;
	}
	
}

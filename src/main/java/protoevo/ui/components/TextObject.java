package protoevo.ui.components;

import protoevo.utils.Vector2;

import java.awt.*;
import java.awt.font.FontRenderContext;


public class TextObject implements UIComponent {

	private String font;
	private String text;
	private Vector2 pos;
	private int size, style;
	private Color colour;

	public TextObject(String text, int size)
	{
		this(text, TextStyle.fontName, size, new Vector2(0f, 0f));
	}

	public TextObject(String text, int size, Vector2 position)
	{
		this(text, TextStyle.fontName, size, position);
	}

	public TextObject(String text, int size, Color colour)
	{
		this(text, size);
		this.colour = colour;
	}

	public TextObject(String text, String font, int size, Vector2 position)
	{
		pos = position;
		this.size = size;
		this.text = text;
		this.font = font;

		style = Font.PLAIN;
		colour = Color.WHITE.darker();
	}

	public void render(Graphics2D g)
	{
		g.setFont(new Font(font, style, size));
		g.setColor(colour);
		g.drawString(text, (int) pos.getX(), (int) pos.getY());
	}

	public void setTextStyle(TextStyle textStyle)
	{
		size = textStyle.getSize();
		font = textStyle.getFont();
		colour = textStyle.getColor();
		style = textStyle.getStyle();
	}

	public int getWidth()
	{
		return (int)
				new Font(font, style, size)
						.getStringBounds(
								text,
								new FontRenderContext(null, true, false)
						).getWidth();
	}

	public int getHeight() {
		return (int)
				new Font(font, style, size)
						.getStringBounds(
								text,
								new FontRenderContext(null, true, false)
						).getHeight();
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setFont(String font) {
		this.font = font;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setColor(Color colour) {
		this.colour = colour;
	}

	public void setPosition(Vector2 position) {
		pos = position;
	}

	public String getText()	{
		return text;
	}

	public Font getFont() {
		return new Font(font, style, size);
	}

	public int getStyle() {
		return style;
	}

	public int getSize() {
		return size;
	}
	public Color getColor() {
		return colour;
	}

	public Vector2 getPosition() {
		return pos;
	}

}

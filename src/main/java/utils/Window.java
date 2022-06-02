package utils;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.Timer;

import core.Controller;
import core.Renderer;
import core.Simulation;
import core.Application;

public class Window extends Canvas implements Runnable, ActionListener
{

	private static final long serialVersionUID = -2111860594941368902L;

	private JFrame frame;
	private Input input;
	private Renderer renderer;
	private Simulation simulation;
	private Controller controller;
	private Graphics2D graphics;
	private int width, height;

	private final Timer timer = new Timer((int) Application.refreshDelay, this);
	
	public Window(String title, Simulation simulation)
	{
		Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		width = (int) d.getWidth();
		height = (int) d.getHeight();
		this.simulation = simulation;
		renderer = new Renderer(simulation, this);

		frame = new JFrame(title);
		frame.setPreferredSize(new Dimension(width, height));
		frame.setMaximumSize(new Dimension(width, height));
		frame.setMinimumSize(new Dimension(width, height));
		
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.add(renderer);
		frame.setVisible(true);
		
		input = new Input();
		controller = new Controller(input, simulation);
		
		renderer.addKeyListener(input);
		renderer.addMouseListener(input);
		renderer.addMouseMotionListener(input);
		renderer.addMouseWheelListener(input);
		renderer.addFocusListener(input);
	}

	@Override
	public void run() 
	{
		timer.start();
	}
	
	@Override
	public void actionPerformed(ActionEvent event)
	{
		controller.update(simulation, renderer);
		renderer.render();
		timer.restart();
	}
	
	public Input getInput() {
		return input;
	}
	
	public Graphics2D getGraphics() {
		return graphics;
	}

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

	public Vector2 getDimensions() {
		return new Vector2(width, height);
	}

	public JFrame getFrame() {
		return frame;
	}
}
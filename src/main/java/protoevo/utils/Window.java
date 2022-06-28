package protoevo.utils;

import protoevo.core.Application;
import protoevo.core.Controller;
import protoevo.core.Renderer;
import protoevo.core.Simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Window extends Canvas implements Runnable, ActionListener
{

	private static final long serialVersionUID = -2111860594941368902L;

	private JFrame frame;
	private Input input;
	private Renderer renderer;
	private Simulation simulation;
	private Controller controller;
	private int width, height;

	private final Timer timer = new Timer((int) Application.refreshDelay, this);
	
	public Window(String title, Simulation simulation)
	{
		Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		width = (int) d.getWidth();
		height = (int) d.getHeight();
//		width = 1920;
//		height = 1080;
		this.simulation = simulation;
		input = new Input();

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

		controller = new Controller(input, simulation, renderer);
		
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
		if (frame.isVisible()) {
			controller.update();
			renderer.render();
			timer.restart();
		}
	}
	
	public Input getInput() {
		return input;
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
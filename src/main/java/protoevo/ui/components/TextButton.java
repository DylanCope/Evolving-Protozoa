package protoevo.ui.components;

import protoevo.utils.Vector2;

import java.awt.*;

public class TextButton implements Clickable {

    private Vector2 pos;
    private TextObject text;
    private final Runnable onClickRunnable;
    private boolean active = true, flipFlop = false, clicked = false;
    private long lastClickTime = 0;
    private int depressTime = 200; // milliseconds
    private Color bgColour = new Color(0, 0, 0, 80);
    private Color depressedColour = new Color(0, 0, 0, 150);
    private float padding;

    public TextButton(TextObject text, Runnable onClick, float padding) {
        this.text = text;
        this.onClickRunnable = onClick;
        this.padding = padding;
    }

    public TextButton(TextObject text, Runnable onClick) {
        this(text, onClick, 0.25f);
    }

    public int getPadAmount() {
        return (int) (padding * text.getHeight());
    }

    @Override
    public int getWidth() {
        return 2 * getPadAmount() + text.getWidth();
    }

    @Override
    public int getHeight() {
        return 2 * getPadAmount() + text.getHeight();
    }

    @Override
    public void deactivate() {
        active = false;
    }

    @Override
    public void activate() {
        active = true;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void onClick() {
        lastClickTime = System.currentTimeMillis();
        this.onClickRunnable.run();
        if (flipFlop)
            clicked = !clicked;
    }

    @Override
    public void render(Graphics2D g) {
        if (!isActive())
            return;

        if (System.currentTimeMillis() - lastClickTime <= depressTime || (flipFlop && clicked))
            g.setColor(depressedColour);
        else
            g.setColor(bgColour);

        g.fillRoundRect(getX(), getY(), getWidth(), getHeight(), getPadAmount(), getPadAmount());

        text.render(g);
    }

    @Override
    public void setPosition(Vector2 pos) {
        this.pos = pos;
        int pad = getPadAmount();
        text.setPosition(pos.add(new Vector2(pad, 2*pad + text.getHeight() / 2f)));
    }

    @Override
    public Vector2 getPosition() {
        return pos;
    }

    public void setBackgroundColour(Color colour) {
        bgColour = colour;
    }

    public void setDepressedColour(Color colour) {
        depressedColour = colour;
    }

    public void setFlipFlop(boolean flipFlop) {
        this.flipFlop = flipFlop;
    }

    public void setPadding(float padding) {
        this.padding = padding;
    }

    public TextObject getText() {
        return text;
    }
}

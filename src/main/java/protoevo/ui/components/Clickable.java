package protoevo.ui.components;

public interface Clickable extends UIComponent {

    void onClick();
    boolean isActive();
    void deactivate();
    void activate();

    default boolean isInClickRegion(int mouseX, int mouseY) {
        return inBounds(mouseX, mouseY);
    }
}

package mostusedips.model.hotkey;

public interface HotKeyExecuter
{
    /**
     * @param modifiers - integer representing the modifier keys
     * @param keyCode - integer representing the code of the key
     * @param isNewKey - when true, means that the key was pressed during hotkey-selection. when false, the actual hotkey was pressed
     */
    public void keyPressed(Integer modifiers, Integer keyCode, boolean isNewKey);
}

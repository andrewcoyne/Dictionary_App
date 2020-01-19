package sample;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.textfield.TextFields;

public class Controller {
    private AnchorPane root;
    private JFXTextField input;
    public void initialize(){
        String[] dictWords = {"hello", "hi"}; //make this the list of terms the user has previously searched
        TextFields.bindAutoCompletion(input, dictWords);
    }
}

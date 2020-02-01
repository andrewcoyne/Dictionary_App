package sample;

import com.jfoenix.controls.JFXAutoCompletePopup;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.textfield.TextFields;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

public class Controller {
    @FXML
    public AnchorPane root;
    public JFXTextField input;
    public JFXTextArea wOTD;
    public JFXTextArea word;
    public JFXTextArea definition;

    public void initialize(){
        JSONParser parser = new JSONParser();
        JSONObject json = new JSONObject();
        try
        {
            File file = new File("D:/Code Projects/DictionaryApp/src/sample"+"/dictionary.json");
            Object object = parser.parse(new FileReader(file));

            //convert Object to JSONObject
            //json = (JSONArray)object;
            json = (JSONObject)object;
        }
        catch(FileNotFoundException fe)
        {
            fe.printStackTrace();
            System.err.println("line 78");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.err.println("line 83");
        }
        Random r = new Random();
        Map<String, JSONValue> map = (Map)json;
        ArrayList<String> list = new ArrayList<>(map.keySet());
        try {
            word.setText(list.get(r.nextInt(list.size()-1)));
            definition.setText(getDef(word.getText()));
        }catch(Exception e){
            System.err.println("Retrieval failed");
        }



        JFXAutoCompletePopup<String> autoCompletePopup = new JFXAutoCompletePopup<>();
        autoCompletePopup.getSuggestions().addAll(list);
        autoCompletePopup.setSelectionHandler(event -> {
            input.setText(event.getObject());
        });
        input.textProperty().addListener(observable -> {
            autoCompletePopup.filter(string -> string.toLowerCase().contains(input.getText().toLowerCase()));
            if (autoCompletePopup.getFilteredSuggestions().isEmpty() || input.getText().isEmpty()) {
                autoCompletePopup.hide();
            } else {
                autoCompletePopup.show(input);
            }
        });
    }
    public void dictSetter(){
        wOTD.setVisible(false);
        word.setVisible(true);
        word.setText(input.getText());
        word.setLayoutY(80);
        definition.setLayoutY(195);
        String def = getDef(input.getText());
        definition.setText(def);
        if(def == null){
            word.setVisible(false);
            definition.setLayoutY(85);
            ArrayList<String> sugs = suggestionGetter();
            String recs = "Perhaps you meant: \n";
            for(int i = 0; i < sugs.size(); i++){
                recs += ""+(i+1)+". "+sugs.get(i);
            }
            definition.setText(recs);
        }
    }
    public ArrayList<String> suggestionGetter(){
        ArrayList<String> sug;
        String text = "apple";
        try {
            text = input.getText();
        } catch (Exception e) {
            System.err.println("WARNING: text assignment failed");
        }
        if (text.contains(" ")) {
            sug = getData(text, true, false);
        } else {
            sug = getData(text, false, false);
        }
        return sug;
    }
    private static ArrayList<JSONObject> builder(String symbol, boolean multiword, boolean regdef) {
        symbol = symbol.replace(" ", "+");
        /*
        if(regdef){
            JSONParser parser = new JSONParser();
            try
            {
                File file = new File("D:/Code Projects/DictionaryApp/src/sample"+"/dictionary.json");
                Object object = parser.parse(new FileReader(file));

                //convert Object to JSONObject
                JSONArray jsonObject = (JSONArray)object;
                JSONObject json = (JSONObject)jsonObject.get(0);
                return json;
            }
            catch(FileNotFoundException fe)
            {
                fe.printStackTrace();
                System.err.println("line 78");
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.err.println("line 83");
            }
        }
        */

        try {
            InputStream is;
            if(multiword) {

                is = new URL("https://api.datamuse.com/words?ml=" + symbol).openStream();

            }else{
                is = new URL("https://api.datamuse.com/sug?s="+symbol).openStream();
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            //System.out.println(jsonText);
            File temp = File.createTempFile("looker", ".json");

            //write it
            BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            bw.write(jsonText);
            bw.close();

            JSONParser parser = new JSONParser();
            try
            {
                Object object = parser.parse(new FileReader(temp));

                //convert Object to JSONObject
                JSONArray jsonObject = (JSONArray)object;
                ArrayList<JSONObject> json = new ArrayList<>();
                for(int i=0; i < 10; i++) {
                    json.add((JSONObject) jsonObject.get(i));
                }
                return json;
            }
            catch(FileNotFoundException fe)
            {
                fe.printStackTrace();
                System.err.println("line 78");
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.err.println("line 83");
            }

        }catch(MalformedURLException e){
            System.err.println("MalformedURLException, lines 49-54.");
        }catch(IOException e){
            e.printStackTrace();
        }catch(Exception e){
            System.err.println("JSONException, lines 49-54.");
        }
        return null;
    }
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        //System.out.println(sb.toString());
        return sb.toString();
    }
    private static ArrayList<String> getData(String symbol, boolean multi, boolean regdef){
        ArrayList<JSONObject> json = builder(symbol, multi, regdef);

        try {
            //json.toString();
            ArrayList<String> dataFinal = new ArrayList<>();
            for(int i=0; i<json.size();i++){
                try {
                    //System.out.println(json.toString());
                    String word = json.get(i).get("word").toString() + "\n";
                    dataFinal.add(word);
                }catch(Exception e){
                    System.err.println("Retrieval failed on " + i);
                    e.printStackTrace();
                }
            }
            return dataFinal;
        }catch(Exception e){
            System.err.println("JSONException, lines 76-83. " + symbol);
        }
        return null;
    }
    private static JSONObject buildertwo(String symbol, boolean multiword, boolean regdef) {
        symbol = symbol.replace(" ", "+");
        if(regdef){
            JSONParser parser = new JSONParser();
            try
            {
                File file = new File(/*"D:/Code Projects/DictionaryApp/src/sample"+*/"/DictionaryApp/sample/dictionary.json");
                Object object = parser.parse(new FileReader(file));

                //convert Object to JSONObject
                //JSONArray jsonObject = (JSONArray)object;
                JSONObject json = (JSONObject)object;
                return json;
            }
            catch(FileNotFoundException fe)
            {
                fe.printStackTrace();
                System.err.println("line 78");
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.err.println("line 83");
            }
        }
        try {
            InputStream is;
            if(multiword) {

                is = new URL("https://api.datamuse.com/words?ml=" + symbol).openStream();

            }else{
                is = new URL("https://api.datamuse.com/sug?s="+symbol).openStream();
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            //System.out.println(jsonText);
            File temp = File.createTempFile("looker", ".json");

            //write it
            BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            bw.write(jsonText);
            bw.close();

            JSONParser parser = new JSONParser();
            try
            {
                Object object = parser.parse(new FileReader(temp));

                //convert Object to JSONObject
                JSONArray jsonObject = (JSONArray)object;
                JSONObject json = (JSONObject)jsonObject.get(0);
                return json;
            }
            catch(FileNotFoundException fe)
            {
                fe.printStackTrace();
                System.err.println("line 78");
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.err.println("line 83");
            }

        }catch(MalformedURLException e){
            System.err.println("MalformedURLException, lines 49-54.");
        }catch(IOException e){
            e.printStackTrace();
        }catch(Exception e){
            System.err.println("JSONException, lines 49-54.");
        }
        return null;
    }
    private static String getDef(String word){
        JSONObject json = buildertwo(word, false, true);
        String def = "";

        try {
            def = (String) json.get(word);
        } catch (Exception e) {
            System.err.println("Could not get definition");
        }

        return def;
    }
}


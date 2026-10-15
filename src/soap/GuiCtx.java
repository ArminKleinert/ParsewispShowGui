package soap;

import de.kleinert.parsewisp.parser.Parser;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

public class GuiCtx {
    public GuiCtx() {
    }

    /*
        {
         :non-term-input  nil
         :text-input      nil
         :zoom-factor     0.5
         :parse-trees     []
         :result-panel    nil
         :img-chooser     nil

         :all-parses      false

         :auto-whitespace (insta/parser "whitespace = #'\\s+'")
         }
         */
    UndoableTextArea nonTermInput = null;
    UndoableTextArea textInput = null;
    double zoomFactor = 0.5;
    List parseTrees = List.of();
    ResultShowPanel resultPanel = null;
    Object imgChooser = null;
    boolean allParses = false;
    Parser autoWhitespace = null;

    public UndoableTextArea getNonTermInput() {
        return nonTermInput;
    }

    public UndoableTextArea getTextInput() {
        return textInput;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public List getParseTrees() {
        return parseTrees;
    }

    public ResultShowPanel getResultPanel() {
        return resultPanel;
    }

    public Object getImgChooser() {
        return imgChooser;
    }

    public boolean isAllParses() {
        return allParses;
    }

    public Parser getAutoWhitespace() {
        return autoWhitespace;
    }

    public void setNonTermInput(UndoableTextArea nonTermInput) {
        this.nonTermInput = nonTermInput;
    }

    public void setTextInput(UndoableTextArea textInput) {
        this.textInput = textInput;
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public void setParseTrees(List parseTrees) {
        this.parseTrees = parseTrees;
    }

    public void setResultPanel(ResultShowPanel resultPanel) {
        this.resultPanel = resultPanel;
    }

    public void setImgChooser(Object imgChooser) {
        this.imgChooser = imgChooser;
    }

    public void setAllParses(boolean allParses) {
        this.allParses = allParses;
    }

    public void setAutoWhitespace(Parser autoWhitespace) {
        this.autoWhitespace = autoWhitespace;
    }

    /*
    (defn reset-output
  []
  (doto ^ResultShowPanel (.getResultPanel @gui-ctx)
    (.setInfoText "")
    (.setGrammarInfoText "")
    (.setTokensInfoText "")
    (.setAstInfoText "")
    (.setDropdownChooserElements [])))
     */
    public void resetOutput() {
        var panel = getResultPanel();
        panel.setInfoText("");
        panel.setGrammarInfoText("");
        panel.setTokensInfoText("");
        panel.setAstInfoText("");
        try {
            panel.setDropdownChooserElements(List.of());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    (defn load-grammar
  [file]
  (println file)
  (reset-output)
  (let [grammar (slurp file)]
    (println grammar)
    (.setText (.getNonTermInput ^GuiCtx @gui-ctx) grammar)))
     */
    public void loadGrammar(String file) throws IOException {
        System.out.println(file);
        resetOutput();
        var grammar = Files.readString(Path.of(file));
        System.out.println(grammar);
        getNonTermInput().setText(grammar);
    }

    /*
    (defn create-grammar-menu-items
  []
  (let [files (GuiUtils/listFiles "grammars")]
    (map
      (fn [^String f]
        (doto (JMenuItem. f)
          (.addActionListener (proxy [ActionListener] [] (actionPerformed [_] (.loadGrammar @gui-ctx (str "grammars/" f ".g")))))))
      files)
    ))
     */
    public List<JMenuItem> createGrammarMenuItems() throws IOException {
        var files = GuiUtils.listFiles("grammars");
        return files.stream().map(f -> {
            var element = new JMenuItem(f);
            element.addActionListener(a -> {
                try {
                    loadGrammar("grammars/" + f + ".g");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            return element;
        }).toList();
    }

    /*
    (defn make-dropdown-image-chooser-elements
  [parse-trees]
  (let [^ResultShowPanel chooser (.getResultPanel @gui-ctx)]
    (.setDropdownChooserElements chooser (range (count parse-trees)))
    ))
     */
    public void makeDropdownImageChooserElements(List parseTrees) throws IOException {
        var chooser = getResultPanel();
        chooser.setDropdownChooserElements(
                IntStream.range(0, parseTrees.size()).mapToObj(it -> (Object) it).toList());
    }
}

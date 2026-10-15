package soap;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.ParsingOptions;
import de.kleinert.parsewisp.result.ParseResult;
import de.kleinert.parsewisp.result.ParseTree;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class GUI {
    private void addAllMenuItems(JMenu menu, List<JMenuItem> menuItems) {
        for (JMenuItem menuItem : menuItems) {
            menu.add(menuItem);
        }
    }

    public void executeMenuItemAction(GuiCtx guiCtx) throws IOException {
        var grammar = guiCtx.getNonTermInput().getText();
        var makerParserResult = tryMakeParser(guiCtx, grammar);

        if (makerParserResult.isSuccess()) {
            var parseRes = tryParsing(
                    makerParserResult.getParser(),
                    guiCtx.getTextInput().getText(),
                    guiCtx.isAllParses() ? 1000000 : 1);
            var success = parseRes.isSuccess();
            var timeNanos = parseRes.getTimeNanos();
            var firstResult = parseRes.getFirstResult();
            var res = parseRes.getResult();
            showParsingResult(guiCtx, success, firstResult, res, timeNanos, makerParserResult.getParser().show());
        } else {
            showGrammarError(guiCtx, makerParserResult.getError());
        }
    }

    public ParserCreationResultInfo tryMakeParser(GuiCtx guiCtx, String grammarText) {
        try {
            var pars = Parsewisp.parser(grammarText, ParserCreationOptions.getDefault().withWhitespaceParser(guiCtx.getAutoWhitespace()));
            return new ParserCreationResultInfo(true, null, pars);
        } catch (RuntimeException e) {
            return new ParserCreationResultInfo(false, e.getMessage(), null);
        }
    }

    public void showGrammarError(GuiCtx guiCtx, String errorText) throws IOException {
        guiCtx.setParseTrees(List.of());
        guiCtx.getResultPanel().setInfoText(errorText);
        guiCtx.makeDropdownImageChooserElements(List.of());
    }

    public ParseResultInfo tryParsing(Parser parser, String text, int limit) {
        var opts = ParsingOptions.getDefault()
                .withFailureIfEmpty(true)
                .withResultLimit(limit);
        var startTime = System.nanoTime();
        ParseResult firstResult;
        List<ParseTree> result;
        var allParses = parser.parses(text, opts);

        if (allParses.isSuccess()) {
            firstResult = allParses.getFirst();
            result = allParses.stream().toList();
        } else {
            firstResult = allParses.castToParsesFailure().asFailure();
            result = List.of();
        }

        var endTime = System.nanoTime();
        return new ParseResultInfo(result, firstResult, startTime, endTime);
    }

    public static JCheckBoxMenuItem menuToggleItemWithAction(String text, ActionListener action) {
        var element = new JCheckBoxMenuItem(text);
        element.addActionListener(action);
        return element;
    }

    public static JRadioButtonMenuItem menuRadioItemWithAction(String text, boolean state, ActionListener action) {
        var element = new JRadioButtonMenuItem(text, state);
        element.addActionListener(action);
        return element;
    }

    public static JMenuItem menuItemWithAction(String text, ActionListener action) {
        var element = new JMenuItem(text);
        element.addActionListener(action);
        return element;
    }

    private ActionListener setAutoWhitespace(GuiCtx ctx, String lhs) {
        return ae -> ctx.setAutoWhitespace(
                lhs == null || lhs.isEmpty()
                        ? null
                        : Parsewisp.parser("whitespace : " + lhs));
    }

    private JMenuBar makeMenuBar(
            GuiCtx ctx
    ) {
        var fileMenu = new JMenu("File");
        fileMenu.add(GuiUtils.menuItemWithAction("Save", actionEvent -> {
        }));
        fileMenu.add(GuiUtils.menuItemWithAction("Load", actionEvent -> {
        }));
        fileMenu.add(GuiUtils.menuItemWithAction("Load Grammar", actionEvent -> {
        }));
        fileMenu.add(GuiUtils.menuItemWithAction("Load text file", actionEvent -> {
        }));
        fileMenu.add(GuiUtils.menuItemWithAction("Close", (actionEvent -> System.exit(0))));

        var grammarsMenu = new JMenu("Grammars");
        grammarsMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent menuEvent) {
                try {
                    addAllMenuItems(grammarsMenu, ctx.createGrammarMenuItems());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void menuDeselected(MenuEvent menuEvent) {
                grammarsMenu.removeAll();
            }

            @Override
            public void menuCanceled(MenuEvent menuEvent) {
                grammarsMenu.removeAll();
            }
        });
        grammarsMenu.setMnemonic(KeyEvent.VK_F);

        var whitespaceMenu = new JMenu("Whitespace");
        var whitespaceMenuButtonGroup = new ButtonGroup();
        var whitespaceMenuButtons = List.of(
                menuRadioItemWithAction(
                        "Ignore all whitespace: (whitespace : #'\\s+')",
                        true,
                        setAutoWhitespace(ctx, "#'\\s+'")),
                menuRadioItemWithAction(
                        "Newlines only: (whitespace : '\\n')",
                        false,
                        setAutoWhitespace(ctx, "'\\n'")),
                menuRadioItemWithAction(
                        "Spaces only: (whitespace : ' ')",
                        false,
                        setAutoWhitespace(ctx, "' '")),
                menuRadioItemWithAction(
                        "Tabs only: (whitespace : '\\t')",
                        false,
                        setAutoWhitespace(ctx, "'\\t'")),
                menuRadioItemWithAction(
                        "Spaces and Tabs only: (whitespace : '\\t' | ' ')",
                        false,
                        setAutoWhitespace(ctx, "'\t' | ' '")),
                menuRadioItemWithAction(
                        "I will do it myself",
                        false,
                        setAutoWhitespace(ctx, null))
        );
        for (JRadioButtonMenuItem btn : whitespaceMenuButtons) {
            whitespaceMenu.add(btn);
            whitespaceMenuButtonGroup.add(btn);
        }

        var runMenu = new JMenu("Run");
        runMenu.add(menuToggleItemWithAction(
                "All parses", actionEvent -> ctx.setAllParses(!ctx.isAllParses())));
        runMenu.add(menuItemWithAction(
                "Execute", actionEvent ->
                {
                    try {
                        executeMenuItemAction(ctx);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ));

        var helpMenu = new JMenu("Help");
        helpMenu.add(menuItemWithAction("Info", actionEvent -> {
        }));

        var menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(grammarsMenu);
        menuBar.add(whitespaceMenu);
        menuBar.add(runMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JSplitPane makeMainPanel(JComponent left, ResultShowPanel right) {
        var mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);

        var width = (int) (32 * 30 * 1.5);
        var height = (int) (20 * 30 * 1.5);
        var size = new Dimension(width, height);
        var halfSize = new Dimension(width / 2, height);

        mainPanel.setSize(size);
        mainPanel.setPreferredSize(size);
        left.setSize(halfSize);
        left.setPreferredSize(halfSize);
        right.setSize(halfSize);
        right.setPreferredSize(halfSize);

        return mainPanel;
    }

    private JSplitPane makeInputPanel(UndoableTextArea nonTermInputArea, UndoableTextArea textInputArea) {
        return new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(nonTermInputArea),
                new JScrollPane(textInputArea));
    }

    String makeInfoText(boolean success, int treeCount, ParseResult parsingResult, long timeNanos) {
        StringBuilder sb = new StringBuilder();
        sb.append("Success?    ").append(success).append('\n');
        sb.append("Time taken: ").append((timeNanos / 1000000.0)).append(" (ms)").append('\n');
        sb.append("Parses:     ").append(treeCount).append(((treeCount > 1) ? " (ambiguous)" : "")).append('\n');

        if (parsingResult.isFailure()) {
            var pr = parsingResult.castToParseFailure();
            int index = pr.index();
            String text = pr.text();
            String c = text == null || index >= text.length()
                    ? "EOF"
                    : String.valueOf(pr.text().charAt(index));
            sb.append("=============================================\n");
            sb.append("Index:      ").append(index).append('\n');
            sb.append("Line:       ").append(pr.line()).append('\n');
            sb.append("Column:     ").append(pr.column()).append('\n');
            sb.append("Char:      \"").append(c).append('"').append('\n');
            sb.append("Expected:   ");
            var iter = pr.reasonList().stream().distinct().iterator();
            while (iter.hasNext()) {
                var r = iter.next();
                sb.append("\n    ").append(r.rule());
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    public void showParsingResult(GuiCtx guiCtx, boolean success, ParseResult firstResult, List<ParseTree> parsingResult, long timeNanos, String grammarText) throws IOException {
        var resultShower = guiCtx.resultPanel;
        var trees = success ? parsingResult : List.of();

        guiCtx.setParseTrees(trees);
        resultShower.setInfoText(makeInfoText(success, trees.size(), firstResult, timeNanos));
        resultShower.setGrammarInfoText("Grammar:\n" + grammarText);
        resultShower.setTokensInfoText(success ? "Success." : "Error info not implemented.");
        guiCtx.makeDropdownImageChooserElements(trees);
    }

    public GUI() {
    }

    public void startup(GuiCtx guiCtx) throws IOException, FontFormatException {
        var window = new JFrame();

        var font1 = Font.createFont(Font.TRUETYPE_FONT, new File("./font/NotoSansMono.ttf")).deriveFont(21.0f);

        var ntInput = new UndoableTextArea("S = A+ ; A = A '1' | '1'", font1);
        var textInput = new UndoableTextArea("111111111", font1);
        guiCtx.setNonTermInput(ntInput);
        guiCtx.setTextInput(textInput);

        var resultShower = new ResultShowPanel(font1, guiCtx);
        guiCtx.setResultPanel(resultShower);

        var inputPanel = makeInputPanel(ntInput, textInput);
        var mainPanel = makeMainPanel(inputPanel, resultShower);

        window.setJMenuBar(makeMenuBar(
                guiCtx));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Mini");
        window.add(mainPanel);
        window.pack();
        window.setVisible(true);
    }
}

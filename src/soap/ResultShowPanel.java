package soap;

import de.kleinert.parsewisp.result.ParseTree;
import de.kleinert.parsewisp.viz.Viztool;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ResultShowPanel extends JTabbedPane {
    private final JLabel map;
    private final JComboBox<Object> dropdownImageChooser;
    final JTextArea infoTextArea, grammarInfoTextArea, tokensInfoTextArea, astInfoTextArea;
    double zoomFactor = 0.5;
    private final JSplitPane splitPane;

    public ResultShowPanel(final Font font, GuiCtx guiCtx) {
        super();

        map = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.scale(zoomFactor, zoomFactor);
                validate();
                super.paintComponent(g);
            }
        };

        dropdownImageChooser = new JComboBox<>();

        dropdownImageChooser.addItemListener(
                e -> {
                    if (dropdownImageChooser.getSelectedItem() != null) {
                        try {
                            File outputFile = dumpParseTree(
                                    (Integer)dropdownImageChooser.getSelectedItem(),
                                    guiCtx
                            );
                            setImage((File) outputFile);
                        } catch (IOException | InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
        );


        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dropdownImageChooser, new JScrollPane(map));

        infoTextArea = new JTextArea();
        infoTextArea.setFont(font);
        infoTextArea.setEditable(false);

        grammarInfoTextArea = new JTextArea();
        grammarInfoTextArea.setFont(font);
        grammarInfoTextArea.setEditable(false);

        tokensInfoTextArea = new JTextArea();
        tokensInfoTextArea.setFont(font);
        tokensInfoTextArea.setEditable(false);

        astInfoTextArea = new JTextArea();
        astInfoTextArea.setFont(font);
        astInfoTextArea.setEditable(false);

        //map.icon = ImageIcon(ImageIO.read(File("HirwT.png")))
        map.setAutoscrolls(true);


        add("Image", splitPane);
        add("Info", new JScrollPane(infoTextArea));
        add("Grammar", new JScrollPane(grammarInfoTextArea));
        add("Tokens", new JScrollPane(tokensInfoTextArea));
        add("AST", new JScrollPane(astInfoTextArea));

        MouseAdapter ma = new MouseAdapter() {
            Point origin;

            @Override
            public void mousePressed(final MouseEvent e) {
                map.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                origin = e.getPoint();
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                map.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseDragged(final MouseEvent e) {
                JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, map);
                int deltaX = origin.x - e.getX();
                int deltaY = origin.y - e.getY();

                Rectangle view = viewPort.getViewRect();
                view.x += deltaX;
                view.y += deltaY;

                map.scrollRectToVisible(view);
            }

            @Override
            public void mouseWheelMoved(final MouseWheelEvent e) {
                if (e == null)
                    return;

                if (e.getWheelRotation() < 0)
                    zoomFactor += (1.0 / 16);
                else if (e.getWheelRotation() > 0)
                    zoomFactor -= (1.0 / 16);

                repaint();
            }
        };

        map.addMouseListener(ma);
        map.addMouseMotionListener(ma);
        map.addMouseWheelListener(ma);
    }

    public void setImage(final File f) throws IOException {
        removeAll();
        if (f != null) {
            map.setIcon(new ImageIcon(ImageIO.read(f)));
            add("Image", splitPane);
            add("Info", new JScrollPane(infoTextArea));
            add("Grammar", new JScrollPane(grammarInfoTextArea));
            add("Tokens", new JScrollPane(tokensInfoTextArea));
            add("AST", new JScrollPane(astInfoTextArea));
        } else {
            map.setIcon(null);
            add("Info", new JScrollPane(infoTextArea));
            add("Grammar", new JScrollPane(grammarInfoTextArea));
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 200);
    }

    public void setDropdownChooserElements(final List<Object> elements) throws IOException {
        dropdownImageChooser.removeAllItems();
        setImage(null);
        for (Object element : elements) {
            dropdownImageChooser.addItem(element);
        }
    }

    public void setInfoText(final String text) {
        infoTextArea.setText(text);
    }

    public void setGrammarInfoText(final String text) {
        grammarInfoTextArea.setText(text);
    }

    public void setTokensInfoText(final String text) {
        tokensInfoTextArea.setText(text);
    }

    public void setAstInfoText(final String text) {
        astInfoTextArea.setText(text);
    }
    /*
    (fn [selected-item]
                (as-> (.getParseTrees @soap.soap/gui-ctx) $
                      (nth $ (int selected-item))
                      (instarun.viz/dump-parse-tree "parse-tree" $ 0)
                      (clojure.java.io/file $)))
     */
    public File dumpParseTree(int selectedItemIndex, GuiCtx guiCtx) throws IOException, InterruptedException {
        var trees = guiCtx.getParseTrees();
        var treeSelected = trees.get(selectedItemIndex);
        Viztool.dumpParseTree("parse-tree", (ParseTree) treeSelected);
        var file = "parse-tree.png"; //(String) vizDumpParseTree.invoke("parse-tree", treeSelected, 0);
        return new File(file);
    }
}

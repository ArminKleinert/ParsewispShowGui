package soap;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GuiUtils {

    public static File openFileChooser(final String directory, final String description, final Collection<String> extensions) {
        JFileChooser fileChooser = new JFileChooser(directory);
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f == null) return false;
                if (f.isDirectory()) return true;

                if (extensions.isEmpty())
                    return true;

                for (String extension : extensions) {
                    if (f.getName().endsWith(extension)) return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return description;
            }
        });

        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile();
        return null;
    }

    public static List<String> listFiles(String s) throws IOException {
        File f = new File(s);
        String[] files = f.list((dir, name) -> name.endsWith(".g"));
        assert files != null;
        for (int i = 0; i < files.length; i++) {
            files[i] = files[i].substring(0, files[i].length() - 2);
        }
        Arrays.sort(files);
        return new ArrayList<>(Arrays.asList(files));
    }

    public static JMenuItem menuItemWithAction(String text, ActionListener action) {
        var element = new JMenuItem(text);
        element.addActionListener(action);
        return element;
    }

    //TODO Move
    public static JMenu makeFileMenuPlaceholder() {
        var menu = new JMenu("File");
        menu.add(menuItemWithAction("Save", (actionEvent -> {})));
        menu.add(menuItemWithAction("Load", (actionEvent -> {})));
        menu.add(menuItemWithAction("Load Grammar", (actionEvent -> {})));
        menu.add(menuItemWithAction("Load text file", (actionEvent -> {})));
        menu.add(menuItemWithAction("Close", (actionEvent -> System.exit(0))));
        return menu;
    }
}

package soap;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class UndoableTextArea extends JTextArea {

    public UndoableTextArea(String text, Font font) {
        super(text);
        setAutoscrolls(true);
        setEnabled(true);

        setText(text);
        setEditable(true);
        setFont(font);

        final UndoManager undoMgr = new UndoManager();

        // Add listener for undoable events
        getDocument().addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent pEvt) {
                undoMgr.addEdit(pEvt.getEdit());
            }
        });

        final String UNDO_ACTION = "undo";
        final String REDO_ACTION = "redo";

        // Add undo/redo actions
        getActionMap().put(UNDO_ACTION, new AbstractAction(UNDO_ACTION) {
            public void actionPerformed(ActionEvent pEvt) {
                if (undoMgr.canUndo()) {
                    undoMgr.undo();
                }
            }
        });
        getActionMap().put(REDO_ACTION, new AbstractAction(REDO_ACTION) {
            public void actionPerformed(ActionEvent pEvt) {
                if (undoMgr.canRedo()) {
                    undoMgr.redo();
                }
            }
        });

        // Create keyboard accelerators for undo/redo actions (Ctrl+Z/Ctrl+Y)
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK),
                UNDO_ACTION);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK),
                REDO_ACTION);
    }

    @Override
    public void setText(String s) {
        if (s == null)
            s = "";
        super.setText(s);
    }
}

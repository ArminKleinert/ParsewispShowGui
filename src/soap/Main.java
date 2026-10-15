package soap;

import com.formdev.flatlaf.FlatIntelliJLaf;

import java.awt.*;
import java.io.IOException;

public final class Main {
    public static void main(String[] args) throws IOException, FontFormatException {
        FlatIntelliJLaf.setup();
        new GUI().startup(new GuiCtx());
    }
}

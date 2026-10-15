package soap;

import de.kleinert.parsewisp.parser.Parser;

public class ParserCreationResultInfo {
    boolean success;
    String error;
    Parser parser;

    public ParserCreationResultInfo(boolean success, String error, Parser parser) {
        this.success=success;
        this.error=error;
        this.parser=parser;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public Parser getParser() {
        return parser;
    }
}

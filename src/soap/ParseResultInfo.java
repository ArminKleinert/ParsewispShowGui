package soap;

import de.kleinert.parsewisp.result.ParseResult;
import de.kleinert.parsewisp.result.ParseTree;

import java.util.List;

public class ParseResultInfo {
    boolean success;
    long timeNanos;
    List<ParseTree> result;
    ParseResult firstResult;

    public ParseResultInfo(List<ParseTree> result, ParseResult firstResult, long startTime, long endTime) {
        this.result = result;
        this.firstResult = firstResult;
        this.success = !result.isEmpty();
        this.timeNanos = endTime-startTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getTimeNanos() {
        return timeNanos;
    }

    public ParseResult getFirstResult() {
        return firstResult;
    }

    public List<ParseTree> getResult() {
        return result;
    }
}

package nl.basjes.parse.useragent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DebugUserAgent extends UserAgent {

    private static final Logger LOG = LoggerFactory.getLogger(DebugUserAgent.class);

    List<UserAgent> appliedMatcherResults = new ArrayList<>(32);

    @Override
    public void set(UserAgent newValuesUserAgent) {
        appliedMatcherResults.add(new UserAgent(newValuesUserAgent));
        super.set(newValuesUserAgent);
    }

    @Override
    public void reset() {
        appliedMatcherResults.clear();
        super.reset();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String toMatchTrace() {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("+==========================================\n");
        sb.append("| Matcher results that have been combined  \n");

        for (UserAgent result: appliedMatcherResults){
            sb.append("+------------------------------------------\n");
            for (String fieldName : result.getAvailableFieldNamesSorted()) {
                AgentField field = result.get(fieldName);
                if (field.confidence >= 0) {
                    sb.append('|').append(fieldName).append('(').append(field.confidence).append(") = ").append(field.getValue()).append('\n');
                }
            }
        }

        sb.append("+==========================================\n");
        return sb.toString();
    }


}

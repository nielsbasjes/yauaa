package nl.basjes.parse.useragent.debug;

import nl.basjes.parse.useragent.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebugUserAgent extends UserAgent {

    private static final Logger LOG = LoggerFactory.getLogger(DebugUserAgent.class);

    final List<UserAgent> appliedMatcherResults = new ArrayList<>(32);

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

    public int getNumberOfAppliedMatches() {
        return appliedMatcherResults.size();
    }

    public String toMatchTrace() {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("+==========================================\n");
        sb.append("| Matcher results that have been combined  \n");

        for (UserAgent result: appliedMatcherResults){
            sb.append("+------------------------------------------\n");
            for (String fieldName : result.getAvailableFieldNamesSorted()) {
                AgentField field = result.get(fieldName);
                if (field.getConfidence() >= 0) {
                    sb.append('|').append(fieldName).append('(').append(field.getConfidence()).append(") = ").append(field.getValue()).append('\n');
                }
            }
        }
        sb.append("+==========================================\n");
        return sb.toString();
    }

    public boolean analyzeMatchersResult() {
        boolean passed = true;
        for (String fieldName : getAvailableFieldNamesSorted()) {
            Map<Long, String> receivedValues = new HashMap<>(32);
            for (UserAgent result: appliedMatcherResults) {
                AgentField partialField = result.get(fieldName);
                if (partialField != null && partialField.getConfidence() >= 0) {
                    String previousValue = receivedValues.get(partialField.getConfidence());
                    if (previousValue != null) {
                        if (!previousValue.equals(partialField.getValue())) {
                            if (passed) {
                                LOG.error("***********************************************************");
                                LOG.error("***        REALLY IMPORTANT ERRORS IN THE RULESET       ***");
                                LOG.error("*** YOU MUST CHANGE THE CONFIDENCE LEVELS OF YOUR RULES ***");
                                LOG.error("***********************************************************");
                            }
                            passed = false;
                            LOG.error("Found different value for \"{}\" with SAME confidence {}: \"{}\" and \"{}\"",
                                fieldName, partialField.getConfidence(), previousValue, partialField.getValue());
                        }
                    } else {
                        receivedValues.put(partialField.getConfidence(), partialField.getValue());
                    }
                }
            }
        }
        return passed;
    }


}

package anonymization;

import named.entity.NERType;
import named.entity.NamedEntityExtractor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class VZDeIdentifierImp implements DeIdentifier {

    private final NamedEntityExtractor namedEntityExtractor;
    private final HashSet<NERType> sensibleNerTypes;


    public VZDeIdentifierImp(final NamedEntityExtractor namedEntityExtractor) {
        this.namedEntityExtractor = Objects.requireNonNull(namedEntityExtractor, "Named Entity Recognizer can't be null");
        sensibleNerTypes = getSensibleNERTypes();
    }

    public String getDeIdentifiedText(final String rawText) {
        if (rawText == null) {
            return "";
        }
        final HashMap<String, NERType> namedEntities = namedEntityExtractor.getNamedEntitiesAndPreprocessedText(rawText);
        String preprocessedText = getTextWithoutDigits(rawText);
        for (Map.Entry<String, NERType> namedEntity : namedEntities.entrySet()) {
            String entity = namedEntity.getKey();
            NERType entityType = namedEntity.getValue();
            preprocessedText = getTextWithDeIdentifiedEntity(preprocessedText, entity, entityType);
        }
        return StringUtils.normalizeSpace(preprocessedText);
    }

    protected HashSet<NERType> getSensibleNERTypes() {
        HashSet<NERType> sensibleNerTypes = new HashSet<>();
        sensibleNerTypes.add(NERType.PERSON);
        sensibleNerTypes.add(NERType.MONEY);
        sensibleNerTypes.add(NERType.DATE);
        sensibleNerTypes.add(NERType.MISC);
        sensibleNerTypes.add(NERType.URL);
        sensibleNerTypes.add(NERType.LOCATION);
        sensibleNerTypes.add(NERType.EMAIL);
        return sensibleNerTypes;
    }

    protected String getTextWithDeIdentifiedEntity(String text, String entity, NERType nerType) {

        if (!sensibleNerTypes.contains(nerType)) {
            return text;
        }
        final String searchPattern = "(?i)" + Pattern.quote(entity);
        return text.replaceAll(searchPattern, "#");
    }

    private String getTextWithoutDigits(final String rawText) {
        return rawText.replaceAll("\\p{N}", "#");
    }
}

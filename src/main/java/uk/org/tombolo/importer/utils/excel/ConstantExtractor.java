package uk.org.tombolo.importer.utils.excel;

public class ConstantExtractor implements SingleValueExtractor {

    private String constant;

    public ConstantExtractor(String constant){
        this.constant = constant;
    }

    @Override
    public String extract() {
        return constant;
    }
}

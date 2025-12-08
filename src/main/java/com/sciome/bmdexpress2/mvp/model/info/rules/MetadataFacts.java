package com.sciome.bmdexpress2.mvp.model.info.rules;

/**
 * Facts container for metadata validation rules.
 * Holds the metadata fields being validated and collects validation errors.
 */
public class MetadataFacts extends FactsBase {

    // Metadata fields
    private String subjectType;      // "in vivo" or "in vitro"
    private String species;
    private String strain;
    private String sex;
    private String organ;
    private String cellLine;
    private String testArticle;
    private String casrn;
    private String dsstox;
    private String studyDuration;
    private String articleType;
    private String articleRoute;
    private String articleVehicle;
    private String administrationMeans;
    private String platform;
    private String provider;

    public MetadataFacts() {
    }

    // Builder-style setters for fluent API
    public MetadataFacts subjectType(String value) { this.subjectType = value; return this; }
    public MetadataFacts species(String value) { this.species = value; return this; }
    public MetadataFacts strain(String value) { this.strain = value; return this; }
    public MetadataFacts sex(String value) { this.sex = value; return this; }
    public MetadataFacts organ(String value) { this.organ = value; return this; }
    public MetadataFacts cellLine(String value) { this.cellLine = value; return this; }
    public MetadataFacts testArticle(String value) { this.testArticle = value; return this; }
    public MetadataFacts casrn(String value) { this.casrn = value; return this; }
    public MetadataFacts dsstox(String value) { this.dsstox = value; return this; }
    public MetadataFacts studyDuration(String value) { this.studyDuration = value; return this; }
    public MetadataFacts articleType(String value) { this.articleType = value; return this; }
    public MetadataFacts articleRoute(String value) { this.articleRoute = value; return this; }
    public MetadataFacts articleVehicle(String value) { this.articleVehicle = value; return this; }
    public MetadataFacts administrationMeans(String value) { this.administrationMeans = value; return this; }
    public MetadataFacts platform(String value) { this.platform = value; return this; }
    public MetadataFacts provider(String value) { this.provider = value; return this; }

    // Standard getters
    public String getSubjectType() { return subjectType; }
    public String getSpecies() { return species; }
    public String getStrain() { return strain; }
    public String getSex() { return sex; }
    public String getOrgan() { return organ; }
    public String getCellLine() { return cellLine; }
    public String getTestArticle() { return testArticle; }
    public String getCasrn() { return casrn; }
    public String getDsstox() { return dsstox; }
    public String getStudyDuration() { return studyDuration; }
    public String getArticleType() { return articleType; }
    public String getArticleRoute() { return articleRoute; }
    public String getArticleVehicle() { return articleVehicle; }
    public String getAdministrationMeans() { return administrationMeans; }
    public String getPlatform() { return platform; }
    public String getProvider() { return provider; }

    // Metadata-specific helper methods
    public boolean isInVivo() {
        return "in vivo".equals(subjectType);
    }

    public boolean isInVitro() {
        return "in vitro".equals(subjectType);
    }

    public boolean isOralRoute() {
        return "oral".equals(articleRoute);
    }

    public boolean isInhaledRoute() {
        return "inhaled".equals(articleRoute);
    }

    public boolean isTransdermalRoute() {
        return "transdermal".equals(articleRoute);
    }

    public boolean isElectromagneticRadiation() {
        return "electromagnetic radiation".equals(articleType);
    }
}

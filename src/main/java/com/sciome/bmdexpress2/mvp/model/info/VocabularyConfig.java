package com.sciome.bmdexpress2.mvp.model.info;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration class that loads vocabulary values from vocabulary.yml.
 * Singleton pattern ensures vocabularies are loaded once at startup.
 */
public class VocabularyConfig {

    private static final Logger logger = LoggerFactory.getLogger(VocabularyConfig.class);
    private static final String CONFIG_FILE = "/vocabulary.yml";

    private static VocabularyConfig instance;

    // Vocabulary fields
    private List<String> providers = new ArrayList<>();
    private List<String> platforms = new ArrayList<>();
    private List<String> subjectTypes = new ArrayList<>();
    private List<String> articleRoutes = new ArrayList<>();
    private List<String> articleVehicles = new ArrayList<>();
    private List<String> administrationMeans = new ArrayList<>();
    private List<String> inVitroDurations = new ArrayList<>();
    private List<String> inVivoDurations = new ArrayList<>();
    private List<String> articleTypes = new ArrayList<>();
    private List<String> species = new ArrayList<>();
    private List<String> sexes = new ArrayList<>();
    private List<String> organs = new ArrayList<>();
    private Map<String, List<String>> strains = new HashMap<>();

    /**
     * Get singleton instance
     */
    public static synchronized VocabularyConfig getInstance() {
        if (instance == null) {
            instance = new VocabularyConfig();
            instance.load();
        }
        return instance;
    }

    private VocabularyConfig() {
        // Private constructor for singleton
    }

    /**
     * Load configuration from YAML file
     */
    private void load() {
        try (InputStream inputStream = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                logger.warn("vocabulary.yml not found in classpath, using empty defaults");
                return;
            }

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            VocabularyConfig loaded = mapper.readValue(inputStream, VocabularyConfig.class);

            // Copy loaded values
            this.providers = loaded.providers != null ? loaded.providers : new ArrayList<>();
            this.platforms = loaded.platforms != null ? loaded.platforms : new ArrayList<>();
            this.subjectTypes = loaded.subjectTypes != null ? loaded.subjectTypes : new ArrayList<>();
            this.articleRoutes = loaded.articleRoutes != null ? loaded.articleRoutes : new ArrayList<>();
            this.articleVehicles = loaded.articleVehicles != null ? loaded.articleVehicles : new ArrayList<>();
            this.administrationMeans = loaded.administrationMeans != null ? loaded.administrationMeans : new ArrayList<>();
            this.inVitroDurations = loaded.inVitroDurations != null ? loaded.inVitroDurations : new ArrayList<>();
            this.inVivoDurations = loaded.inVivoDurations != null ? loaded.inVivoDurations : new ArrayList<>();
            this.articleTypes = loaded.articleTypes != null ? loaded.articleTypes : new ArrayList<>();
            this.species = loaded.species != null ? loaded.species : new ArrayList<>();
            this.sexes = loaded.sexes != null ? loaded.sexes : new ArrayList<>();
            this.organs = loaded.organs != null ? loaded.organs : new ArrayList<>();
            this.strains = loaded.strains != null ? loaded.strains : new HashMap<>();

            logger.info("Loaded vocabulary configuration: {} species, {} platforms, {} organs",
                    this.species.size(), this.platforms.size(), this.organs.size());

        } catch (Exception e) {
            logger.error("Failed to load vocabulary.yml: {}", e.getMessage(), e);
        }
    }

    // Getters

    public List<String> getProviders() {
        return providers;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public List<String> getSubjectTypes() {
        return subjectTypes;
    }

    public List<String> getArticleRoutes() {
        return articleRoutes;
    }

    public List<String> getArticleVehicles() {
        return articleVehicles;
    }

    public List<String> getAdministrationMeans() {
        return administrationMeans;
    }

    public List<String> getInVitroDurations() {
        return inVitroDurations;
    }

    public List<String> getInVivoDurations() {
        return inVivoDurations;
    }

    public List<String> getAllDurations() {
        List<String> all = new ArrayList<>();
        all.addAll(inVitroDurations);
        all.addAll(inVivoDurations);
        return all;
    }

    public List<String> getArticleTypes() {
        return articleTypes;
    }

    public List<String> getSpecies() {
        return species;
    }

    public List<String> getSexes() {
        return sexes;
    }

    public List<String> getOrgans() {
        return organs;
    }

    public Map<String, List<String>> getStrains() {
        return strains;
    }

    public List<String> getStrainsForSpecies(String speciesName) {
        return strains.getOrDefault(speciesName, List.of());
    }

    // Setters (required for Jackson deserialization)

    public void setProviders(List<String> providers) {
        this.providers = providers;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    public void setSubjectTypes(List<String> subjectTypes) {
        this.subjectTypes = subjectTypes;
    }

    public void setArticleRoutes(List<String> articleRoutes) {
        this.articleRoutes = articleRoutes;
    }

    public void setArticleVehicles(List<String> articleVehicles) {
        this.articleVehicles = articleVehicles;
    }

    public void setAdministrationMeans(List<String> administrationMeans) {
        this.administrationMeans = administrationMeans;
    }

    public void setInVitroDurations(List<String> inVitroDurations) {
        this.inVitroDurations = inVitroDurations;
    }

    public void setInVivoDurations(List<String> inVivoDurations) {
        this.inVivoDurations = inVivoDurations;
    }

    public void setArticleTypes(List<String> articleTypes) {
        this.articleTypes = articleTypes;
    }

    public void setSpecies(List<String> species) {
        this.species = species;
    }

    public void setSexes(List<String> sexes) {
        this.sexes = sexes;
    }

    public void setOrgans(List<String> organs) {
        this.organs = organs;
    }

    public void setStrains(Map<String, List<String>> strains) {
        this.strains = strains;
    }
}

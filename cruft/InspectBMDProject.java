import java.io.*;
import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;

public class InspectBMDProject {
    public static void main(String[] args) throws Exception {
        System.out.println("Inspecting BMDExpress project structure...\n");
        
        // Load the project using the same method as WasmCompatibleDuckDBExport
        BMDProject project = loadBMDProject("DEHP.bm2");
        
        System.out.println("Project: " + project.getName());
        System.out.println("Dose Response Experiments: " + project.getDoseResponseExperiments().size());
        System.out.println("BMD Results: " + project.getbMDResult().size());
        System.out.println("Category Analysis Results: " + project.getCategoryAnalysisResults().size());
        
        System.out.println("\n=== BMD RESULTS ===");
        for (int i = 0; i < project.getbMDResult().size(); i++) {
            BMDResult result = project.getbMDResult().get(i);
            System.out.println("BMD Result " + i + ":");
            System.out.println("  Name: " + result.getName());
            System.out.println("  BMD Method: " + (result.getBmdMethod() != null ? result.getBmdMethod() : "null"));
            System.out.println("  Dose Response Experiment: " + (result.getDoseResponseExperiment() != null ? result.getDoseResponseExperiment().getName() : "null"));
        }
        
        System.out.println("\n=== CATEGORY ANALYSIS RESULTS ===");
        for (int i = 0; i < project.getCategoryAnalysisResults().size(); i++) {
            CategoryAnalysisResults categoryResults = project.getCategoryAnalysisResults().get(i);
            System.out.println("Category Analysis " + i + ":");
            System.out.println("  Name: " + categoryResults.getName());
            System.out.println("  BMD Result: " + (categoryResults.getBmdResult() != null ? categoryResults.getBmdResult().getName() : "null"));
            System.out.println("  Results count: " + categoryResults.getCategoryAnalsyisResults().size());
        }
        
        System.out.println("\n=== DOSE RESPONSE EXPERIMENTS ===");
        for (int i = 0; i < project.getDoseResponseExperiments().size(); i++) {
            DoseResponseExperiment exp = project.getDoseResponseExperiments().get(i);
            System.out.println("Experiment " + i + ":");
            System.out.println("  Name: " + exp.getName());
            System.out.println("  Dose Groups: " + exp.getDoseGroups().size());
            System.out.println("  Treatments: " + exp.getTreatments().size());
        }
        
        // Check if there are multiple BMD methods that should create separate result sets
        System.out.println("\n=== BMD METHODS ANALYSIS ===");
        for (BMDResult result : project.getbMDResult()) {
            if (result.getBmdMethod() != null) {
                System.out.println("BMD Method: " + result.getBmdMethod() + " -> " + result.getName());
            }
        }
    }
    
    private static BMDProject loadBMDProject(String bm2FilePath) {
        try {
            FileInputStream fileIn = new FileInputStream(new File(bm2FilePath));
            BufferedInputStream bIn = new BufferedInputStream(fileIn, 1024 * 2000);
            ObjectInputStream in = new ObjectInputStream(bIn);
            
            BMDProject project = (BMDProject) in.readObject();
            
            in.close();
            fileIn.close();
            
            return project;
            
        } catch (Exception e) {
            System.err.println("Error loading BMD project: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
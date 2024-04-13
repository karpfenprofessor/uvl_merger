package fish.model;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public abstract class BaseModel {

    // Define variables
    public IntVar region;
    public IntVar habitat;
    public IntVar size;
    public IntVar diet;
    public IntVar fishFamily;
    public IntVar fishSpecies;

    public Model model;
    public Region regionModel;

    public BaseModel() {
        model = new Model();
    }

    public Solver getSolver() {
        return model.getSolver();
    }

    public void solveXNumberOfTimes(int x) {
        Solver solver = getSolver();
        int cnt = 0;
        long msSum = 0;
        do {
            long startTime = System.currentTimeMillis();
            while (solver.solve()) {}
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            msSum = msSum + executionTime;
            getSolver().reset();
            

            cnt++;
        } while (cnt < x);

        System.out.println("Average calculation time in " + regionModel.printRegion() + " over " + cnt + " runs: " + (msSum / cnt) + " ms");
    }

    // Method to solve the model and print the number of solutions
    public void solveAndPrintNumberOfSolutions() {
        Solver solver = getSolver();
        int cnt = 0;
        long startTime = System.currentTimeMillis();
        while (solver.solve()) {
            cnt++;
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        getSolver().reset();
        System.out.println("Number of solutions in " + regionModel.printRegion() + " with calculation time " + executionTime + " ms: " + cnt);
    }

    // Method to solve the model and print the solution
    public void solveAndPrintSolution() {
        long startTime = System.currentTimeMillis();
        getSolver().solve();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        getSolver().reset();

        // Print solution
        System.out.println("Solution found in " + executionTime + " ms");
        System.out.println("  Region: " + regionModel.printRegion() + " | Habitat: " + getHabitat(habitat.getValue()) + " | Size: " + getSize(size.getValue()));
        System.out.println("  Diet: " + getDiet(diet.getValue()) + " | Family: " + getFishFamily(fishFamily.getValue()) + " | Species: " + getFishSpecies(fishSpecies.getValue()) + "\n");
    }

    protected abstract String getHabitat(int value);
    protected abstract String getSize(int value);
    protected abstract String getDiet(int value);
    protected abstract String getFishFamily(int value);
    protected abstract String getFishSpecies(int value);
}

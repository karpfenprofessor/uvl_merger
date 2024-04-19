package fish.model.base;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import java.util.Random;
import org.chocosolver.solver.variables.IntVar;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    public Set<String> constraintsSet;
    protected final Logger logger;

    public BaseModel() {
        model = new Model();
        constraintsSet = new HashSet<>();
        logger = LogManager.getLogger(this.getClass());
    }

    private boolean isConstraintInModel(String description) {
        if (!constraintsSet.contains(description)) {
            constraintsSet.add(description);
            /*System.out.println(
                "[OK] Constraint will be put in the model " + regionModel.printRegion() + ": " + description);*/
            return false;
        }

        /*System.out.println(
                "[ERR] Constraint already in the model " + regionModel.printRegion() + ": " + description);*/
        return true;
    }

    public String getRandomConstraintDescription(IntVar a, int ax, IntVar b, int bx) {
        return "(" + a + "=" + ax + ")=(" + b + "=" + bx + ")";
    }

    public void addRandomConstraints(int numberOfConstraints) {
        long constraintsInModel = model.getNbCstrs();
        System.out.println("START|ADD-RANDOM| " + numberOfConstraints + " |-> " + regionModel.printRegion()
                + " | CONSTRAINTS: " + constraintsInModel);
        Random random = new Random(12345);
        for (int i = 0; i < numberOfConstraints; i++) {
            int constraintType = random.nextInt(5); // Assume 5 different types of constraints
            switch (constraintType) {
                case 0: // Constraint on habitat based on family
                    int familyIndex = random.nextInt(4); // Assuming 4 fish families
                    int habitatType = random.nextInt(2); // 0 for Freshwater, 1 for Saltwater
                    if(!isConstraintInModel(getRandomConstraintDescription(fishFamily, familyIndex, habitat, habitatType))) {
                        model.ifThen(model.arithm(fishFamily, "=", familyIndex),
                            model.arithm(habitat, "=", habitatType));
                    } else {
                        i--;
                    }
                    break;
                case 1: // Constraint on diet based on size
                    int sizeIndex = random.nextInt(3); // 0 for S, 1 for M, 2 for L
                    int dietType = random.nextInt(3); // 0 for Herbivore, 1 for Omnivore, 2 for Carnivore
                    if(!isConstraintInModel(getRandomConstraintDescription(size, sizeIndex, diet, dietType))) {
                        model.ifThen(model.arithm(size, "=", sizeIndex),
                            model.arithm(diet, "=", dietType));
                    } else {
                        i--;
                    }
                    break;
                case 2: // Constraint linking size to habitat
                    int sizeForHabitat = random.nextInt(3); // Size
                    int habitatForSize = random.nextInt(2); // Habitat
                    if(!isConstraintInModel(getRandomConstraintDescription(size, sizeForHabitat, habitat, habitatForSize))) {
                        model.ifThen(model.arithm(size, "=", sizeForHabitat),
                            model.arithm(habitat, "=", habitatForSize));
                    } else {
                        i--;
                    }
                    break;
                case 3: // Species to habitat constraint
                    int speciesIndex = random.nextInt(8); // Assuming 8 species
                    int speciesHabitat = random.nextInt(2); // Habitat
                    if(!isConstraintInModel(getRandomConstraintDescription(fishSpecies, speciesIndex, habitat, speciesHabitat))) {
                        model.ifThen(model.arithm(fishSpecies, "=", speciesIndex),
                            model.arithm(habitat, "=", speciesHabitat));
                    } else {
                        i--;
                    }
                    break;
                case 4: // Diet restrictions based on species
                    int speciesForDiet = random.nextInt(8); // Species
                    int dietForSpecies = random.nextInt(3); // Diet
                    if(!isConstraintInModel(getRandomConstraintDescription(fishSpecies, speciesForDiet, diet, dietForSpecies))) {
                        model.ifThen(model.arithm(fishSpecies, "=", speciesForDiet),
                            model.arithm(diet, "=", dietForSpecies));
                    } else {
                        i--;
                    }
                    break;
            }
        }

        constraintsInModel = model.getNbCstrs();
        System.out.println("END  |ADD-RANDOM| " + numberOfConstraints + " |-> " + regionModel.printRegion()
                + " | CONSTRAINTS: " + constraintsInModel);

    }

    public Solver getSolver() {
        return model.getSolver();
    }

    public Model getModel() {
        return model;
    }

    public void solveXNumberOfTimes(int x) {
        Solver solver = getSolver();
        int cnt = 0;
        long msSum = 0;
        do {
            long startTime = System.currentTimeMillis();
            while (solver.solve()) {
            }
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            msSum = msSum + executionTime;
            getSolver().reset();

            cnt++;
        } while (cnt < x);

        System.out.println("[SOL] Average calculation time in " + regionModel.printRegion() + " over " + cnt + " runs: "
                + (msSum / cnt) + " ms");
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
        System.out.println("[SOL] Number of solutions in " + regionModel.printRegion() + " with calculation time "
                + executionTime + " ms: " + cnt);
    }

    // Method to solve the model and print the solution
    public void solveAndPrintSolution() {
        long startTime = System.currentTimeMillis();
        getSolver().solve();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        getSolver().reset();

        // Print solution
        System.out.println("[SOL] Solution found in " + executionTime + " ms");
        System.out.println("  Region: " + regionModel.printRegion() + " | Habitat: " + getHabitat(habitat.getValue())
                + " | Size: " + getSize(size.getValue()));
        System.out.println("  Diet: " + getDiet(diet.getValue()) + " | Family: " + getFishFamily(fishFamily.getValue())
                + " | Species: " + getFishSpecies(fishSpecies.getValue()) + "\n");
    }

    protected abstract String getHabitat(int value);

    protected abstract String getSize(int value);

    protected abstract String getDiet(int value);

    protected abstract String getFishFamily(int value);

    protected abstract String getFishSpecies(int value);
}

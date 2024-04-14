package fish.model;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;

import java.util.Random;
import org.chocosolver.solver.variables.IntVar;
import java.util.HashSet;
import java.util.Set;

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
    public Set<String> constraintSet;

    public BaseModel() {
        model = new Model();
        constraintSet = new HashSet<>();
    }

    public Solver getSolver() {
        return model.getSolver();
    }

    public Model getModel() {
        return model;
    }

    public boolean addConstraint(Constraint constraint, String constraintDescription) {
        if (!constraintSet.contains(constraintDescription)) {
            constraint.post();
            constraintSet.add(constraintDescription);
            return true;
        }

        System.out.println("[ERR] Constraint already in the model " + regionModel.printRegion() + ": " + constraintDescription);
        return false;
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

    public void addRandomConstraints(int numberOfConstraints) {
        long constraintsInModel = model.getNbCstrs();
        String constraintDescription = null;
        System.out.println("START|ADD-RANDOM| " + numberOfConstraints + " |-> " + regionModel.printRegion() + " | CONSTRAINTS: " + constraintsInModel);
        Random random = new Random(12345);
        for (int i = 0; i < numberOfConstraints; i++) {
            int constraintType = random.nextInt(5); // Assume 5 different types of constraints
            switch (constraintType) {
                case 0: // Constraint on habitat based on family
                    int familyIndex = random.nextInt(4); // Assuming 4 fish families
                    int habitatType = random.nextInt(2); // 0 for Freshwater, 1 for Saltwater
                    constraintDescription = familyIndex + "=" + habitatType;
                    model.ifThen(model.arithm(fishFamily, "=", familyIndex),
                            model.arithm(habitat, "=", habitatType));
                    break;
                case 1: // Constraint on diet based on size
                    int sizeIndex = random.nextInt(3); // 0 for S, 1 for M, 2 for L
                    int dietType = random.nextInt(3); // 0 for Herbivore, 1 for Omnivore, 2 for Carnivore
                    constraintDescription = sizeIndex + "=" + dietType;
                    model.ifThen(model.arithm(size, "=", sizeIndex),
                            model.arithm(diet, "=", dietType));
                    break;
                case 2: // Constraint linking size to habitat
                    int sizeForHabitat = random.nextInt(3); // Size
                    int habitatForSize = random.nextInt(2); // Habitat
                    constraintDescription = sizeForHabitat + "=" + habitatForSize;
                    model.ifThen(model.arithm(size, "=", sizeForHabitat),
                            model.arithm(habitat, "=", habitatForSize));
                    break;
                case 3: // Species to habitat constraint
                    int speciesIndex = random.nextInt(8); // Assuming 8 species
                    int speciesHabitat = random.nextInt(2); // Habitat
                    constraintDescription = speciesIndex + "=" + speciesHabitat;
                    model.ifThen(model.arithm(fishSpecies, "=", speciesIndex),
                            model.arithm(habitat, "=", speciesHabitat));
                    break;
                case 4: // Diet restrictions based on species
                    int speciesForDiet = random.nextInt(8); // Species
                    int dietForSpecies = random.nextInt(3); // Diet
                    constraintDescription = speciesForDiet + "=" + dietForSpecies;
                    model.ifThen(model.arithm(fishSpecies, "=", speciesForDiet),
                            model.arithm(diet, "=", dietForSpecies));
                    break;
            }
        }

        constraintsInModel = model.getNbCstrs();
        System.out.println("END  |ADD-RANDOM| " + numberOfConstraints + " |-> " + regionModel.printRegion()
                + " | CONSTRAINTS: " + constraintsInModel);

    }

    protected abstract String getHabitat(int value);

    protected abstract String getSize(int value);

    protected abstract String getDiet(int value);

    protected abstract String getFishFamily(int value);

    protected abstract String getFishSpecies(int value);
}

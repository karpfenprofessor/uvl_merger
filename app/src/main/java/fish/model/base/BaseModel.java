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

    protected Model model;
    public Region regionModel;
    protected Set<String> constraintsSet;
    protected final Logger logger;
    private Random random = new Random();

    public BaseModel() {
        model = new Model();
        constraintsSet = new HashSet<>();
        logger = LogManager.getLogger(this.getClass());
    }

    private boolean isConstraintInModel(String description) {
        if (!constraintsSet.contains(description)) {
            constraintsSet.add(description);
            logger.info("[OK] Constraint will be put in the model " + printRegion() + ": " + description);

            return false;
        }

        logger.info("[ERR] Constraint already in the model " + printRegion() + ": " + description);
        return true;
    }

    private String getRandomConstraintDescription(IntVar a, String operator1, int ax, IntVar b, String operator2,
            int bx) {
        return "(" + a + operator1 + ax + ")=(" + b + operator2 + bx + ")";
    }

    public String printRegion() {
        return regionModel.printRegion();
    }

    public Region getRegionModel() {
        return regionModel;
    }

    private String getRandomOperator() {
        int operator = random.nextInt(3); // 0 for =, 1 for <=, 2 for >=, 3 for !=

        switch (operator) {
            case 0:
                return "=";
            case 1:
                return "<=";
            case 2:
                return ">=";
            case 3:
                return "!=";

            default:
                logger.error("Wrong Operator Generation!");
                return "=";
        }
    }

    public void addRandomConstraints(int numberOfConstraints) {
        // long constraintsInModel = model.getNbCstrs();
        String operator1 = null;
        String operator2 = null;
        // logger.info("START|ADD-RANDOM| " + numberOfConstraints + " |-> " +
        // regionModel.printRegion()
        // + " | CONSTRAINTS: " + constraintsInModel);

        for (int i = 0; i < numberOfConstraints; i++) {
            int constraintType = random.nextInt(5); // Assume 5 different types of constraints
            switch (constraintType) {
                case 0: // Constraint on habitat based on family
                    int familyIndex = random.nextInt(fishFamily.getLB(), fishFamily.getUB()); // Assuming 4 fish
                                                                                              // families
                    int habitatType = random.nextInt(2); // 0 for Freshwater, 1 for Saltwater
                    operator1 = getRandomOperator();
                    operator2 = getRandomOperator();
                    if (!isConstraintInModel(
                            getRandomConstraintDescription(fishFamily, operator1, familyIndex, habitat, operator2,
                                    habitatType))) {
                        model.ifThen(model.arithm(fishFamily, operator1, familyIndex),
                                model.arithm(habitat, operator2, habitatType));
                    } else {
                        i--;
                    }
                    break;
                case 1: // Constraint on diet based on size
                    int sizeIndex = random.nextInt(3); // 0 for S, 1 for M, 2 for L
                    int dietType = random.nextInt(3); // 0 for Herbivore, 1 for Omnivore, 2 for Carnivore
                    operator1 = getRandomOperator();
                    operator2 = getRandomOperator();
                    if (!isConstraintInModel(
                            getRandomConstraintDescription(size, operator1, sizeIndex, diet, operator2, dietType))) {
                        model.ifThen(model.arithm(size, operator1, sizeIndex),
                                model.arithm(diet, operator2, dietType));
                    } else {
                        i--;
                    }
                    break;
                case 2: // Constraint linking size to habitat
                    int sizeForHabitat = random.nextInt(3); // Size
                    int habitatForSize = random.nextInt(2); // Habitat
                    operator1 = getRandomOperator();
                    operator2 = getRandomOperator();
                    if (!isConstraintInModel(
                            getRandomConstraintDescription(size, operator1, sizeForHabitat, habitat, operator2,
                                    habitatForSize))) {
                        model.ifThen(model.arithm(size, operator1, sizeForHabitat),
                                model.arithm(habitat, operator2, habitatForSize));
                    } else {
                        i--;
                    }
                    break;
                case 3: // Species to habitat constraint
                    int speciesIndex = random.nextInt(fishSpecies.getLB(), fishSpecies.getUB()); // Assuming 8 species
                    int speciesHabitat = random.nextInt(2); // Habitat
                    operator1 = getRandomOperator();
                    operator2 = getRandomOperator();
                    if (!isConstraintInModel(
                            getRandomConstraintDescription(fishSpecies, operator1, speciesIndex, habitat, operator2,
                                    speciesHabitat))) {
                        model.ifThen(model.arithm(fishSpecies, operator1, speciesIndex),
                                model.arithm(habitat, operator2, speciesHabitat));
                    } else {
                        i--;
                    }
                    break;
                case 4: // Diet restrictions based on species
                    int speciesForDiet = random.nextInt(fishSpecies.getLB(), fishSpecies.getUB()); // Species
                    int dietForSpecies = random.nextInt(3); // Diet
                    operator1 = getRandomOperator();
                    operator2 = getRandomOperator();
                    if (!isConstraintInModel(
                            getRandomConstraintDescription(fishSpecies, operator1, speciesForDiet, diet, operator2,
                                    dietForSpecies))) {
                        model.ifThen(model.arithm(fishSpecies, operator1, speciesForDiet),
                                model.arithm(diet, operator2, dietForSpecies));
                    } else {
                        i--;
                    }
                    break;
            }
        }

        // constraintsInModel = model.getNbCstrs();
        // logger.info("END |ADD-RANDOM| " + numberOfConstraints + " |-> " +
        // regionModel.printRegion()
        // + " | CONSTRAINTS: " + constraintsInModel);

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

        logger.debug("[SOL] Average calculation time in " + regionModel.printRegion() + " over " + cnt + " runs: "
                + (msSum / cnt) + " ms");
    }

    // Method to solve the model and print the number of solutions
    public int solveAndPrintNumberOfSolutions() {
        int cnt = 0;
        long startTime = System.currentTimeMillis();
        while (getSolver().solve()) {
            cnt++;
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        getSolver().reset();
        logger.debug("[SOL] Number of solutions in " + regionModel.printRegion() + " with calculation time "
                + executionTime + " ms: " + cnt);
        return cnt;
    }

    // Method to solve the model and print the solution
    public void solveAndPrintSolution() {
        long startTime = System.currentTimeMillis();
        getSolver().solve();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        getSolver().reset();

        // Print solution
        logger.debug("[SOL] Solution found in " + executionTime + " ms");
        logger.debug("  Region: " + regionModel.printRegion() + " | Habitat: " + getHabitat(habitat.getValue())
                + " | Size: " + getSize(size.getValue()));
        logger.debug("  Diet: " + getDiet(diet.getValue()) + " | Family: " + getFishFamily(fishFamily.getValue())
                + " | Species: " + getFishSpecies(fishSpecies.getValue()) + "\n");
    }

    public Set<String> getVariableNames() {
        return Set.of("region", "habitat", "size", "diet", "fishFamily", "fishSpecies");
    }

    public abstract String getHabitat(int value);

    public abstract String getSize(int value);

    public abstract String getDiet(int value);

    public abstract String getFishFamily(int value);

    public abstract String getFishSpecies(int value);
}

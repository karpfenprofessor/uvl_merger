package fish.model;

public class EuropeFishModel extends BaseModel {

    public EuropeFishModel() {
        super();
        regionModel = Region.EUROPE;

        // Initialize variables
        region = model.intVar("region", 0, 0); // Europe
        habitat = model.intVar("habitat", 0, 1); // Freshwater: 0, Saltwater: 1
        size = model.intVar("size", 0, 2); // S: 0, M: 1, L: 2
        diet = model.intVar("diet", 0, 2); // Herbivore: 0, Omnivore: 1, Carnivore: 2
        fishFamily = model.intVar("fishFamily", 0, 3); // Salmonidae: 0, Cyprinidae: 1, Percidae: 2, Gadidae: 3
        fishSpecies = model.intVar("fishSpecies", 0, 7); // Grayling: 0, Brown Trout: 1, Common Carp: 2, Roach: 3,
                                                         // European Perch: 4, Pikeperch: 5, Atlantic Cod: 6, Atlantic
                                                         // Halibut: 7

        // Constraint 1: Salmonidae found in both freshwater and saltwater
        model.ifThen(model.arithm(fishFamily, "=", 0),
                model.or(model.arithm(habitat, "=", 0), model.arithm(habitat, "=", 1)));

        // Constraint 2: Cyprinidae found primarily in freshwater
        model.ifThen(model.arithm(fishFamily, "=", 1),
                model.arithm(habitat, "=", 0));

        // Constraint 3: Larger fish like Atlantic Cod found in saltwater
        model.ifThen(model.arithm(fishSpecies, "=", 6),
                model.arithm(habitat, "=", 1));

        // Constraint 4: Pikeperch, a larger fish, is carnivorous
        model.ifThen(model.arithm(fishSpecies, "=", 5),
                model.arithm(diet, "=", 2));

        // Constraint 5: Herbivores are generally smaller in size
        model.ifThen(model.arithm(diet, "=", 0),
                model.arithm(size, "=", 0));

        // Constraint 6: Gadidae, including Atlantic Cod, generally large and in
        // saltwater
        model.ifThen(model.arithm(fishFamily, "=", 3),
                model.and(model.arithm(habitat, "=", 1), model.arithm(size, "=", 2)));

        // Constraint 7: Percidae found in freshwater, generally omnivorous
        model.ifThen(model.arithm(fishFamily, "=", 2),
                model.and(model.arithm(habitat, "=", 0), model.arithm(diet, "=", 1)));

        // Constraint 8: Omnivores like Roach found in both water types
        model.ifThen(model.arithm(fishSpecies, "=", 3),
                model.or(model.arithm(habitat, "=", 0), model.arithm(habitat, "=", 1)));

        // Constraint 9: Small size fish like Grayling in freshwater
        model.ifThen(model.arithm(fishSpecies, "=", 0),
                model.arithm(habitat, "=", 0));

        // Constraint 10: Brown Trout adaptable to both water types
        model.ifThen(model.arithm(fishSpecies, "=", 1),
                model.or(model.arithm(habitat, "=", 0), model.arithm(habitat, "=", 1)));

        // Constraint 11: Carnivores more likely in saltwater, like Atlantic Halibut
        model.ifThen(model.arithm(fishSpecies, "=", 7),
                model.arithm(diet, "=", 2));

        // Constraint 12: Large, carnivorous fish are more likely in deeper waters
        model.ifThen(model.arithm(size, "=", 2),
                model.and(model.arithm(habitat, "=", 1), model.arithm(diet, "=", 2)));

        // Constraint 13: European Perch, typically omnivorous in freshwater
        model.ifThen(model.arithm(fishSpecies, "=", 4),
                model.and(model.arithm(habitat, "=", 0), model.arithm(diet, "=", 1)));

        // Constraint 14: Common Carp, large and herbivorous, found in freshwater
        model.ifThen(model.arithm(fishSpecies, "=", 2),
                model.and(model.arithm(size, "=", 2), model.arithm(habitat, "=", 0)));

        // Constraint 15: Small and medium fish are more common in freshwater
        model.ifThen(model.arithm(size, "<", 2),
                model.arithm(habitat, "=", 0));

        // Constraint 16: Carnivorous diet primarily in saltwater
        model.ifThen(model.arithm(diet, "=", 2),
                model.arithm(habitat, "=", 1));

        // Constraint 17: Omnivores found across all size categories
        model.ifThen(model.arithm(diet, "=", 1),
                model.or(model.arithm(size, "=", 0), model.or(model.arithm(size, "=", 1), model.arithm(size, "=", 2))));

        // Constraint 18: Roach adaptable to both habitats
        model.ifThen(model.arithm(fishSpecies, "=", 3),
                model.or(model.arithm(habitat, "=", 0), model.arithm(habitat, "=", 1)));

        // Constraint 19: Large fish species in saltwater are mostly carnivorous
        model.ifThen(model.arithm(size, "=", 2),
                model.and(model.arithm(habitat, "=", 1), model.arithm(diet, "=", 2)));

        // Constraint 20: Freshwater habitats favor smaller, herbivorous fish
        model.ifThen(model.arithm(habitat, "=", 0),
                model.and(model.arithm(size, "=", 0), model.arithm(diet, "=", 0)));
    }

    @Override
    public String getSize(int value) {
        switch (value) {
            case 0:
                return "S";
            case 1:
                return "M";
            case 2:
                return "L";
            default:
                return "Unknown";
        }
    }

    @Override
    public String getHabitat(int value) {
        switch (value) {
            case 0:
                return "Freshwater";
            case 1:
                return "Saltwater";
            default:
                return "Unknown";
        }
    }

    @Override
    public String getDiet(int value) {
        switch (value) {
            case 0:
                return "Herbivore";
            case 1:
                return "Omnivore";
            case 2:
                return "Carnivore";
            default:
                return "Unknown";
        }
    }

    @Override
    public String getFishFamily(int value) {
        switch (value) {
            case 0:
                return "Salmonidae";
            case 1:
                return "Cyprinidae";
            case 2:
                return "Percidae";
            case 3:
                return "Gadidae";
            default:
                return "Unknown";
        }
    }

    @Override
    public String getFishSpecies(int value) {
        switch (value) {
            case 0:
                return "Grayling";
            case 1:
                return "Brown Trout";
            case 2:
                return "Common Carp";
            case 3:
                return "Roach";
            case 4:
                return "European Perch";
            case 5:
                return "Pikeperch";
            case 6:
                return "Atlantic Cod";
            case 7:
                return "Atlantic Halibut";
            default:
                return "Unknown";
        }
    }
}

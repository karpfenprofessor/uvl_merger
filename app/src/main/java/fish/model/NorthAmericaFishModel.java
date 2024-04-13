package fish.model;

public class NorthAmericaFishModel extends BaseModel {

    public NorthAmericaFishModel() {
        super();
        regionModel = Region.NORTH_AMERICA;

        region = model.intVar("region", 1, 1); // North America
        habitat = model.intVar("habitat", 0, 1); // Freshwater: 0, Saltwater: 1
        size = model.intVar("size", 0, 2); // S: 0, M: 1, L: 2
        diet = model.intVar("diet", 0, 2); // Herbivore: 0, Omnivore: 1, Carnivore: 2
        fishFamily = model.intVar("fishFamily", 0, 3); // Salmonidae: 0, Ictaluridae: 1, Serranidae: 2, Centrarchidae: 3
        fishSpecies = model.intVar("fishSpecies", 0, 7); // Chinook Salmon: 0, Rainbow Trout: 1, Channel Catfish: 2,
                                                         // Blue Catfish: 3, Black Sea Bass: 4, Red Grouper: 5,
                                                         // Bluegill: 6, Largemouth Bass: 7

        // Constraint 1: Salmonidae found in both freshwater and saltwater
        model.ifThen(model.arithm(fishFamily, "=", 0),
                model.or(model.arithm(habitat, "=", 0), model.arithm(habitat, "=", 1)));

        // Constraint 2: Ictaluridae primarily found in freshwater
        model.ifThen(model.arithm(fishFamily, "=", 1),
                model.arithm(habitat, "=", 0));

        // Constraint 3: Serranidae such as Red Grouper found in saltwater
        model.ifThen(model.arithm(fishFamily, "=", 2),
                model.arithm(habitat, "=", 1));

        // Constraint 4: Centrarchidae primarily freshwater and carnivorous
        model.ifThen(model.arithm(fishFamily, "=", 3),
                model.and(model.arithm(habitat, "=", 0), model.arithm(diet, "=", 2)));

        // Constraint 5: Large fish such as Black Sea Bass found in saltwater
        model.ifThen(model.arithm(fishSpecies, "=", 4),
                model.arithm(habitat, "=", 1));

        // Constraint 6: Small Ictaluridae like Channel Catfish are primarily herbivores
        model.ifThen(model.arithm(fishSpecies, "=", 2),
                model.arithm(diet, "=", 0));

        // Constraint 7: Medium-sized fish like Rainbow Trout are omnivores
        model.ifThen(model.arithm(fishSpecies, "=", 1),
                model.arithm(diet, "=", 1));

        // Constraint 8: Large fish such as Chinook Salmon found in both water types
        model.ifThen(model.arithm(fishSpecies, "=", 0),
                model.or(model.arithm(habitat, "=", 0), model.arithm(habitat, "=", 1)));

        // Constraint 9: Medium-sized Centrarchidae like Largemouth Bass are carnivorous
        model.ifThen(model.arithm(fishSpecies, "=", 7),
                model.arithm(diet, "=", 2));

        // Constraint 10: Small fish like Bluegill are omnivores
        model.ifThen(model.arithm(fishSpecies, "=", 6),
                model.arithm(diet, "=", 1));

        // Constraint 11: Carnivorous diet is common among large saltwater species
        model.ifThen(model.arithm(diet, "=", 2),
                model.arithm(habitat, "=", 1));

        // Constraint 12: Small to medium fish are primarily found in freshwater
        model.ifThen(model.arithm(size, "!=", 2),
                model.arithm(habitat, "=", 0));

        // Constraint 13: Large fish in saltwater are typically carnivorous
        model.ifThen(model.and(model.arithm(size, "=", 2), model.arithm(habitat, "=", 1)),
                model.arithm(diet, "=", 2));

        // Constraint 14: Herbivores are typically small in size
        model.ifThen(model.arithm(diet, "=", 0),
                model.arithm(size, "=", 0));

        // Constraint 15: Omnivores found across all size categories
        model.ifThen(model.arithm(diet, "=", 1),
                model.or(model.arithm(size, "=", 0), model.or(model.arithm(size, "=", 1), model.arithm(size, "=", 2))));

        // Constraint 16: Freshwater environments typically host smaller fish
        model.ifThen(model.arithm(habitat, "=", 0),
                model.or(model.arithm(size, "=", 0), model.arithm(size, "=", 1)));

        // Constraint 17: Large freshwater fish are generally carnivorous
        model.ifThen(model.and(model.arithm(size, "=", 2), model.arithm(habitat, "=", 0)),
                model.arithm(diet, "=", 2));

        // Constraint 18: Saltwater habitats predominantly host medium to large fish
        model.ifThen(model.arithm(habitat, "=", 1),
                model.or(model.arithm(size, "=", 1), model.arithm(size, "=", 2)));

        // Constraint 19: Medium-sized freshwater fish are typically omnivorous
        model.ifThen(model.and(model.arithm(size, "=", 1), model.arithm(habitat, "=", 0)),
                model.arithm(diet, "=", 1));

        // Constraint 20: Small fish in freshwater environments are often herbivores
        model.ifThen(model.and(model.arithm(size, "=", 0), model.arithm(habitat, "=", 0)),
                model.arithm(diet, "=", 0));

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
                return "Ictaluridae";
            case 2:
                return "Serranidae";
            case 3:
                return "Centrarchidae";
            default:
                return "Unknown";
        }
    }

    @Override
    public String getFishSpecies(int value) {
        switch (value) {
            case 0:
                return "Chinook Salmon";
            case 1:
                return "Rainbow Trout";
            case 2:
                return "Channel Catfish";
            case 3:
                return "Blue Catfish";
            case 4:
                return "Black Sea Bass";
            case 5:
                return "Red Grouper";
            case 6:
                return "Bluegill";
            case 7:
                return "Largemouth Bass";
            default:
                return "Unknown";
        }
    }
}

package car.model.recreate.constraints;

public abstract class AbstractConstraint {
    private boolean isContextualized;
    private Integer contextualizationValue;

    public AbstractConstraint() {
        this.isContextualized = Boolean.FALSE;
        contextualizationValue = null;
    }

    public AbstractConstraint(boolean isContextualized, Integer contextualizationValue) {
        this.isContextualized = isContextualized;
        this.contextualizationValue = contextualizationValue;
    }

    public boolean isContextualized() {
        return isContextualized;
    }

    public Integer getContextualizationValue() {
        return contextualizationValue;
    }

    public void setContextualize(Integer value) {
        this.isContextualized = Boolean.TRUE;
        this.contextualizationValue = value;
    }
}

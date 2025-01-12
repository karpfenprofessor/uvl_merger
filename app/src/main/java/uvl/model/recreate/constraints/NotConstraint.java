package uvl.model.recreate.constraints;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NotConstraint extends AbstractConstraint {

    public AbstractConstraint inner;

    public NotConstraint(AbstractConstraint c) {
        this.inner = c;
    }

    @Override
    public AbstractConstraint copy() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }
    
}

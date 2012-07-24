
package org.wrml.model.schema.constraint.text;

import org.wrml.model.schema.Constraint;

public interface SyntaxConstraint extends Constraint {

    public String getSyntaxName();

    public void setSyntaxName(String syntaxName);
}

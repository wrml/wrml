
package org.wrml.model.schema.constraint;

import java.net.URI;

import org.wrml.model.schema.Constraint;

public interface SchemaConstraint extends Constraint {

    public URI getConstrainedSchemaId();

    public void setConstrainedSchemaId(URI schemaId);
}

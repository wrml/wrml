
package org.wrml.contrib.runtime.service.groovy;

import org.wrml.model.Model;
import org.wrml.runtime.Context;
import org.wrml.runtime.Dimensions;
import org.wrml.runtime.Keys;

public interface GroovyTemplate 
{
    public <M extends Model> M fill(Context context, M model, Keys keys, Dimensions dimensions);
}
